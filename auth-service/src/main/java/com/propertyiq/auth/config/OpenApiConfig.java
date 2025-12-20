package com.propertyiq.auth.config;

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

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .description("Authentication and user management service for PropertyIQ. " +
                                "Handles user registration, login, JWT token issuance, and token refresh.")
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
