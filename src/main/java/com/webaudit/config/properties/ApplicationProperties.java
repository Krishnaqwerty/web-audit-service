package com.webaudit.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "audit.app")
public class ApplicationProperties {
    private String name = "Web Audit Service";
    private String version = "0.1.0-SNAPSHOT";
}
