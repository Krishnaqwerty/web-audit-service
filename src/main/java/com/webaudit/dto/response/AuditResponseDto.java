package com.webaudit.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String url;
    private String status; // PENDING, COMPLETED, FAILED
    private Integer httpStatusCode;
    private String pageTitle;
    private Long responseTimeMs;
    private String finalRedirectedUrl;
    private String contentType;
    private Long contentLengthBytes;
    private Integer redirectCount;
    private String message;
    private boolean cached;
    private Instant createdAt;
    private Instant completedAt;
}
