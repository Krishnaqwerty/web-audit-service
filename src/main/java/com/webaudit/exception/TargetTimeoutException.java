package com.webaudit.exception;

import com.webaudit.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

public class TargetTimeoutException extends BaseException {
    public TargetTimeoutException(String url, long timeoutMs) {
        super(String.format("Target URL '%s' timed out after %d ms", url, timeoutMs),
              HttpStatus.GATEWAY_TIMEOUT,
              "TARGET_TIMEOUT");
    }
}
