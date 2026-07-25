package com.webaudit.logging;

import com.webaudit.constants.AppConstants;
import com.webaudit.constants.HeaderConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = extractOrGenerateRequestId(request);

        MDC.put(AppConstants.MDC_REQUEST_ID_KEY, requestId);
        response.setHeader(HeaderConstants.X_REQUEST_ID, requestId);
        response.setHeader(HeaderConstants.X_CORRELATION_ID, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(AppConstants.MDC_REQUEST_ID_KEY);
        }
    }

    private String extractOrGenerateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(HeaderConstants.X_REQUEST_ID);
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = request.getHeader(HeaderConstants.X_CORRELATION_ID);
        }
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        return requestId;
    }
}
