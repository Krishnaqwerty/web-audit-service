package com.webaudit.service.impl;

import com.webaudit.config.properties.HttpClientProperties;
import com.webaudit.config.properties.TimeoutProperties;
import com.webaudit.constants.AppConstants;
import com.webaudit.dto.request.AuditRequestDto;
import com.webaudit.dto.response.AuditResponseDto;
import com.webaudit.exception.ResourceNotFoundException;
import com.webaudit.exception.TargetConnectionException;
import com.webaudit.exception.TargetTimeoutException;
import com.webaudit.service.AuditService;
import com.webaudit.service.ConcurrencyControlService;
import com.webaudit.service.PageParserService;
import com.webaudit.service.RateLimitationService;
import com.webaudit.service.UrlValidationService;
import com.webaudit.util.UrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final UrlValidationService urlValidationService;
    private final RateLimitationService rateLimitationService;
    private final ConcurrencyControlService concurrencyControlService;
    private final WebClient webClient;
    private final PageParserService pageParserService;
    private final CacheManager cacheManager;
    private final TimeoutProperties timeoutProperties;
    private final HttpClientProperties httpClientProperties;
    private final com.webaudit.service.MetricsService metricsService;

    // Fallback storage for GET /api/v1/audits/{id}
    private final Map<String, AuditResponseDto> auditStore = new ConcurrentHashMap<>();

    @Override
    public AuditResponseDto processAuditRequest(AuditRequestDto requestDto, String clientIp) {
        String targetUrl = requestDto.getUrl();
        log.info("Processing audit request for URL: {} from IP: {}", targetUrl, clientIp);

        metricsService.incrementActiveRequests();

        try {
            // 1. Rate Limit Enforcement
            rateLimitationService.checkRateLimitOrThrow(clientIp);

            // 2. Strict URL & SSRF Validation
            urlValidationService.validateUrlOrThrow(targetUrl);

            // 3. Cache Lookup by Normalized URL
            String normalizedUrl = UrlUtils.normalizeUrl(targetUrl);
            Cache cache = cacheManager.getCache(AppConstants.CACHE_NAME_AUDITS);

            if (!requestDto.isForceRefresh() && cache != null) {
                AuditResponseDto cachedResponse = cache.get(normalizedUrl, AuditResponseDto.class);
                if (cachedResponse != null) {
                    log.info("Cache HIT for normalized URL: {} | duration=0ms | cached=true", normalizedUrl);
                    metricsService.recordCacheHit();
                    metricsService.recordHttpStatus(cachedResponse.getHttpStatusCode());
                    return cloneResponseWithCachedFlag(cachedResponse, true);
                }
            }

            log.info("Cache MISS for normalized URL: {} | Executing outbound fetch", normalizedUrl);
            metricsService.recordCacheMiss();

            // 4. Outbound Concurrency Permit Acquisition
            concurrencyControlService.acquirePermitOrThrow();
            long startTimeMs = System.currentTimeMillis();

            try {
                // 5. Execute HTTP Fetch via WebClient
                ResponseEntity<String> responseEntity = webClient.get()
                        .uri(targetUrl)
                        .accept(MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML, MediaType.ALL)
                        .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                        .timeout(Duration.ofMillis(timeoutProperties.getDefaultAuditMs()))
                        .onErrorMap(java.util.concurrent.TimeoutException.class, e -> {
                            metricsService.recordTimeoutFailure();
                            return new TargetTimeoutException(targetUrl, timeoutProperties.getDefaultAuditMs());
                        })
                        .onErrorMap(WebClientRequestException.class, e -> {
                            metricsService.recordExternalFailure();
                            return new TargetConnectionException(targetUrl, e.getMessage());
                        })
                        .onErrorMap(org.springframework.core.io.buffer.DataBufferLimitException.class, e -> {
                            metricsService.recordExternalFailure();
                            return new TargetConnectionException(targetUrl, "Response body size exceeded buffer limit of " + httpClientProperties.getMaxBodySizeBytes() + " bytes");
                        })
                        .block();

                long durationMs = System.currentTimeMillis() - startTimeMs;
                metricsService.recordRequestLatency(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

                int statusCode = (responseEntity != null) ? responseEntity.getStatusCode().value() : 500;
                metricsService.recordHttpStatus(statusCode);

                HttpHeaders headers = (responseEntity != null) ? responseEntity.getHeaders() : new HttpHeaders();
                String htmlBody = (responseEntity != null) ? responseEntity.getBody() : "";

                String pageTitle = pageParserService.extractTitle(htmlBody);
                String contentType = (headers.getContentType() != null) ? headers.getContentType().toString() : null;
                long contentLength = headers.getContentLength() >= 0 ? headers.getContentLength() : (htmlBody != null ? htmlBody.getBytes().length : 0);

                String auditId = "aud_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                Instant now = Instant.now();

                AuditResponseDto auditResponse = AuditResponseDto.builder()
                        .id(auditId)
                        .url(targetUrl)
                        .status("COMPLETED")
                        .httpStatusCode(statusCode)
                        .pageTitle(pageTitle)
                        .responseTimeMs(durationMs)
                        .finalRedirectedUrl(targetUrl)
                        .contentType(contentType)
                        .contentLengthBytes(contentLength)
                        .redirectCount(0)
                        .cached(false)
                        .createdAt(now)
                        .completedAt(now)
                        .build();

                // Store in Caffeine cache and local audit store
                if (cache != null) {
                    cache.put(normalizedUrl, auditResponse);
                }
                auditStore.put(auditId, auditResponse);

                log.info("Audit COMPLETED for URL: {} | Status: {} | Duration: {}ms | Title: '{}' | cached=false",
                        targetUrl, statusCode, durationMs, pageTitle);

                return auditResponse;

            } finally {
                concurrencyControlService.releasePermit();
            }
        } finally {
            metricsService.decrementActiveRequests();
        }
    }

    @Override
    public AuditResponseDto getAuditById(String id) {
        log.info("Fetching audit report by ID: {}", id);
        AuditResponseDto report = auditStore.get(id);
        if (report == null) {
            throw new ResourceNotFoundException("AuditReport", id);
        }
        return report;
    }

    private AuditResponseDto cloneResponseWithCachedFlag(AuditResponseDto original, boolean cached) {
        return AuditResponseDto.builder()
                .id(original.getId())
                .url(original.getUrl())
                .status(original.getStatus())
                .httpStatusCode(original.getHttpStatusCode())
                .pageTitle(original.getPageTitle())
                .responseTimeMs(original.getResponseTimeMs())
                .finalRedirectedUrl(original.getFinalRedirectedUrl())
                .contentType(original.getContentType())
                .contentLengthBytes(original.getContentLengthBytes())
                .redirectCount(original.getRedirectCount())
                .message(original.getMessage())
                .cached(cached)
                .createdAt(original.getCreatedAt())
                .completedAt(original.getCompletedAt())
                .build();
    }
}
