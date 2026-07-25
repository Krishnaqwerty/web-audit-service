package com.webaudit.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "audit.logging")
public class LoggingProperties {
    private boolean includeQueryParams = true;
    private boolean includeHeaders = false;
    private int maxPayloadLength = 1000;
}
