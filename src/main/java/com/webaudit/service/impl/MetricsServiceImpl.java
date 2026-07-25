package com.webaudit.service.impl;

import com.webaudit.service.MetricsService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsServiceImpl implements MetricsService {

    private final MeterRegistry meterRegistry;
    private final Timer requestLatencyTimer;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Counter timeoutCounter;
    private final Counter externalFailureCounter;
    private final Counter rateLimitViolationCounter;
    private final AtomicInteger activeRequestsGauge;

    public MetricsServiceImpl(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.requestLatencyTimer = Timer.builder("webaudit.requests.latency")
                .description("Latency of URL audit executions in milliseconds")
                .register(meterRegistry);

        this.cacheHitCounter = Counter.builder("webaudit.cache.hits")
                .description("Total Caffeine cache hits")
                .register(meterRegistry);

        this.cacheMissCounter = Counter.builder("webaudit.cache.misses")
                .description("Total Caffeine cache misses")
                .register(meterRegistry);

        this.timeoutCounter = Counter.builder("webaudit.failures.timeout")
                .description("Total outbound target HTTP fetch timeouts")
                .register(meterRegistry);

        this.externalFailureCounter = Counter.builder("webaudit.failures.external")
                .description("Total outbound target connection or network failures")
                .register(meterRegistry);

        this.rateLimitViolationCounter = Counter.builder("webaudit.ratelimit.violations")
                .description("Total client rate limit violations")
                .register(meterRegistry);

        this.activeRequestsGauge = new AtomicInteger(0);
        Gauge.builder("webaudit.requests.active", activeRequestsGauge, AtomicInteger::get)
                .description("Number of active in-flight audit requests")
                .register(meterRegistry);
    }

    @Override
    public void recordRequestLatency(long duration, TimeUnit timeUnit) {
        requestLatencyTimer.record(duration, timeUnit);
    }

    @Override
    public void recordCacheHit() {
        cacheHitCounter.increment();
    }

    @Override
    public void recordCacheMiss() {
        cacheMissCounter.increment();
    }

    @Override
    public void recordTimeoutFailure() {
        timeoutCounter.increment();
    }

    @Override
    public void recordExternalFailure() {
        externalFailureCounter.increment();
    }

    @Override
    public void recordHttpStatus(int statusCode) {
        Counter.builder("webaudit.requests.status")
                .tag("status", String.valueOf(statusCode))
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordRateLimitViolation() {
        rateLimitViolationCounter.increment();
    }

    @Override
    public void incrementActiveRequests() {
        activeRequestsGauge.incrementAndGet();
    }

    @Override
    public void decrementActiveRequests() {
        activeRequestsGauge.decrementAndGet();
    }
}
