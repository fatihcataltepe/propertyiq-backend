package com.propertyiq.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret:your-secret-key-change-this-in-production}")
    private String jwtSecret;

    private JwtParser jwtParser;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.jwtParser = Jwts.parser()
                .setSigningKey(keyBytes)
                .build();
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
                Claims claims = jwtParser.parseClaimsJws(token).getBody();

                // Add user info to request headers for downstream services
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(r -> r.header("X-User-Id", claims.getSubject())
                                      .header("X-User-Email", claims.get("email", String.class))
                                      .header("X-User-Roles", claims.get("roles", String.class)))
                        .build();

                return chain.filter(modifiedExchange);
            } catch (Exception e) {
                return onError(exchange, "Invalid JWT token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Configuration properties if needed
    }
}
