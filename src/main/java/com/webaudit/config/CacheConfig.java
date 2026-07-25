package com.webaudit.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.webaudit.config.properties.CacheProperties;
import com.webaudit.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CacheConfig {

    private final CacheProperties properties;

    @Bean
    public CacheManager cacheManager() {
        log.info("Initializing Caffeine CacheManager with initialCapacity={}, maxSize={}, expireAfterWrite={}min",
                properties.getCaffeine().getInitialCapacity(),
                properties.getCaffeine().getMaximumSize(),
                properties.getCaffeine().getExpireAfterWriteMinutes());

        CaffeineCacheManager cacheManager = new CaffeineCacheManager(AppConstants.CACHE_NAME_AUDITS);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(properties.getCaffeine().getInitialCapacity())
                .maximumSize(properties.getCaffeine().getMaximumSize())
                .expireAfterWrite(properties.getCaffeine().getExpireAfterWriteMinutes(), TimeUnit.MINUTES)
                .recordStats());

        return cacheManager;
    }
}
