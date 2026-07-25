package com.webaudit.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "audit.cors")
public class CorsProperties {

    private List<String> allowedOrigins = List.of(
            "http://localhost:3000",
            "https://krishnaqwerty.github.io",
            "https://pulse.krishnakumar.tech"
    );
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    private List<String> allowedHeaders = List.of("*");
    private List<String> exposedHeaders = List.of("Location", "X-Request-ID", "X-Correlation-ID");
    private boolean allowCredentials = false;
    private long maxAgeSeconds = 3600;
}
