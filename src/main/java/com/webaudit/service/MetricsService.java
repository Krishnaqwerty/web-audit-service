package com.webaudit.service;

import java.util.concurrent.TimeUnit;

public interface MetricsService {
    void recordRequestLatency(long duration, TimeUnit timeUnit);
    void recordCacheHit();
    void recordCacheMiss();
    void recordTimeoutFailure();
    void recordExternalFailure();
    void recordHttpStatus(int statusCode);
    void recordRateLimitViolation();
    void incrementActiveRequests();
    void decrementActiveRequests();
}
