package com.treemoon.MeetAssist.client;

import com.treemoon.MeetAssist.dto.AnHengRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

// 文件路径：src/main/java/com/example/client/LLMClient.java
@Component
@RequiredArgsConstructor
public class AnHengClient {
    private final WebClient llmWebClient;

    // 同步调用
    public String executeSync(AnHengRequest request) {
        return llmWebClient.post()
                .uri("/execute") // 基础路径已在配置中设置
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // 流式调用
    public Flux<String> executeStream(AnHengRequest request) {
        return llmWebClient.post()
                .uri("/execute")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class)
                .retry(3);
    }

    // 返回 Mono 的异步方法
    public Mono<String> executeAsync(AnHengRequest request) {

        Duration timeoutDuration = Duration.ofSeconds(900);

        return llmWebClient.post()
                .uri("/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(timeoutDuration);
    }
}
