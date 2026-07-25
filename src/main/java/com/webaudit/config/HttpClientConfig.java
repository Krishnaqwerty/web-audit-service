package com.webaudit.config;

import com.webaudit.config.properties.HttpClientProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HttpClientConfig {

    private final HttpClientProperties properties;

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        log.info("Configuring WebClient with connectTimeout={}ms, readTimeout={}ms, maxConnections={}",
                properties.getConnectTimeoutMs(), properties.getReadTimeoutMs(), properties.getMaxConnections());

        ConnectionProvider provider = ConnectionProvider.builder("webaudit-http-pool")
                .maxConnections(properties.getMaxConnections())
                .pendingAcquireMaxCount(properties.getMaxConnections() * 2)
                .maxIdleTime(Duration.ofSeconds(properties.getConnectionIdleTimeoutSec()))
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeoutMs())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(properties.getReadTimeoutMs(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(properties.getWriteTimeoutMs(), TimeUnit.MILLISECONDS)))
                .compress(true)
                .followRedirect(properties.isFollowRedirects());

        return webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.USER_AGENT, properties.getUserAgent())
                .build();
    }
}
