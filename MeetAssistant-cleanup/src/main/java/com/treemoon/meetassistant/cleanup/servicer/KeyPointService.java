//package com.treemoon.meetassistant.cleanup.servicer;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.treemoon.meetassistant.cleanup.dto.AnHengRequest;
//import com.treemoon.meetassistant.cleanup.mapper.EnhanceTransMapper;
//import com.treemoon.meetassistant.cleanup.pojo.Meeting;
//import com.treemoon.meetassistant.cleanup.pojo.Transcription;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//import reactor.util.retry.Retry;
//
//import java.time.Duration;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.atomic.AtomicReference;
//
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class KeyPointService {
//
//    private final SseService sseService;
//    private final AgentService agentService;
//    private final EnhanceTransMapper enhanceTransMapper;
//
//    public SseEmitter keyPoint(String taskKey){
//        AnHengRequest request = new AnHengRequest();
//        request.setSid(UUID.randomUUID().toString());
//        request.setId("6c0fef8d-187e-4189-9cbc-7cc8aa92eb21");
//
//        try {
//
//            List<Transcription> enhanceTrans = enhanceTransMapper.getAllByTaskKey(taskKey);
//
//            request.setInput(jsonString);
//            log.info("chatRequest：" + jsonString);
//
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//
//        SseEmitter emitter = sseService.createEmitter(taskKey);
//
//        int maxRetries = 3;
//        final AtomicReference<String> currentStage = new AtomicReference<>(null);
//        agentService.executeStream(request)
//                .doOnSubscribe(sub -> log.info("开始调用taskKey为" + taskKey + "的会话"))
//                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(100))
//
//                                //错误过滤，不懂
////                        .filter(error -> error instanceof TimeoutException
////                                || isRetryableError(error) || isNetworkError(error)
////                        )
//
//                                .doBeforeRetry(retrySignal->
//                                        log.warn("@{} 重试中 [第 {} 次], 触发重试的异常信息: {}",
//                                                request.getSid(),
//                                                retrySignal.totalRetries() + 1,
//                                                retrySignal.failure().getMessage()))
//                                .onRetryExhaustedThrow((spec, signal) ->
//                                        new RuntimeException(
//                                                "请求失败，超过最大重试次数 " + maxRetries, signal.failure()
//                                        )
//                                )
//                )
//                .subscribe(
//                        result -> currentStage.set(
//                                chatResponseService
//                                        .ResponseHandler(result,taskKey, currentStage.get()
//                                        )
//                        ),
//                        error -> sseService.handleEmitterError(error,emitter,taskKey),
//                        () -> sseService.completeEmitter(emitter)
//                );
//
//        return emitter;
//    }
//
//}
