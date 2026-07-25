package com.webaudit.controller;

import com.webaudit.constants.AppConstants;
import com.webaudit.dto.request.AuditRequestDto;
import com.webaudit.dto.response.AuditResponseDto;
import com.webaudit.dto.response.ErrorResponseDto;
import com.webaudit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(AppConstants.AUDITS_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "URL Audit API", description = "Endpoints for submitting and retrieving website audits")
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "Submit a URL for auditing", description = "Fetches the target website, extracts page title, status, metadata, response time, and caches the result.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Audit successfully completed or retrieved from cache",
                    content = @Content(schema = @Schema(implementation = AuditResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid URL syntax, unsupported scheme, or SSRF policy violation",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded (returns Retry-After header)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "503", description = "Outbound concurrency limit reached",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping
    public ResponseEntity<AuditResponseDto> submitAudit(
            @Valid @RequestBody AuditRequestDto requestDto,
            HttpServletRequest request
    ) {
        String clientIp = resolveClientIp(request);
        log.info("Received REST API request to audit URL: {}", requestDto.getUrl());
        AuditResponseDto response = auditService.processAuditRequest(requestDto, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get audit report by ID", description = "Retrieves an existing completed audit report by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit report found",
                    content = @Content(schema = @Schema(implementation = AuditResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Audit report not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AuditResponseDto> getAuditById(@PathVariable String id) {
        log.info("Received REST API request for audit report ID: {}", id);
        AuditResponseDto response = auditService.getAuditById(id);
        return ResponseEntity.ok(response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
