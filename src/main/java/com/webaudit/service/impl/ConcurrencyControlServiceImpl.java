package com.webaudit.service.impl;

import com.webaudit.config.properties.ConcurrencyProperties;
import com.webaudit.exception.ConcurrencyLimitExceededException;
import com.webaudit.service.ConcurrencyControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ConcurrencyControlServiceImpl implements ConcurrencyControlService {

    private final Semaphore semaphore;
    private final ConcurrencyProperties properties;

    public ConcurrencyControlServiceImpl(ConcurrencyProperties properties) {
        this.properties = properties;
        this.semaphore = new Semaphore(properties.getMaxConcurrentAudits(), true);
        log.info("Initialized ConcurrencyControlService with maxConcurrentAudits={}", properties.getMaxConcurrentAudits());
    }

    @Override
    public void acquirePermitOrThrow() {
        try {
            boolean acquired = semaphore.tryAcquire(properties.getAcquireTimeoutMs(), TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.warn("Failed to acquire outbound audit concurrency permit within {}ms", properties.getAcquireTimeoutMs());
                throw new ConcurrencyLimitExceededException(properties.getMaxConcurrentAudits());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConcurrencyLimitExceededException(properties.getMaxConcurrentAudits());
        }
    }

    @Override
    public void releasePermit() {
        semaphore.release();
    }
}
