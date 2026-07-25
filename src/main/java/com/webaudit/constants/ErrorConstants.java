package com.webaudit.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorConstants {
    public static final String ERR_BAD_REQUEST = "BAD_REQUEST";
    public static final String ERR_INVALID_URL = "INVALID_URL_SCHEME_OR_SYNTAX";
    public static final String ERR_SSRF_VIOLATION = "SSRF_SECURITY_VIOLATION";
    public static final String ERR_RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String ERR_RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String ERR_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
}
