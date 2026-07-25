package com.webaudit.exception;

import org.springframework.http.HttpStatus;

public class TooManyRedirectsException extends BaseException {
    public TooManyRedirectsException(String url, int maxRedirects) {
        super(String.format("Target URL '%s' exceeded maximum allowed redirects (%d)", url, maxRedirects),
              HttpStatus.LOOP_DETECTED,
              "TOO_MANY_REDIRECTS");
    }
}
