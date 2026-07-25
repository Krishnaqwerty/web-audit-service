package com.webaudit.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResilientWebClient {

    private final WebClient webClient;

    public Mono<String> fetchUrlContent(String url, int timeoutMs) {
        log.debug("Executing non-blocking GET fetch to target URL: {}", url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeoutMs));
    }
}
