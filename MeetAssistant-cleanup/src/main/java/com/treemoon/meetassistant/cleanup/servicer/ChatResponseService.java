package com.treemoon.meetassistant.cleanup.servicer;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.treemoon.meetassistant.cleanup.dto.ChatData;
import com.treemoon.meetassistant.cleanup.dto.SsePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatResponseService {

    private final SseService sseService;

    public String ResponseHandler(String result,String taskKey, String currentStage) {

        ObjectMapper mapper = new ObjectMapper();

        try {

            JsonNode jsonNode = mapper.readTree(result)
                    .path("data");
//            log.info("Response:" + jsonNode);

            String from = jsonNode.path("from").asText();
            String content = "";
            String type="answer";

            // 用于存储reference列表
            List<Map<String, Object>> references = new ArrayList<>();
            ChatData chatData = new ChatData();
            boolean is_final = false;
            if (from.equals("execute_result")) {
                if (jsonNode.has("content")) {
                    content = jsonNode.path("content").asText();
                } else if (jsonNode.has("results")) {

                    // 提取reference数组
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
                    handleStageChange(currentStage,taskKey);
                }

                return currentStage;
            }

            chatData.setText(content);
            chatData.setMessage_id(jsonNode.path("message_id").asText());
            chatData.setTimestamp(jsonNode.path("timestamp").asText());

            SsePayload chatPayload = new SsePayload(
                    type,
                    chatData,
                    System.currentTimeMillis(),
                    is_final
            );

            sseService.sendChatEmitter(taskKey,chatPayload);

        } catch (IOException e) {
            log.error("{}出错:{}",taskKey,e.getMessage(),e);
            throw new RuntimeException(e);
        }

        return currentStage;

    }

    private void handleStageChange(String currentStage,String taskKey) {
        switch (currentStage) {
            case "node4" ->{

                log.info("进入阶段: 问题安全检测阶段");
                sendStageEmitter(4,taskKey);

            }
            case "node15" -> {

                log.info("进入阶段: 意图识别阶段");
                sendStageEmitter(15, taskKey);

            }
            case "node16" ->{

                log.info("进入阶段: 普通回答思考");
                sendStageEmitter(16,taskKey);

            }
            case "node10" ->{

                log.info("进入阶段: 会议内资料筛选与思考");
                sendStageEmitter(10,taskKey);

            }
            case "node9" ->{

                log.info("进入阶段: 会议外资料筛选与思考");
                sendStageEmitter(9,taskKey);

            }
            case "node8" ->{

                log.info("进入阶段: 回答安全检测阶段");
                sendStageEmitter(8,taskKey);

            }
            default ->{

                log.info("未知阶段");
                sendStageEmitter(-1,taskKey);

            }
        }
    }

    private void sendStageEmitter(Integer s,String taskKey) {

        JSONObject data = new JSONObject();
        data.fluentPut("phaseCode",s);

        SsePayload chatPayload = new SsePayload(
                "stage",
                data,
                System.currentTimeMillis(),
                true
        );

        sseService.sendChatEmitter(taskKey,chatPayload);

    }

}
