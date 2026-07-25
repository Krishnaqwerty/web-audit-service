package com.webaudit.service;

public interface UrlValidationService {
    void validateUrlOrThrow(String url);
    boolean isValid(String url);
}
