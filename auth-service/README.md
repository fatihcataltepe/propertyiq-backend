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

## Integration
All other services validate JWT tokens issued by this service. The auth service provides:
- Public key for JWT validation
- User context (user ID, roles, tenant)
- Inter-service authentication
