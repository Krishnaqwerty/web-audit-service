package com.webaudit.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "audit.timeouts")
public class TimeoutProperties {
    private long defaultAuditMs = 10000;
}
