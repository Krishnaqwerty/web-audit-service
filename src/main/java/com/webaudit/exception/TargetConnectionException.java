package com.webaudit.exception;

import com.webaudit.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

public class TargetConnectionException extends BaseException {
    public TargetConnectionException(String url, String reason) {
        super(String.format("Connection to target URL '%s' failed: %s", url, reason),
              HttpStatus.BAD_GATEWAY,
              "TARGET_CONNECTION_FAILED");
    }
}
