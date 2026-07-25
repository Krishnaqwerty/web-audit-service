package com.webaudit.logging;

import com.webaudit.constants.AppConstants;
import com.webaudit.constants.HeaderConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("Should generate UUID when no correlation ID header is present in request")
    void filter_shouldGenerateUuid_whenNoHeaderPresent() throws ServletException, IOException {
        filter.doFilter(request, response, filterChain);

        String responseHeader = response.getHeader(HeaderConstants.X_REQUEST_ID);
        assertThat(responseHeader).isNotNull().isNotBlank();

        verify(filterChain).doFilter(request, response);
        // MDC must be cleared after filter completion
        assertThat(MDC.get(AppConstants.MDC_REQUEST_ID_KEY)).isNull();
    }

    @Test
    @DisplayName("Should reuse incoming X-Request-ID header when present")
    void filter_shouldReuseExistingHeader_whenHeaderPresent() throws ServletException, IOException {
        String existingRequestId = "client-req-12345";
        request.addHeader(HeaderConstants.X_REQUEST_ID, existingRequestId);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader(HeaderConstants.X_REQUEST_ID)).isEqualTo(existingRequestId);
        assertThat(response.getHeader(HeaderConstants.X_CORRELATION_ID)).isEqualTo(existingRequestId);

        verify(filterChain).doFilter(request, response);
        assertThat(MDC.get(AppConstants.MDC_REQUEST_ID_KEY)).isNull();
    }
}
