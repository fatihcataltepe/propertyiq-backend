package com.propertyiq.notification.config;

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

    @Value("${server.port:8086}")
    private String serverPort;

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("Multi-channel notification delivery service for PropertyIQ. " +
                                "Handles email notifications for property value changes, expense thresholds, " +
                                "and mortgage refinancing opportunities.")
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
