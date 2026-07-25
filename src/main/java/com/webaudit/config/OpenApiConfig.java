package com.webaudit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Web Audit Service REST API")
                        .version("0.1.0-SNAPSHOT")
                        .description("Production-grade URL auditing REST API built with Java 21, Spring Boot 3.4+, WebClient, Caffeine Cache, and Bucket4j Rate Limiting.")
                        .contact(new Contact()
                                .name("Engineering Lead")
                                .email("engineering@webaudit.internal"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
