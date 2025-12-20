package com.propertyiq.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${supabase.jwt.secret:your-supabase-jwt-secret-change-this-in-production}")
    private String supabaseJwtSecret;

    private JwtParser jwtParser;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = supabaseJwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtParser = Jwts.parser()
                .verifyWith(key)
                .build();
        logger.info("JwtAuthenticationFilter initialized with Supabase JWT secret");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (!authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = jwtParser.parseSignedClaims(token).getPayload();

                String userId = claims.getSubject();
                String email = claims.get("email", String.class);
                String role = extractRole(claims);

                logger.debug("JWT validated for user: {}, email: {}, role: {}", userId, email, role);

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(r -> r.header("X-User-Id", userId)
                                      .header("X-User-Email", email != null ? email : "")
                                      .header("X-User-Roles", role != null ? role : "authenticated"))
                        .build();

                return chain.filter(modifiedExchange);
            } catch (Exception e) {
                logger.warn("JWT validation failed: {}", e.getMessage());
                return onError(exchange, "Invalid JWT token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private String extractRole(Claims claims) {
        Map<String, Object> userMetadata = claims.get("user_metadata", Map.class);
        if (userMetadata != null && userMetadata.containsKey("role")) {
            return userMetadata.get("role").toString();
        }

        Map<String, Object> appMetadata = claims.get("app_metadata", Map.class);
        if (appMetadata != null && appMetadata.containsKey("role")) {
            return appMetadata.get("role").toString();
        }

        String role = claims.get("role", String.class);
        if (role != null) {
            return role;
        }

        return "authenticated";
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        logger.debug("Authentication error: {}", error);
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
    }
}
