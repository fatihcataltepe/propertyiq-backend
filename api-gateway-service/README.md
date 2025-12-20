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
- **Supabase JWT Authentication**: Validates JWT tokens issued by Supabase Auth
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
- JWT (io.jsonwebtoken)

## Port
8080 (main public-facing port)

## Configuration

### Environment Variables
- `SUPABASE_JWT_SECRET` - JWT secret from your Supabase project (Settings > API > JWT Settings)
- `SUPABASE_URL` - Your Supabase project URL (e.g., `https://your-project.supabase.co`)
- `SPRING_REDIS_HOST` - Redis host for rate limiting
- `SPRING_REDIS_PORT` - Redis port

### Obtaining Supabase JWT Secret
1. Go to your Supabase project dashboard at [supabase.com](https://supabase.com)
2. Navigate to **Settings** > **API**
3. Under **JWT Settings**, copy the **JWT Secret**
4. Set this as the `SUPABASE_JWT_SECRET` environment variable

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
  - Validates tokens using the Supabase JWT secret (HS256)
  - Extracts user ID from `sub` claim, email from `email` claim
  - Extracts roles from `user_metadata.role`, `app_metadata.role`, or `role` claim
  - Can be applied selectively to protected routes
  - Public routes (e.g., `/api/auth/login`, `/api/auth/signup`) bypass this

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

1. Set the required environment variables:
```bash
export SUPABASE_JWT_SECRET="your-supabase-jwt-secret"
export SUPABASE_URL="https://your-project.supabase.co"
```

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
