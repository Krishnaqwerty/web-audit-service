package com.webaudit.exception;

import com.webaudit.dto.response.ErrorResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/audits");
    }

    @Test
    @DisplayName("Should handle BaseException and return standard ErrorResponseDto")
    void handleBaseException_shouldReturnFormattedErrorDto() {
        InvalidUrlException ex = new InvalidUrlException("ftp://invalid", "Unsupported scheme");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleBaseException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("INVALID_URL_SCHEME_OR_SYNTAX");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/audits");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should handle generic exception without exposing stack trace")
    void handleGenericException_shouldReturnInternalServerErrorWithoutStackTrace() {
        RuntimeException ex = new RuntimeException("Sensitive database connection password failure");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).doesNotContain("password");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected internal error occurred. Please try again later.");
    }
}
