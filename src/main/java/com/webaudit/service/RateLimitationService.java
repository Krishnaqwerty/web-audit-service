package com.webaudit.service;

public interface RateLimitationService {
    void checkRateLimitOrThrow(String clientIp);
}
