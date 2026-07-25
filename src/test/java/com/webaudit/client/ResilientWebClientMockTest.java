package com.webaudit.client;

import com.webaudit.service.PageParserService;
import com.webaudit.service.impl.PageParserServiceImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ResilientWebClientMockTest {

    private MockWebServer mockWebServer;
    private WebClient webClient;
    private PageParserService pageParserService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();
        pageParserService = new PageParserServiceImpl();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Should successfully fetch HTML body and extract title from MockWebServer")
    void fetch_shouldReturnHtmlAndExtractTitle() {
        String htmlPayload = "<html><head><title>Mocked Test Website</title></head><body><h1>Hello World</h1></body></html>";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/html; charset=UTF-8")
                .setBody(htmlPayload));

        StepVerifier.create(webClient.get().retrieve().bodyToMono(String.class))
                .assertNext(body -> {
                    assertThat(body).contains("Mocked Test Website");
                    String title = pageParserService.extractTitle(body);
                    assertThat(title).isEqualTo("Mocked Test Website");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle 500 Internal Server Error from target server")
    void fetch_shouldHandle500ServerError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        StepVerifier.create(webClient.get().exchangeToMono(response -> response.toEntity(String.class)))
                .assertNext(entity -> assertThat(entity.getStatusCode().value()).isEqualTo(500))
                .verifyComplete();
    }
}
