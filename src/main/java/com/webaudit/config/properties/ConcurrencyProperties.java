package com.webaudit.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "audit.concurrency")
public class ConcurrencyProperties {
    private int maxConcurrentAudits = 50;
    private long acquireTimeoutMs = 1000;
}
