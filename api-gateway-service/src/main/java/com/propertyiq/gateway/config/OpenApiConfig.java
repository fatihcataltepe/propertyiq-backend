package com.propertyiq.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI apiGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PropertyIQ API Gateway")
                        .description("API Gateway for PropertyIQ microservices platform. " +
                                "Routes requests to backend services and handles JWT authentication. " +
                                "For detailed API documentation, access each service's Swagger UI directly: " +
                                "Auth (8081), Portfolio (8082), Expense (8083), Analytics (8084), " +
                                "Reporting (8085), Notification (8086).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PropertyIQ Team")
                                .email("support@propertyiq.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://propertyiq.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")));
    }
}
