package com.webaudit.service;

import com.webaudit.exception.InvalidUrlException;
import com.webaudit.exception.SsrfViolationException;
import com.webaudit.service.impl.UrlValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlValidationServiceTest {

    private UrlValidationService urlValidationService;

    @BeforeEach
    void setUp() {
        urlValidationService = new UrlValidationServiceImpl();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.com",
            "https://example.com/path?query=val",
            "https://sub.domain.org:8080/page#section"
    })
    @DisplayName("Valid public HTTP/HTTPS URLs should pass validation")
    void validUrls_shouldPassValidation(String url) {
        assertThat(urlValidationService.isValid(url)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ftp://files.example.com",
            "file:///etc/passwd",
            "javascript:alert(1)",
            "mailto:user@example.com",
            "gopher://localhost:70"
    })
    @DisplayName("Non-HTTP schemes should be rejected with InvalidUrlException")
    void invalidSchemes_shouldThrowInvalidUrlException(String url) {
        assertThatThrownBy(() -> urlValidationService.validateUrlOrThrow(url))
                .isInstanceOf(InvalidUrlException.class)
                .hasMessageContaining("Unsupported scheme");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost",
            "http://localhost:8080",
            "http://127.0.0.1",
            "http://127.0.0.1:9000",
            "http://10.0.0.1",
            "http://172.16.0.1",
            "http://192.168.1.1",
            "http://169.254.169.254/latest/meta-data/"
    })
    @DisplayName("Localhost and private/link-local IP targets should be rejected with SsrfViolationException")
    void privateAndLoopbackTargets_shouldThrowSsrfViolationException(String url) {
        assertThatThrownBy(() -> urlValidationService.validateUrlOrThrow(url))
                .isInstanceOf(SsrfViolationException.class)
                .hasMessageContaining("SSRF Violation");
    }

    @Test
    @DisplayName("Blank or null URL should throw InvalidUrlException")
    void nullOrBlankUrl_shouldThrowInvalidUrlException() {
        assertThatThrownBy(() -> urlValidationService.validateUrlOrThrow(null))
                .isInstanceOf(InvalidUrlException.class);
        assertThatThrownBy(() -> urlValidationService.validateUrlOrThrow("   "))
                .isInstanceOf(InvalidUrlException.class);
    }
}
