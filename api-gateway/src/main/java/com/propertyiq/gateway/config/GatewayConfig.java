package com.propertyiq.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes
                .route("auth-service", r -> r
                        .path("/api/auth/**", "/api/users/**")
                        .uri("http://localhost:8081"))
                
                // Portfolio Service Routes
                .route("portfolio-service", r -> r
                        .path("/api/properties/**", "/api/portfolio/**")
                        .uri("http://localhost:8082"))
                
                // Expense Service Routes
                .route("expense-service", r -> r
                        .path("/api/expenses/**")
                        .uri("http://localhost:8083"))
                
                // Analytics Service Routes
                .route("analytics-service", r -> r
                        .path("/api/analytics/**")
                        .uri("http://localhost:8084"))
                
                // Reporting Service Routes
                .route("reporting-service", r -> r
                        .path("/api/reports/**")
                        .uri("http://localhost:8085"))
                
                // Notification Service Routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("http://localhost:8086"))
                
                .build();
    }
}
