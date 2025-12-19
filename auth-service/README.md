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

## Testing

### Running Unit Tests

The auth-service includes comprehensive unit tests for both the service and controller layers. Run all tests using:

```bash
# Run all auth-service tests
./gradlew :auth-service:test

# Run with verbose output
./gradlew :auth-service:test --info
```

### Test Coverage

The test suite covers the following areas:

**AuthService Tests** (`AuthServiceTest.java`):
- User registration with valid data
- Duplicate email detection
- Email normalization (case insensitivity)
- Name trimming
- Password hashing
- Default value assignment

**AuthController Tests** (`AuthControllerTest.java`):
- Successful signup (201 Created)
- Duplicate email handling (409 Conflict)
- Validation errors for missing/invalid fields (400 Bad Request)

**UserService Tests** (`UserServiceTest.java`):
- Get user by ID (success and not found scenarios)
- Update user profile with valid name
- Name trimming on update
- Handling null/blank name updates
- User not found on update

**UserController Tests** (`UserControllerTest.java`):
- GET /users/me - Returns current user profile
- GET /users/me - Returns 404 when user not found
- GET /users/{id} - Returns user by ID
- GET /users/{id} - Returns 404 when user not found
- PUT /users/me - Updates user profile successfully
- PUT /users/me - Returns 404 when user not found
- PUT /users/me - Validates name length constraint

### Test Architecture

Tests use the following patterns:

- **Controller Tests**: Use `@WebMvcTest` with `MockMvc` for HTTP layer testing. Services are mocked using `@MockBean`.
- **Service Tests**: Use `@ExtendWith(MockitoExtension.class)` for pure unit testing with mocked repositories.
- **Security**: Tests use `@WithMockUser` to bypass Spring Security authentication.
- **Assertions**: AssertJ is used for fluent assertions.

### Testing User Profile Endpoints with curl

After starting the service, you can test the user profile endpoints. Note that these endpoints require the `X-User-Id` header which is normally set by the API Gateway after JWT validation.

#### Get Current User Profile

```bash
curl -X GET http://localhost:8081/users/me \
  -H "X-User-Id: 550e8400-e29b-41d4-a716-446655440000"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
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

#### Get User by ID (Internal Use)

```bash
curl -X GET http://localhost:8081/users/550e8400-e29b-41d4-a716-446655440000
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
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

#### Update User Profile

```bash
curl -X PUT http://localhost:8081/users/me \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{
    "name": "Updated Name"
  }'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "User profile updated successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "test@example.com",
    "name": "Updated Name",
    "emailVerified": false,
    "subscriptionTier": "free",
    "createdAt": "2025-12-19T15:00:00",
    "updatedAt": "2025-12-19T15:00:00"
  }
}
```

#### User Not Found

```bash
curl -X GET http://localhost:8081/users/00000000-0000-0000-0000-000000000000
```

**Expected Response (404 Not Found):**
```json
{
  "success": false,
  "message": "User with id '00000000-0000-0000-0000-000000000000' not found",
  "data": null
}
```

## Integration
All other services validate JWT tokens issued by this service. The auth service provides:
- Public key for JWT validation
- User context (user ID, roles, tenant)
- Inter-service authentication
