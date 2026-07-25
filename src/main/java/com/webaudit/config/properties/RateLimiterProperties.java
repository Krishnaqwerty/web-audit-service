package com.webaudit.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "audit.rate-limiter")
public class RateLimiterProperties {
    private boolean enabled = true;
    private long capacity = 60;
    private long refillTokens = 60;
    private long refillDurationSeconds = 60;
}
