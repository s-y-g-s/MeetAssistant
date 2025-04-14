package com.treemoon.MeetAssist.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.treemoon.MeetAssist.dto.AnHengRequest;
import com.treemoon.MeetAssist.dto.ChatData;
import com.treemoon.MeetAssist.dto.ChatRequest;
import com.treemoon.MeetAssist.dto.SsePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final AgentService agentService;
    private final SseService sseService;

    public SseEmitter ChatGet(ChatRequest chatRequest) {
//        System.out.println("1111111111111\n"+chatRequest);

        SseEmitter emitter = new SseEmitter(60*60*1000L);
        AnHengRequest request = new AnHengRequest();
        request.setSid(UUID.randomUUID().toString());
        request.setId("a64a474a-d669-475a-bfac-06940fada3b4");
//        request.setId("4c5a1179-cc8e-4468-b4f8-bc5edc1307e9");

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(chatRequest);
            request.setInput(jsonString);
            System.out.println("json:" + jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        int maxRetries = 3;
        final AtomicReference<String> currentStage = new AtomicReference<>(null);
        agentService.executeStream(request)
                .doOnSubscribe(sub -> System.out.println("开始调用 [序号: " + request.getSid() + "]"))
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(100))
                        .filter(error -> error instanceof TimeoutException || isRetryableError(error) || isNetworkError(error))
                        .doBeforeRetry(retrySignal -> System.out.printf("@%s 重试中 [第 %d 次], 错误: %s%n",
                                request.getSid(), retrySignal.totalRetries() + 1, retrySignal.failure().getMessage()))
                        .onRetryExhaustedThrow((spec, signal) -> new RuntimeException("请求失败，超过最大重试次数 " + maxRetries, signal.failure())))
                .subscribe(
                        result -> currentStage.set(sendDataToEmitter(result, emitter, currentStage.get())),
                        error -> handleEmitterError(error, emitter , request.getSid()),
                        () -> completeEmitter(emitter, request.getSid())
                );

        return emitter;

    }


    private void completeEmitter(SseEmitter emitter, String Sid) {

        try {
            emitter.send(SseEmitter.event()
                    .name("complete")
                    .data("流式处理结束")
            );
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

    }

    private void handleEmitterError(Throwable error, SseEmitter emitter , String Sid) {

        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(Map.of("message", error.getMessage()))
            );
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private String sendDataToEmitter(String result, SseEmitter emitter, String currentStage) {

//        AtomicBoolean shouldStop = new AtomicBoolean(true);
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode jsonNode = mapper.readTree(result)
                    .path("data");
            System.out.println("result:" + jsonNode);
            String from = jsonNode.path("from").asText();
//            System.out.println("11111111from:" + from);

            String content = "";
            String type="answer";
            List<Map<String, Object>> references = new ArrayList<>(); // 用于存储reference列表
            ChatData chatData = new ChatData();
            boolean is_final = false;
            if (from.equals("execute_result")) {
                if (jsonNode.has("content")) {
                    content = jsonNode.path("content").asText();
                } else if (jsonNode.has("results")) {
                    // 正确提取reference数组
                    JsonNode referenceNode = jsonNode.path("results").path("reference");
                    if (referenceNode.isArray()) {
                        for (JsonNode ref : referenceNode) {
                            Map<String, Object> refMap = new HashMap<>();
                            // 只提取存在的字段
                            if (ref.has("sentenceId")) {
                                refMap.put("sentenceId", ref.path("sentenceId").asInt());
                            }
                            if (ref.has("startTime")) {
                                JsonNode startTimeNode = ref.path("startTime");
                                if (startTimeNode.isInt()) {
                                    // 处理整数时间戳的情况
                                    refMap.put("startTime", startTimeNode.asInt());
                                } else if (startTimeNode.isTextual()) {
                                    // 处理ISO 8601字符串格式的情况
                                    refMap.put("startTime", startTimeNode.asText());
                                }
                                // 也可以考虑统一转换为某种格式
                            }
                            if (ref.has("endTime")) {
                                JsonNode endTimeNode = ref.path("endTime");
                                if (endTimeNode.isInt()) {
                                    // 处理整数时间戳的情况
                                    refMap.put("endTime", endTimeNode.asInt());
                                } else if (endTimeNode.isTextual()) {
                                    // 处理ISO 8601字符串格式的情况
                                    refMap.put("endTime", endTimeNode.asText());
                                }
                                // 也可以考虑统一转换为某种格式
                            }
                            if (ref.has("text")) {
                                refMap.put("text", ref.path("text").asText());
                            }

                            if (ref.has("description")) {
                                refMap.put("description", ref.path("description").asText());
                            }
                            if (ref.has("location")) {
                                refMap.put("location", ref.path("location").asText());
                            }
                            if (ref.has("meetingId")) {
                                refMap.put("meetingId", ref.path("meetingId").asText());
                            }
                            if (ref.has("title")) {
                                refMap.put("title", ref.path("title").asText());
                            }
                            references.add(refMap);
                        }
                        chatData.setReferences(references);
                    }

                    type = "reference";
                    is_final = true;
                }
            } else {
                if(!jsonNode.path("node_id").asText(null).equals(currentStage)){
                    currentStage = jsonNode.path("node_id").asText(null);
                    handleStageChange(currentStage,emitter);
                }
                return currentStage;
            }

            chatData.setText(content);
            chatData.setMessage_id(jsonNode.path("message_id").asText());
            chatData.setTimestamp(jsonNode.path("timestamp").asText());

            System.out.println("chatData:"+chatData);

            SsePayload chatPayload = new SsePayload(
                    type,
                    chatData,
                    System.currentTimeMillis(),
                    is_final
            );
            emitter.send( // 直接使用传入的emitter
                    SseEmitter.event()
                            .name("chat")
                            .data(chatPayload)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return currentStage;

    }

    private void handleStageChange(String currentStage,SseEmitter emitter) {
        switch (currentStage) {
            case "node4" ->
                    sendStageToEmitter(4,emitter);
//                    System.out.println("进入阶段: 问题安全检测阶段");
            case "node15" ->
                    sendStageToEmitter(15,emitter);
//                    System.out.println("进入阶段: 意图识别阶段");
            case "node16" ->
                    sendStageToEmitter(16,emitter);
//                    System.out.println("进入阶段: 普通回答思考");
            case "node10" ->
                    sendStageToEmitter(10,emitter);
//                    System.out.println("进入阶段: 会议内资料筛选与思考");
            case "node9" ->
                    sendStageToEmitter(9,emitter);
//                    System.out.println("进入阶段: 会议外资料筛选与思考");
            case "node8" ->
                    sendStageToEmitter(8,emitter);
//                    System.out.println("进入阶段: 回答安全检测阶段");
            default ->
                    sendStageToEmitter(-1,emitter);
//                    System.out.println("未知阶段");
        }

    }

    private void sendStageToEmitter(Integer s,SseEmitter emitter) {

        JSONObject data = new JSONObject();
        data.fluentPut("phaseCode",s);

        SsePayload chatPayload = new SsePayload(
                "stage",
                data,
                System.currentTimeMillis(),
                true
        );
        try {
            emitter.send(
                    SseEmitter.event()
                            .name("chat")
                            .data(chatPayload)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // 判断服务端错误或客户端可重试错误（如 5xx、429 等）
    private boolean isRetryableError(Throwable error) {
        if (error instanceof WebClientResponseException) {
            int statusCode = ((WebClientResponseException) error).getStatusCode().value();
            return statusCode >= 500 || statusCode == 429; // 可根据需求扩展其他状态码
        }
        return false;
    }

    // 判断网络相关错误（如连接失败、Socket异常等）
    private boolean isNetworkError(Throwable error) {
        return error instanceof IOException;
    }

}


