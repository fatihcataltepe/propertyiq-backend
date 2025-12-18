package com.propertyiq.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.propertyiq.portfolio", "com.propertyiq.common"})
@EnableJpaRepositories("com.propertyiq.portfolio.repository")
@EntityScan("com.propertyiq.portfolio.model")
public class PortfolioServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortfolioServiceApplication.class, args);
    }
}
