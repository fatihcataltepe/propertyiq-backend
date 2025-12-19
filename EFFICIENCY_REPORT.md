# PropertyIQ Backend - Efficiency Analysis Report

This report identifies several areas in the codebase where efficiency improvements could be made.

## Issue 1: JWT Parser Recreation on Every Request (HIGH IMPACT)

**Location:** `api-gateway-service/src/main/java/com/propertyiq/gateway/filter/JwtAuthenticationFilter.java`

**Current Code:**
```java
Claims claims = Jwts.parser()
        .setSigningKey(jwtSecret.getBytes())
        .build()
        .parseClaimsJws(token)
        .getBody();
```

**Problem:** The JWT parser is created fresh for every incoming request that requires authentication. This includes:
- Creating a new parser builder instance
- Converting the secret string to bytes on every request
- Building a new immutable parser instance

Since this filter runs on the gateway's hot path for almost every authenticated request, this creates unnecessary CPU overhead and memory allocation pressure.

**Recommended Fix:** Cache the `JwtParser` instance at startup using `@PostConstruct` and reuse it for all requests. The built `JwtParser` is thread-safe and designed for reuse.

**Impact:** Reduces per-request CPU usage and garbage collection pressure on the API gateway, which handles all incoming traffic.

---

## Issue 2: UUID Generation for Correlation IDs (LOW IMPACT)

**Location:** `api-gateway-service/src/main/java/com/propertyiq/gateway/filter/GlobalLoggingFilter.java` (line 25)

**Current Code:**
```java
correlationId = UUID.randomUUID().toString();
```

**Problem:** `UUID.randomUUID()` uses `SecureRandom` internally, which while cryptographically secure, is more expensive than necessary for correlation IDs that don't require cryptographic properties.

**Recommended Fix:** Consider using a faster ID generator like `ThreadLocalRandom` combined with a simple counter, or libraries like `java-uuid-generator` (JUG) with time-based UUIDs. However, this is a micro-optimization and should only be considered after addressing higher-impact issues.

**Impact:** Minimal - the overhead of UUID generation is typically dwarfed by network I/O and other gateway operations.

---

## Issue 3: HashMap Creation in Exception Handlers (LOW IMPACT)

**Location:** `common/src/main/java/com/propertyiq/common/exception/GlobalExceptionHandler.java` (lines 17, 26)

**Current Code:**
```java
Map<String, Object> error = new HashMap<>();
error.put("timestamp", LocalDateTime.now());
error.put("message", ex.getMessage());
error.put("status", HttpStatus.NOT_FOUND.value());
```

**Problem:** A new `HashMap` is created for each error response instead of using a dedicated error response DTO class.

**Recommended Fix:** Create an `ErrorResponse` DTO class that can be directly serialized. This improves type safety and allows Jackson to optimize serialization.

**Impact:** Low - this code only executes on error paths, which should be infrequent in normal operation.

---

## Issue 4: Using Maps Instead of DTOs in Controllers (LOW IMPACT)

**Location:** 
- `auth-service/src/main/java/com/propertyiq/auth/controller/AuthController.java`
- `auth-service/src/main/java/com/propertyiq/auth/controller/UserController.java`

**Current Code:**
```java
Map<String, String> response = new HashMap<>();
response.put("token", "sample-jwt-token");
return ResponseEntity.ok(ApiResponse.success(response));
```

**Problem:** Using `Map<String, String>` and `Map<String, Object>` instead of proper DTOs is less type-safe and potentially less efficient for JSON serialization.

**Recommended Fix:** Create dedicated request/response DTO classes (e.g., `LoginRequest`, `LoginResponse`, `UserResponse`). This improves type safety, documentation, and allows Jackson to optimize serialization paths.

**Impact:** Low - primarily a maintainability and type-safety concern rather than a significant performance issue.

---

## Issue 5: Inconsistent Collection Factory Usage (NEGLIGIBLE IMPACT)

**Location:** `api-gateway-service/src/main/java/com/propertyiq/gateway/config/CorsConfig.java` (lines 18-20)

**Current Code:**
```java
corsConfig.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
```

**Problem:** Mixing `List.of()` and `Arrays.asList()` is inconsistent. While `List.of()` returns an immutable list and `Arrays.asList()` returns a fixed-size list backed by an array, the performance difference is negligible.

**Recommended Fix:** Use `List.of()` consistently for immutable lists.

**Impact:** Negligible - this is a one-time startup configuration.

---

## Summary

| Issue | Impact | Effort | Recommendation |
|-------|--------|--------|----------------|
| JWT Parser Recreation | HIGH | Low | Fix immediately |
| UUID Generation | LOW | Medium | Consider later |
| HashMap in Exception Handlers | LOW | Low | Nice to have |
| Maps Instead of DTOs | LOW | Medium | Nice to have |
| Inconsistent Collection Factory | NEGLIGIBLE | Low | Optional cleanup |

The JWT parser caching issue (Issue 1) is the most impactful and should be addressed first, as it affects every authenticated request passing through the API gateway.
