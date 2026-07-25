package com.webaudit.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "audit.http-client")
public class HttpClientProperties {
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 5000;
    private int writeTimeoutMs = 5000;
    private int maxConnections = 100;
    private int maxConnectionsPerRoute = 20;
    private int connectionIdleTimeoutSec = 30;
    private String userAgent = "WebAuditService/1.0 (+https://webaudit.internal)";
    private boolean followRedirects = true;
    private int maxRedirects = 3;
    private long maxBodySizeBytes = 2097152; // 2 MB
}
