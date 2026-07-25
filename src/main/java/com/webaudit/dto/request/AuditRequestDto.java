package com.webaudit.dto.request;

import com.webaudit.validation.ValidUrl;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@io.swagger.v3.oas.annotations.media.Schema(description = "Payload for submitting a URL audit request")
public class AuditRequestDto {

    @NotBlank(message = "URL must not be blank")
    @ValidUrl
    @io.swagger.v3.oas.annotations.media.Schema(description = "Target HTTP or HTTPS URL to audit", example = "https://example.com")
    private String url;

    @io.swagger.v3.oas.annotations.media.Schema(description = "If true, bypasses Caffeine cache and forces a fresh network audit", example = "false")
    private boolean forceRefresh;
}
