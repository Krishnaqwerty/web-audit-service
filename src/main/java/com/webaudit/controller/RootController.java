package com.webaudit.controller;

import com.webaudit.config.properties.ApplicationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Service Information", description = "Root service health and metadata info")
public class RootController {

    private final ApplicationProperties applicationProperties;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Operation(summary = "Root service info", description = "Returns core metadata, operational status, and links to Swagger documentation and health actuator.")
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("service", applicationProperties.getName());
        info.put("version", applicationProperties.getVersion());
        info.put("status", "UP");
        info.put("profile", activeProfile);
        info.put("documentation", "/swagger-ui/index.html");
        info.put("health", "/actuator/health");
        return ResponseEntity.ok(info);
    }
}
