package com.webaudit.config;

import com.webaudit.config.properties.CorsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> rawOrigins = corsProperties.getAllowedOrigins();
        List<String> origins = new ArrayList<>();

        if (rawOrigins != null) {
            for (String raw : rawOrigins) {
                if (raw != null) {
                    for (String s : raw.split(",")) {
                        String trimmed = s.trim();
                        if (!trimmed.isEmpty()) {
                            origins.add(trimmed);
                        }
                    }
                }
            }
        }

        log.info("Configuring global CorsFilter with allowed origins: {}", origins);

        for (String origin : origins) {
            if ("*".equals(origin)) {
                config.addAllowedOriginPattern("*");
            } else {
                config.addAllowedOrigin(origin);
                config.addAllowedOriginPattern(origin);
            }
        }

        for (String method : corsProperties.getAllowedMethods()) {
            config.addAllowedMethod(method);
        }

        for (String header : corsProperties.getAllowedHeaders()) {
            config.addAllowedHeader(header);
        }

        for (String header : corsProperties.getExposedHeaders()) {
            config.addExposedHeader(header);
        }

        config.setAllowCredentials(corsProperties.isAllowCredentials());
        config.setMaxAge(corsProperties.getMaxAgeSeconds());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Configuring global CORS mappings for allowed origins: {}", corsProperties.getAllowedOrigins());

        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods(corsProperties.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders(corsProperties.getAllowedHeaders().toArray(new String[0]))
                .exposedHeaders(corsProperties.getExposedHeaders().toArray(new String[0]))
                .allowCredentials(corsProperties.isAllowCredentials())
                .maxAge(corsProperties.getMaxAgeSeconds());
    }
}
