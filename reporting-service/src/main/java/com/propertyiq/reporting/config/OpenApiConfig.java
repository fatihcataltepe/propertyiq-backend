package com.propertyiq.reporting.config;

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

    @Value("${server.port:8085}")
    private String serverPort;

    @Bean
    public OpenAPI reportingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reporting Service API")
                        .description("Report generation service for PropertyIQ. " +
                                "Creates tax summaries, annual financial reports, and aggregated " +
                                "portfolio performance reports for property investors.")
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
