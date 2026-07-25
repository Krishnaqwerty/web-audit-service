package com.webaudit.health;

import com.webaudit.config.properties.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomHealthIndicator implements HealthIndicator {

    private final ApplicationProperties applicationProperties;

    @Override
    public Health health() {
        return Health.up()
                .withDetail("service", applicationProperties.getName())
                .withDetail("version", applicationProperties.getVersion())
                .withDetail("virtualThreadsEnabled", true)
                .withDetail("ssrfProtectionActive", true)
                .build();
    }
}
