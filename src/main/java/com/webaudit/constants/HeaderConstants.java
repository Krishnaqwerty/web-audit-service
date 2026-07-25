package com.webaudit.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class HeaderConstants {
    public static final String X_REQUEST_ID = "X-Request-ID";
    public static final String X_CORRELATION_ID = "X-Correlation-ID";
    public static final String X_RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
    public static final String X_RATE_LIMIT_RETRY_AFTER_SECONDS = "X-RateLimit-Retry-After-Seconds";
}
