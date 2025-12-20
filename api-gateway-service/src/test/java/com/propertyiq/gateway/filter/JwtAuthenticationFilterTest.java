package com.propertyiq.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

class JwtAuthenticationFilterTest {

    private static final String TEST_SECRET = "this-is-a-test-secret-key-that-is-at-least-256-bits-long-for-hs256";
    private static final String TEST_USER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String TEST_EMAIL = "test@example.com";

    private JwtAuthenticationFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "supabaseJwtSecret", TEST_SECRET);
        filter.init();

        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldRejectRequestWithoutAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldRejectRequestWithInvalidAuthorizationFormat() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Basic invalid")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldRejectExpiredToken() {
        String expiredToken = createSupabaseJwt(TEST_USER_ID, TEST_EMAIL, "authenticated", null, -3600);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        String differentSecret = "different-secret-key-that-is-also-at-least-256-bits-long-for-hs256";
        String tokenWithWrongSignature = createJwtWithSecret(TEST_USER_ID, TEST_EMAIL, "authenticated", differentSecret);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithWrongSignature)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldAcceptValidSupabaseToken() {
        String validToken = createSupabaseJwt(TEST_USER_ID, TEST_EMAIL, "authenticated", null, 3600);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldExtractRoleFromUserMetadata() {
        Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("role", "LANDLORD");

        String token = createSupabaseJwt(TEST_USER_ID, TEST_EMAIL, "authenticated", userMetadata, 3600);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        gatewayFilter.filter(exchange, chain).block();
    }

    @Test
    void shouldUseDefaultRoleWhenNoRoleProvided() {
        String token = createSupabaseJwt(TEST_USER_ID, TEST_EMAIL, null, null, 3600);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        gatewayFilter.filter(exchange, chain).block();
    }

    @Test
    void shouldHandleTokenWithAppMetadataRole() {
        Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("role", "ADMIN");

        String token = createSupabaseJwtWithAppMetadata(TEST_USER_ID, TEST_EMAIL, null, appMetadata, 3600);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        gatewayFilter.filter(exchange, chain).block();
    }

    private String createSupabaseJwt(String userId, String email, String role, Map<String, Object> userMetadata, int expirationSeconds) {
        byte[] keyBytes = TEST_SECRET.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        if (role != null) {
            claims.put("role", role);
        }
        if (userMetadata != null) {
            claims.put("user_metadata", userMetadata);
        }
        claims.put("iss", "https://test-project.supabase.co/auth/v1");
        claims.put("aud", "authenticated");

        return Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000L))
                .signWith(key)
                .compact();
    }

    private String createSupabaseJwtWithAppMetadata(String userId, String email, String role, Map<String, Object> appMetadata, int expirationSeconds) {
        byte[] keyBytes = TEST_SECRET.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        if (role != null) {
            claims.put("role", role);
        }
        if (appMetadata != null) {
            claims.put("app_metadata", appMetadata);
        }
        claims.put("iss", "https://test-project.supabase.co/auth/v1");
        claims.put("aud", "authenticated");

        return Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000L))
                .signWith(key)
                .compact();
    }

    private String createJwtWithSecret(String userId, String email, String role, String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);

        return Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600 * 1000L))
                .signWith(key)
                .compact();
    }
}
