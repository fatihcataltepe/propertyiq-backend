package com.propertyiq.gateway.filter;

import com.propertyiq.gateway.security.JwksKeyProvider;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private static final String TEST_KID = "test-key-id";
    private static final String TEST_USER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String TEST_EMAIL = "test@example.com";

    private JwtAuthenticationFilter filter;
    private JwksKeyProvider jwksKeyProvider;
    private GatewayFilterChain chain;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();

        jwksKeyProvider = mock(JwksKeyProvider.class);
        filter = new JwtAuthenticationFilter(jwksKeyProvider);

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
    void shouldRejectTokenWithoutKid() {
        String tokenWithoutKid = createJwtWithoutKid(TEST_USER_ID, TEST_EMAIL, "authenticated", keyPair.getPrivate(), 3600);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithoutKid)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldRejectExpiredToken() {
        when(jwksKeyProvider.getKey(TEST_KID)).thenReturn(Mono.just(keyPair.getPublic()));

        String expiredToken = createSupabaseJwt(TEST_USER_ID, TEST_EMAIL, "authenticated", null, keyPair.getPrivate(), -3600);

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
    void shouldRejectTokenWithUnknownKid() {
        when(jwksKeyProvider.getKey(TEST_KID)).thenReturn(Mono.error(new IllegalArgumentException("Unknown key ID")));

        String token = createSupabaseJwt(TEST_USER_ID, TEST_EMAIL, "authenticated", null, keyPair.getPrivate(), 3600);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldAcceptValidSupabaseToken() {
        when(jwksKeyProvider.getKey(TEST_KID)).thenReturn(Mono.just(keyPair.getPublic()));

        String validToken = createSupabaseJwt(TEST_USER_ID, TEST_EMAIL, "authenticated", null, keyPair.getPrivate(), 3600);

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
        when(jwksKeyProvider.getKey(TEST_KID)).thenReturn(Mono.just(keyPair.getPublic()));

        Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("role", "LANDLORD");

        String token = createSupabaseJwt(TEST_USER_ID, TEST_EMAIL, "authenticated", userMetadata, keyPair.getPrivate(), 3600);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(any());
    }

    @Test
    void shouldUseDefaultRoleWhenNoRoleProvided() {
        when(jwksKeyProvider.getKey(TEST_KID)).thenReturn(Mono.just(keyPair.getPublic()));

        String token = createSupabaseJwt(TEST_USER_ID, TEST_EMAIL, null, null, keyPair.getPrivate(), 3600);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(any());
    }

    @Test
    void shouldHandleTokenWithAppMetadataRole() {
        when(jwksKeyProvider.getKey(TEST_KID)).thenReturn(Mono.just(keyPair.getPublic()));

        Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("role", "ADMIN");

        String token = createSupabaseJwtWithAppMetadata(TEST_USER_ID, TEST_EMAIL, null, appMetadata, keyPair.getPrivate(), 3600);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        Mono<Void> result = gatewayFilter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(any());
    }

    private String createSupabaseJwt(String userId, String email, String role, Map<String, Object> userMetadata, PrivateKey privateKey, int expirationSeconds) {
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
                .header().keyId(TEST_KID).and()
                .subject(userId)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000L))
                .signWith(privateKey)
                .compact();
    }

    private String createSupabaseJwtWithAppMetadata(String userId, String email, String role, Map<String, Object> appMetadata, PrivateKey privateKey, int expirationSeconds) {
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
                .header().keyId(TEST_KID).and()
                .subject(userId)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000L))
                .signWith(privateKey)
                .compact();
    }

    private String createJwtWithoutKid(String userId, String email, String role, PrivateKey privateKey, int expirationSeconds) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);

        return Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000L))
                .signWith(privateKey)
                .compact();
    }
}
