package com.webaudit.service.impl;

import com.webaudit.config.RateLimiterConfig;
import com.webaudit.config.properties.RateLimiterProperties;
import com.webaudit.exception.RateLimitExceededException;
import com.webaudit.service.RateLimitationService;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitationServiceImpl implements RateLimitationService {

    private final RateLimiterConfig rateLimiterConfig;
    private final RateLimiterProperties rateLimiterProperties;
    private final com.webaudit.service.MetricsService metricsService;

    @Override
    public void checkRateLimitOrThrow(String clientIp) {
        if (!rateLimiterProperties.isEnabled()) {
            return;
        }

        String ip = (clientIp == null || clientIp.trim().isEmpty()) ? "unknown-ip" : clientIp;
        Bucket bucket = rateLimiterConfig.resolveBucket(ip);

        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for client IP: {}", ip);
            metricsService.recordRateLimitViolation();
            throw new RateLimitExceededException(ip);
        }
    }
}
