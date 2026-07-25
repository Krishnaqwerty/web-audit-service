package com.webaudit.config;

import com.webaudit.config.properties.RateLimiterProperties;
import io.github.bucket4j.Bandwidth;

import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RateLimiterConfig {

    private final RateLimiterProperties properties;
    private final Map<String, Bucket> bucketMap = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return bucketMap.computeIfAbsent(key, this::createNewBucket);
    }

    private Bucket createNewBucket(String key) {
        log.debug("Creating new rate limit bucket for key '{}' with capacity {}", key, properties.getCapacity());
        Bandwidth limit = Bandwidth.builder()
                .capacity(properties.getCapacity())
                .refillGreedy(properties.getRefillTokens(), Duration.ofSeconds(properties.getRefillDurationSeconds()))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
