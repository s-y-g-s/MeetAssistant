package com.treemoon.meetassistant.cleanup.servicer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.treemoon.meetassistant.cleanup.dto.AnHengRequest;
import com.treemoon.meetassistant.cleanup.dto.SsePayload;
import com.treemoon.meetassistant.cleanup.mapper.EnhanceTransMapper;
import com.treemoon.meetassistant.cleanup.mapper.TranscriptionMapper;
import com.treemoon.meetassistant.cleanup.pojo.Transcription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@Slf4j
@RequiredArgsConstructor
public class EnhanceTransService {

    private final AgentService agentService;
    private final TranscriptionMapper transcriptionMapper;
    private final EnhanceTransMapper enhanceTransMapper;
    private final SseService sseService;

    // 类成员变量，用于生成唯一序号
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    public void enhanceTrans(String taskKey,String textContent){

        // 构建请求参数
        AnHengRequest request = new AnHengRequest();
        request.setSid(taskKey.substring(6)); // 唯一会话ID
        request.setId("a15aae3e-0c04-43ee-a3c3-3023ea81402f");     // 智能体ID
        request.setInput(textContent);

        // 在调用异步方法的地方
        int currentIndex = requestCounter.getAndIncrement(); // 获取当前调用序号
        int maxRetries = 3; // 最大重试次数（总尝试次数 = 1 + maxRetries）
        agentService.executeStream(request)
                .doOnSubscribe(sub ->
                        System.out.println("开始调用安恒转录增强 [句子序号: " + currentIndex + "]")
                )
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(100)) // 修正重试次数

                        //过滤错误，不懂
//                        .filter(error ->
//                                error instanceof TimeoutException ||
//                                        isRetryableError(error) ||
//                                        isNetworkError(error)
//                        )

                        .doBeforeRetry(retrySignal ->
                                log.info("index为{}的句子增强重试中 [第 {} 次], 错误: {}%n",
                                        currentIndex,
                                        retrySignal.totalRetries() + 1,
                                        retrySignal.failure().getMessage())
                        )
                        .onRetryExhaustedThrow((spec, signal) ->
                                new RuntimeException("请求失败，超过最大重试次数 " + maxRetries, signal.failure())
                        )
                )
                .subscribe(
                        result -> {

                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode jsonNode = null;
                            try {
                                jsonNode = mapper.readTree(result);
                            } catch (JsonProcessingException e) {
                                log.error("JSON 解析失败或处理异常: " + e.getMessage(), e);
                            }
                            JsonNode dataNode = null;
                            if (jsonNode != null) {
                                dataNode = jsonNode.path("data");
                            }

                            if (dataNode != null && !dataNode.has("content")) {

                                Transcription enhanceTranscription = new Transcription();
                                JsonNode jsonContent = dataNode.path("results")
                                        .path("formated_data");

                                enhanceTranscription.setCard(jsonContent.path("knowledge_data").toString());
                                enhanceTranscription.setSentenceIndex(currentIndex);
                                enhanceTranscription.setTextContent(jsonContent.path("text").asText());
                                enhanceTranscription.setStartTime(transcriptionMapper
                                        .selectStartTimeBySentenceIndex(currentIndex)
                                );
                                enhanceTranscription.setTaskKey(taskKey);
                                enhanceTransMapper.addEnhanceTrans(enhanceTranscription);

                                SsePayload enhancedPayload = new SsePayload(
                                        "enhanced",
                                        enhanceTranscription,
                                        System.currentTimeMillis(),
                                        true
                                );
                                sseService.sendTransEmitter(taskKey, enhancedPayload);

                                log.info("转录{}增强发送成功：" + enhanceTranscription, taskKey);
                            }

                        },
                        error -> log.error("转录增强[序号: " + currentIndex + "]失败: " + error.getMessage(),error)
                );

    }

}
