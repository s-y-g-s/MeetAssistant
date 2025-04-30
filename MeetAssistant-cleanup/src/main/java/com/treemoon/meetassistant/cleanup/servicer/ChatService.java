package com.treemoon.meetassistant.cleanup.servicer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.treemoon.meetassistant.cleanup.dto.AnHengRequest;
import com.treemoon.meetassistant.cleanup.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final SseService sseService;
    private final AgentService agentService;
    private final ChatResponseService chatResponseService;

    public SseEmitter Chat(ChatRequest chatRequest) {

        AnHengRequest request = new AnHengRequest();
        request.setSid(UUID.randomUUID().toString());
        request.setId("a64a474a-d669-475a-bfac-06940fada3b4");

        ObjectMapper objectMapper = new ObjectMapper();
        try {

            String jsonString = objectMapper.writeValueAsString(chatRequest);
            request.setInput(jsonString);
            log.info("chatRequest：" + jsonString);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String taskKey = "Chat-" + request.getSid();
        SseEmitter emitter = sseService.createEmitter(taskKey);

        int maxRetries = 3;
        final AtomicReference<String> currentStage = new AtomicReference<>(null);
        agentService.executeStream(request)
                .doOnSubscribe(sub -> log.info("开始调用taskKey为" + taskKey + "的会话"))
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(100))

                                //错误过滤，不懂
//                        .filter(error -> error instanceof TimeoutException
//                                || isRetryableError(error) || isNetworkError(error)
//                        )

                        .doBeforeRetry(retrySignal->
                                log.warn("@{} 重试中 [第 {} 次], 触发重试的异常信息: {}",
                                                request.getSid(),
                                                retrySignal.totalRetries() + 1,
                                                retrySignal.failure().getMessage()))
                        .onRetryExhaustedThrow((spec, signal) ->
                                new RuntimeException(
                                        "请求失败，超过最大重试次数 " + maxRetries, signal.failure()
                                )
                        )
                )
                .subscribe(
                        result -> currentStage.set(
                                chatResponseService
                                        .ResponseHandler(result,taskKey, currentStage.get()
                                )
                        ),
                        error -> sseService.handleEmitterError(error,emitter,taskKey),
                        () -> sseService.completeEmitter(emitter)
                );

        return emitter;

    }
}
