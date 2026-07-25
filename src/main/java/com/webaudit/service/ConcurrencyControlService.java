package com.webaudit.service;

public interface ConcurrencyControlService {
    void acquirePermitOrThrow();
    void releasePermit();
}
