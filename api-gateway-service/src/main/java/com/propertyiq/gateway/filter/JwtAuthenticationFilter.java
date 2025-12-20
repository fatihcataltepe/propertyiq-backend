package com.propertyiq.gateway.filter;

import com.propertyiq.gateway.security.JwksKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwksKeyProvider jwksKeyProvider;

    public JwtAuthenticationFilter(JwksKeyProvider jwksKeyProvider) {
        super(Config.class);
        this.jwksKeyProvider = jwksKeyProvider;
        logger.info("JwtAuthenticationFilter initialized with JWKS-based validation");
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
                String kid = extractKidFromToken(token);
                if (kid == null) {
                    logger.warn("JWT token does not contain kid in header");
                    return onError(exchange, "Invalid JWT token: missing key ID", HttpStatus.UNAUTHORIZED);
                }

                return jwksKeyProvider.getKey(kid)
                        .flatMap(publicKey -> validateTokenAndContinue(exchange, chain, token, publicKey))
                        .onErrorResume(e -> {
                            logger.warn("JWT validation failed: {}", e.getMessage());
                            return onError(exchange, "Invalid JWT token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                        });
            } catch (Exception e) {
                logger.warn("JWT validation failed: {}", e.getMessage());
                return onError(exchange, "Invalid JWT token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private String extractKidFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            int kidIndex = headerJson.indexOf("\"kid\"");
            if (kidIndex == -1) {
                return null;
            }
            int colonIndex = headerJson.indexOf(":", kidIndex);
            int startQuote = headerJson.indexOf("\"", colonIndex + 1);
            int endQuote = headerJson.indexOf("\"", startQuote + 1);
            return headerJson.substring(startQuote + 1, endQuote);
        } catch (Exception e) {
            logger.warn("Failed to extract kid from token: {}", e.getMessage());
            return null;
        }
    }

    private Mono<Void> validateTokenAndContinue(ServerWebExchange exchange, 
                                                 GatewayFilterChain chain,
                                                 String token, 
                                                 PublicKey publicKey) {
        return Mono.fromCallable(() -> {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            String role = extractRole(claims);

            logger.debug("JWT validated for user: {}, email: {}, role: {}", userId, email, role);

            return new UserInfo(userId, email, role);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(userInfo -> {
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(r -> r.header("X-User-Id", userInfo.userId())
                                  .header("X-User-Email", userInfo.email() != null ? userInfo.email() : "")
                                  .header("X-User-Roles", userInfo.role() != null ? userInfo.role() : "authenticated"))
                    .build();
            return chain.filter(modifiedExchange);
        });
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

    private record UserInfo(String userId, String email, String role) {}

    public static class Config {
    }
}
