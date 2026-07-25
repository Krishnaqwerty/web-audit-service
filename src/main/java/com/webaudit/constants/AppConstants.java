package com.webaudit.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AppConstants {
    public static final String API_V1_BASE = "/api/v1";
    public static final String AUDITS_ENDPOINT = API_V1_BASE + "/audits";
    
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String MDC_REQUEST_ID_KEY = "requestId";
    
    public static final String CACHE_NAME_AUDITS = "auditsCache";
}
