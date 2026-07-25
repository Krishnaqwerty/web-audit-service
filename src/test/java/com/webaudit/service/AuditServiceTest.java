package com.webaudit.service;

import com.webaudit.config.properties.HttpClientProperties;
import com.webaudit.config.properties.TimeoutProperties;
import com.webaudit.dto.request.AuditRequestDto;
import com.webaudit.dto.response.AuditResponseDto;
import com.webaudit.exception.InvalidUrlException;
import com.webaudit.exception.RateLimitExceededException;
import com.webaudit.exception.SsrfViolationException;
import com.webaudit.service.impl.AuditServiceImpl;
import com.webaudit.service.impl.PageParserServiceImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private UrlValidationService urlValidationService;

    @Mock
    private RateLimitationService rateLimitationService;

    @Mock
    private ConcurrencyControlService concurrencyControlService;

    @Mock
    private MetricsService metricsService;

    private MockWebServer mockWebServer;
    private AuditService auditService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();
        PageParserService pageParserService = new PageParserServiceImpl();
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("auditsCache");

        auditService = new AuditServiceImpl(
                urlValidationService,
                rateLimitationService,
                concurrencyControlService,
                webClient,
                pageParserService,
                cacheManager,
                new TimeoutProperties(),
                new HttpClientProperties(),
                metricsService
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Should process audit request successfully, extract title, and cache the result")
    void processAuditRequest_shouldFetchExtractTitleAndCache() {
        String mockHtml = "<html><head><title>Spring Boot Audit Test</title></head><body>Content</body></html>";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/html; charset=UTF-8")
                .setBody(mockHtml));

        String testUrl = mockWebServer.url("/").toString();
        AuditRequestDto requestDto = AuditRequestDto.builder().url(testUrl).build();

        // 1. Initial Request -> Cache Miss
        AuditResponseDto firstResponse = auditService.processAuditRequest(requestDto, "127.0.0.1");

        assertThat(firstResponse).isNotNull();
        assertThat(firstResponse.getStatus()).isEqualTo("COMPLETED");
        assertThat(firstResponse.getHttpStatusCode()).isEqualTo(200);
        assertThat(firstResponse.getPageTitle()).isEqualTo("Spring Boot Audit Test");
        assertThat(firstResponse.isCached()).isFalse();
        verify(concurrencyControlService).acquirePermitOrThrow();
        verify(concurrencyControlService).releasePermit();

        // 2. Second Request -> Cache Hit (cached=true, duration=0, no second webclient fetch)
        AuditResponseDto secondResponse = auditService.processAuditRequest(requestDto, "127.0.0.1");

        assertThat(secondResponse).isNotNull();
        assertThat(secondResponse.isCached()).isTrue();
        assertThat(secondResponse.getPageTitle()).isEqualTo("Spring Boot Audit Test");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1); // Server called only once
    }

    @Test
    @DisplayName("Should throw RateLimitExceededException when rate limit check fails")
    void processAuditRequest_shouldThrowRateLimitExceeded() {
        doThrow(new RateLimitExceededException("192.168.1.100"))
                .when(rateLimitationService).checkRateLimitOrThrow(anyString());

        AuditRequestDto requestDto = AuditRequestDto.builder().url("https://example.com").build();

        assertThatThrownBy(() -> auditService.processAuditRequest(requestDto, "192.168.1.100"))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Rate limit exceeded");
    }

    @Test
    @DisplayName("Should throw SsrfViolationException when URL validation detects internal target")
    void processAuditRequest_shouldThrowSsrfViolation() {
        doThrow(new SsrfViolationException("localhost"))
                .when(urlValidationService).validateUrlOrThrow(anyString());

        AuditRequestDto requestDto = AuditRequestDto.builder().url("http://localhost:8080").build();

        assertThatThrownBy(() -> auditService.processAuditRequest(requestDto, "127.0.0.1"))
                .isInstanceOf(SsrfViolationException.class)
                .hasMessageContaining("SSRF Violation");
    }
}
