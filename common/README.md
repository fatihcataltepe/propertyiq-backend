# Common Module

## Purpose
Shared library module containing common utilities, DTOs, exceptions, and configurations used across all PropertyIQ microservices.

## Contents

### Exception Handling
- `GlobalExceptionHandler` - Centralized exception handling
- Custom exception classes:
  - `ResourceNotFoundException`
  - `UnauthorizedException`
  - `ValidationException`
  - `ServiceUnavailableException`

### Common DTOs
- `ErrorResponse` - Standard error response format
- `ApiResponse<T>` - Generic API response wrapper
- `PagedResponse<T>` - Paginated response wrapper
- `UserContext` - User information from JWT

### Utilities
- `DateUtils` - Date and time utilities
- `ValidationUtils` - Common validation helpers
- `CurrencyUtils` - Currency formatting and calculations
- `JwtUtils` - JWT token parsing and validation

### Constants
- `ApiConstants` - API path constants
- `ErrorCodes` - Standard error codes
- `DateFormats` - Date format patterns

### Configurations
- `WebConfig` - Common web configurations
- `SecurityConfig` - Shared security configurations
- `JacksonConfig` - JSON serialization settings

### Annotations
- `@CurrentUser` - Inject current user into controller methods
- `@ValidEnum` - Custom enum validation
- `@ValidCurrency` - Currency format validation

## Usage
Include this module as a dependency in service `build.gradle`:

```gradle
dependencies {
    implementation project(':common')
    // other dependencies
}
```

## Key Features

### Consistent Error Handling
All services return errors in the same format:
```json
{
  "timestamp": "2025-12-18T23:44:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Property not found",
  "code": "PROPERTY_NOT_FOUND",
  "path": "/api/properties/123"
}
```

### JWT Token Validation
Shared utilities for validating JWT tokens across services:
- Extract user ID and roles
- Verify token signature
- Check expiration
- Extract tenant information

### Logging Standards
Common logging configuration and correlation ID support for distributed tracing.

### Pagination
Standard pagination support:
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "last": false
}
```

## Design Principles
- Keep it lightweight
- No business logic
- Only truly shared code
- Avoid circular dependencies
- Backward compatible changes only

## Testing
Contains shared test utilities and fixtures for use in service tests.
