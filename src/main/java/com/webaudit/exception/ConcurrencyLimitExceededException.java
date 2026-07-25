package com.webaudit.exception;

import com.webaudit.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

public class ConcurrencyLimitExceededException extends BaseException {
    public ConcurrencyLimitExceededException(int maxConcurrent) {
        super(String.format("Service outbound audit concurrency limit reached (%d max concurrent audits). Please retry shortly.", maxConcurrent),
              HttpStatus.SERVICE_UNAVAILABLE,
              ErrorConstants.ERR_RATE_LIMIT_EXCEEDED);
    }
}
