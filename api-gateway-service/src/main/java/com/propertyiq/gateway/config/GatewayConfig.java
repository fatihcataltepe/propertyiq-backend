package com.propertyiq.gateway.config;

import com.propertyiq.gateway.filter.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public GatewayConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes - public endpoints (no JWT required)
                .route("auth-service-public", r -> r
                        .path("/api/auth/signup", "/api/auth/login", "/api/auth/refresh")
                        .uri("http://localhost:8081"))
                
                // Auth Service Routes - protected endpoints (JWT required)
                .route("auth-service-protected", r -> r
                        .path("/api/auth/**", "/api/users/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8081"))
                
                // Portfolio Service Routes (JWT required)
                .route("portfolio-service", r -> r
                        .path("/api/properties/**", "/api/portfolio/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8082"))
                
                // Expense Service Routes (JWT required)
                .route("expense-service", r -> r
                        .path("/api/expenses/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8083"))
                
                // Analytics Service Routes (JWT required)
                .route("analytics-service", r -> r
                        .path("/api/analytics/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8084"))
                
                // Reporting Service Routes (JWT required)
                .route("reporting-service", r -> r
                        .path("/api/reports/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8085"))
                
                // Notification Service Routes (JWT required)
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8086"))
                
                .build();
    }
}
