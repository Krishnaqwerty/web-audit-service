package com.webaudit.service;

import com.webaudit.dto.request.AuditRequestDto;
import com.webaudit.dto.response.AuditResponseDto;

public interface AuditService {
    AuditResponseDto processAuditRequest(AuditRequestDto requestDto, String clientIp);
    AuditResponseDto getAuditById(String id);
}
