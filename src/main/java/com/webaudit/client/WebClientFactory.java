package com.webaudit.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class WebClientFactory {

    private final WebClient webClient;

    public WebClient getWebClient() {
        return webClient;
    }
}
