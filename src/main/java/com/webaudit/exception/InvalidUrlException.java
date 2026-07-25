package com.webaudit.exception;

import com.webaudit.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

public class InvalidUrlException extends BaseException {
    public InvalidUrlException(String url, String reason) {
        super(String.format("Invalid URL '%s': %s", url, reason), HttpStatus.BAD_REQUEST, ErrorConstants.ERR_INVALID_URL);
    }
}
