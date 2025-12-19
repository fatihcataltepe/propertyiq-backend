# Auth & Identity Service

## Responsibility
Manages user authentication, authorization, JWT token issuance and validation, and tenant isolation for PropertyIQ platform.

## Core Features
- User registration and login
- JWT-based authentication
- OAuth2 support (future)
- Role-based access control (RBAC)
- Tenant isolation (landlords vs accountants)
- Session management
- Password reset and email verification

## Main APIs
- `POST /api/auth/signup` - User registration
- `POST /api/auth/login` - User authentication
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/logout` - Invalidate session
- `GET /api/users/me` - Get current user profile
- `GET /api/users/{id}` - Get user by ID (internal use)
- `PUT /api/users/me` - Update user profile
- `POST /api/auth/forgot-password` - Password reset request
- `POST /api/auth/reset-password` - Complete password reset

## Database
- Schema: `auth_db`
- Tables:
  - `users` - User accounts and credentials
    - id, email, password_hash, name, created_at, subscription_tier
  - `sessions` - Active sessions (optional)
  - `refresh_tokens` - Refresh token storage
  - `roles` - User roles
  - `user_roles` - User-role mapping

## Security
- Passwords hashed with BCrypt
- JWT tokens with configurable expiration
- Refresh token rotation
- Rate limiting on auth endpoints
- Account lockout after failed attempts

## Technologies
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (io.jsonwebtoken:jjwt)

## Port
8081

## Environment Variables
- `JWT_SECRET` - Secret key for JWT signing
- `JWT_EXPIRATION` - JWT token expiration time (default: 1h)
- `REFRESH_TOKEN_EXPIRATION` - Refresh token expiration (default: 7d)

## Local Development with Docker

### Prerequisites

Make sure you don't have a local PostgreSQL running on port 5432 (it will conflict with Docker):
```bash
# Check what's using port 5432
lsof -iTCP:5432 -sTCP:LISTEN

# If you have Homebrew PostgreSQL running, stop it:
brew services stop postgresql
```

### Starting Dependencies

From the project root directory:
```bash
# Start PostgreSQL and Redis containers
docker-compose up -d

# Verify containers are running
docker-compose ps
```

### Running the Auth Service

```bash
# From project root
./gradlew :auth-service:bootRun
```

The service will start on port 8081 and automatically create the `users` table in the `auth_db` database.

### Testing with curl

#### 1. Successful User Registration

```bash
curl -X POST http://localhost:8081/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "Test User"
  }'
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "test@example.com",
    "name": "Test User",
    "emailVerified": false,
    "subscriptionTier": "free",
    "createdAt": "2025-12-19T15:00:00",
    "updatedAt": "2025-12-19T15:00:00"
  }
}
```

#### 2. Duplicate Email Registration

```bash
curl -X POST http://localhost:8081/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "Another User"
  }'
```

**Expected Response (409 Conflict):**
```json
{
  "success": false,
  "message": "User with email 'test@example.com' already exists",
  "data": null
}
```

#### 3. Email Case Insensitivity

Emails are normalized to lowercase, so `TEST@EXAMPLE.COM` is treated the same as `test@example.com`:

```bash
curl -X POST http://localhost:8081/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "TEST@EXAMPLE.COM",
    "password": "password123",
    "name": "Uppercase Email User"
  }'
```

**Expected Response (409 Conflict):**
```json
{
  "success": false,
  "message": "User with email 'test@example.com' already exists",
  "data": null
}
```

#### 4. Invalid Email Format

```bash
curl -X POST http://localhost:8081/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "invalid-email",
    "password": "password123",
    "name": "Test User"
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "email: Invalid email format",
  "data": null
}
```

#### 5. Password Too Short

```bash
curl -X POST http://localhost:8081/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "short",
    "name": "Test User"
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "password: Password must be between 8 and 100 characters",
  "data": null
}
```

#### 6. Missing Required Fields

```bash
curl -X POST http://localhost:8081/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com"
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "password: Password is required; name: Name is required",
  "data": null
}
```

### Login

#### 1. Successful Login

```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "test@example.com",
      "name": "Test User",
      "emailVerified": false,
      "subscriptionTier": "free",
      "createdAt": "2025-12-19T15:00:00",
      "updatedAt": "2025-12-19T15:00:00"
    }
  }
}
```

#### 2. Invalid Credentials (Wrong Password)

```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "wrongpassword"
  }'
```

**Expected Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Invalid email or password",
  "data": null
}
```

#### 3. Invalid Credentials (Non-existent User)

```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nonexistent@example.com",
    "password": "password123"
  }'
```

**Expected Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Invalid email or password",
  "data": null
}
```

#### 4. Email Case Insensitivity

Emails are normalized to lowercase, so you can login with any case:

```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "TEST@EXAMPLE.COM",
    "password": "password123"
  }'
```

**Expected Response (200 OK):** Same as successful login above.

### Logout

#### 1. Successful Logout

```bash
curl -X POST http://localhost:8081/auth/logout
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null
}
```

**Note:** This is a stateless logout. The client is responsible for deleting the stored access token. Server-side token invalidation (via token blacklist or refresh token revocation) is not yet implemented.

### Verifying Data in PostgreSQL

You can connect to the PostgreSQL container to verify the data:

```bash
# Connect to the database
docker exec -it propertyiq-postgres psql -U postgres -d auth_db

# List users
SELECT id, email, name, emailverified, subscriptiontier, createdat FROM users;

# Exit psql
\q
```

### Stopping Services

```bash
# Stop the auth-service with Ctrl+C

# Stop and remove Docker containers (keeps data)
docker-compose down

# Stop and remove containers AND volumes (deletes all data)
docker-compose down -v
```

## Integration
All other services validate JWT tokens issued by this service. The auth service provides:
- Public key for JWT validation
- User context (user ID, roles, tenant)
- Inter-service authentication
