package com.webaudit.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "audit.cache")
public class CacheProperties {
    private CaffeineProperties caffeine = new CaffeineProperties();

    @Data
    public static class CaffeineProperties {
        private int initialCapacity = 100;
        private long maximumSize = 10000;
        private long expireAfterWriteMinutes = 15;
    }
}
