package com.webaudit.exception;

import com.webaudit.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends BaseException {
    public RateLimitExceededException(String clientIp) {
        super(String.format("Rate limit exceeded for IP address '%s'. Please wait before sending more requests.", clientIp),
              HttpStatus.TOO_MANY_REQUESTS,
              ErrorConstants.ERR_RATE_LIMIT_EXCEEDED);
    }
}
