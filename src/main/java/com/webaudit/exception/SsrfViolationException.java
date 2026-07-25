package com.webaudit.exception;

import com.webaudit.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

public class SsrfViolationException extends BaseException {
    public SsrfViolationException(String host) {
        super(String.format("SSRF Violation: Target host '%s' resolves to a restricted local or private IP address", host), 
              HttpStatus.BAD_REQUEST, 
              ErrorConstants.ERR_SSRF_VIOLATION);
    }
}
