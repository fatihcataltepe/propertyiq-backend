# API Gateway Service

## Responsibility
Single entry point for all client requests to PropertyIQ backend services. Handles routing, authentication, rate limiting, and cross-cutting concerns.

## Key Features

### Request Routing
Routes incoming requests to appropriate microservices:
- `/api/auth/**`, `/api/users/**` → Auth Service (8081)
- `/api/properties/**`, `/api/portfolio/**` → Portfolio Service (8082)
- `/api/expenses/**` → Expense Service (8083)
- `/api/analytics/**` → Analytics Service (8084)
- `/api/reports/**` → Reporting Service (8085)
- `/api/notifications/**` → Notification Service (8086)

### Security
- **Supabase JWT Authentication (JWKS)**: Validates JWT tokens using public keys from Supabase's JWKS endpoint
- **No Secrets Required**: Uses asymmetric key validation - only needs `SUPABASE_URL`, no JWT secret storage
- **Token Parsing**: Extracts user context (ID, email, roles) from Supabase JWT claims and forwards to services
- **CORS Handling**: Centralized CORS configuration for frontend access
- **SSL/TLS Termination**: Single point for HTTPS (production)

### Cross-Cutting Concerns
- **Correlation IDs**: Generates/propagates correlation IDs for distributed tracing
- **Request/Response Logging**: Centralized logging with correlation context
- **Rate Limiting**: Protects backend services from overload (configurable)
- **Circuit Breaking**: Fault tolerance patterns (future enhancement)

### Monitoring
- Health checks: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus endpoint: `/actuator/prometheus`

## Technology Stack
- Spring Cloud Gateway (reactive, non-blocking)
- Spring Boot Actuator (monitoring)
- Redis (rate limiting, session storage)
- JWT (io.jsonwebtoken) with JWKS support (nimbus-jose-jwt)

## Port
8080 (main public-facing port)

## Configuration

### Environment Variables
- `SUPABASE_URL` - Your Supabase project URL (e.g., `https://your-project.supabase.co`)
- `SPRING_REDIS_HOST` - Redis host for rate limiting
- `SPRING_REDIS_PORT` - Redis port

### Supabase JWKS Configuration
The API Gateway uses JWKS (JSON Web Key Set) for JWT validation, which means **no secrets need to be stored**. The gateway automatically fetches public keys from Supabase's JWKS endpoint.

**Important**: Your Supabase project must use asymmetric JWT signing (RS256 or ES256) for JWKS validation to work. To enable this:
1. Go to your Supabase project dashboard at [supabase.com](https://supabase.com)
2. Navigate to **Settings** > **API** > **JWT Settings**
3. Ensure asymmetric signing is enabled (RS256 recommended)

The gateway fetches keys from: `{SUPABASE_URL}/auth/v1/.well-known/jwks.json`

### Service URLs
Configure downstream service URLs via environment or `application.yml`:
```yaml
services:
  auth:
    url: http://auth-service:8081
  portfolio:
    url: http://portfolio-service:8082
  # ... etc
```

## Filters

### Global Filters
- **GlobalLoggingFilter**: Logs all requests/responses with correlation IDs
- **CORS Filter**: Handles cross-origin requests

### Route Filters
- **JwtAuthenticationFilter**: Validates Supabase JWT tokens and adds user headers
  - Validates tokens using public keys from Supabase JWKS endpoint (RS256/ES256)
  - Extracts `kid` (key ID) from JWT header to select correct public key
  - Extracts user ID from `sub` claim, email from `email` claim
  - Extracts roles from `user_metadata.role`, `app_metadata.role`, or `role` claim
  - Caches JWKS keys for 10 minutes to minimize network calls
  - Can be applied selectively to protected routes
  - Public routes (e.g., `/api/auth/login`, `/api/auth/signup`) bypass this

- **JwksKeyProvider**: Manages JWKS key fetching and caching
  - Fetches public keys from `{SUPABASE_URL}/auth/v1/.well-known/jwks.json`
  - Caches keys with 10-minute TTL
  - Supports key rotation via `kid` claim lookup

## Request Flow

1. Client sends request to `http://api.propertyiq.com/api/properties`
2. Gateway generates/extracts correlation ID
3. Logs incoming request
4. Validates JWT token (if protected route)
5. Extracts user context from JWT
6. Adds headers: `X-User-Id`, `X-User-Email`, `X-User-Roles`, `X-Correlation-Id`
7. Routes to Portfolio Service at `http://localhost:8082/api/properties`
8. Returns response to client
9. Logs response status

## Example Headers Forwarded to Services

```
X-User-Id: 12345
X-User-Email: user@example.com
X-User-Roles: ROLE_USER,ROLE_LANDLORD
X-Correlation-Id: 550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer eyJhbGc...
```

Downstream services can use these headers without re-validating JWT.

## Public Routes (No JWT Required)

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /actuator/health`

## Protected Routes (JWT Required)

All other routes require valid JWT in `Authorization: Bearer <token>` header.

## Rate Limiting (Future)

Configure per-user or per-IP rate limits:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

## Production Deployment

### Docker
```dockerfile
FROM eclipse-temurin:21-jre-alpine
COPY build/libs/api-gateway-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes Service
```yaml
apiVersion: v1
kind: Service
metadata:
  name: api-gateway-service
spec:
  type: LoadBalancer
  ports:
    - port: 80
      targetPort: 8080
  selector:
    app: api-gateway-service
```

## Testing

### Running Unit Tests
```bash
./gradlew :api-gateway-service:test
```

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Running Locally with Supabase Authentication

1. Configure the Supabase URL using one of these methods:

**Option A: Environment variable**
```bash
export SUPABASE_URL="https://your-project.supabase.co"
```

**Option B: Config file** (create `application-local.yml` in `src/main/resources/`)
```yaml
supabase:
  url: https://your-project.supabase.co
```
Then run with: `./gradlew :api-gateway-service:bootRun --args='--spring.profiles.active=local'`

**Note:** The gateway will fail to start if `SUPABASE_URL` is not configured. No JWT secret is required - the gateway fetches public keys from Supabase's JWKS endpoint.

2. Start the API Gateway:
```bash
./gradlew :api-gateway-service:bootRun
```

3. Get a JWT token from your frontend (which uses Supabase Auth) or use the Supabase client:
```javascript
// In your frontend using Supabase JS client
const { data: { session } } = await supabase.auth.getSession()
const jwt = session?.access_token
```

4. Test a protected route with the Supabase JWT:
```bash
# Using a Supabase access token
curl http://localhost:8080/api/properties \
  -H "Authorization: Bearer <your-supabase-access-token>"
```

### Test with a Sample Supabase JWT
The Supabase JWT contains the following claims that are extracted by the gateway:
- `sub` - User ID (UUID) -> forwarded as `X-User-Id` header
- `email` - User email -> forwarded as `X-User-Email` header
- `role` or `user_metadata.role` or `app_metadata.role` -> forwarded as `X-User-Roles` header

### Verifying Headers are Forwarded
When a valid JWT is provided, the gateway adds these headers to downstream service requests:
```
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
X-User-Email: user@example.com
X-User-Roles: authenticated
```

## Benefits

- **Simplified Client**: Frontend only needs to know one URL
- **Security**: Centralized authentication and authorization
- **Monitoring**: Single point for observability
- **Flexibility**: Easy to add/remove services without client changes
- **Performance**: Connection pooling, load balancing
- **Resilience**: Circuit breakers, retries, timeouts
