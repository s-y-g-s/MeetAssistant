package com.treemoon.meetassistant.cleanup.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final AnHengConfig anHengConfig;

    @Bean
    public WebClient AnHengWebClient(WebClient.Builder builder){

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000)
                .responseTimeout(Duration.ofSeconds(90));

        return builder
                .baseUrl("https://www.das-ai.com/open/api/v2/agent")
                .defaultHeader("appKey", anHengConfig.getAppKey())
                .defaultHeader("sign", anHengConfig.getSign())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((request, next) -> {
                    return next.exchange(request)
                            .retry(3); // 重试 3 次
                })
                .build();

    }

}
