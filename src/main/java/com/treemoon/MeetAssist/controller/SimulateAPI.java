package com.treemoon.MeetAssist.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.treemoon.MeetAssist.dto.SsePayload;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

@RestController
public class SimulateAPI {

    @GetMapping(value = "/chatSimulate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatSimulate() {
        String text = "防火墙作为安全的第一道防线，经历了几十年的发展。未来的防火墙将更加智能，通过一体化安全运营和体系化的AI体系发挥作用。目前，我们的防火墙已经实现了与平台和周边产品的良好联动与对接，能够打通数据日志分析和处置的整个流程，并提供防火墙一体化的MS云托管服务。如果客户在节假日希望将防火墙托管到云端，可以使用安恒的MS云托管服务，我们会有专人帮助客户运营。请注意，以上信息仅供参考，具体服务内容和条款以安恒官方公告为准。";

        SseEmitter emitter = new SseEmitter();

        // 异步线程发送数据
        new Thread(() -> {
            try {
                JSONObject data = new JSONObject();
                data.put("phaseCode", 4);
                SsePayload chatPayload = new SsePayload(
                        "stage",
                        data,
                        System.currentTimeMillis(),
                        true
                );
                sendEvent(emitter, chatPayload);
                sleep(2000);

                data.clear();
                data.put("phaseCode", 15);
                chatPayload = new SsePayload(
                        "stage",
                        data,
                        System.currentTimeMillis(),
                        true
                );
                sendEvent(emitter, chatPayload);
                sleep(2000);

                data.clear();
                data.put("phaseCode", 10);
                chatPayload = new SsePayload(
                        "stage",
                        data,
                        System.currentTimeMillis(),
                        true
                );
                sendEvent(emitter, chatPayload);
                sleep(2000);

                data.clear();
                data.put("phaseCode", 8);
                chatPayload = new SsePayload(
                        "stage",
                        data,
                        System.currentTimeMillis(),
                        true
                );
                sendEvent(emitter, chatPayload);
                sleep(2000);

                for (int i = 0; i < text.length(); i++) {
                    data.clear();
                    data.put("text", text.charAt(i));
                    chatPayload = new SsePayload(
                            "answer",
                            data,
                            System.currentTimeMillis(),
                            false
                    );
                    sendEvent(emitter, chatPayload);
                    sleep(50);
                }

                String referenceStr = """
                        [
                            {
                                "sentenceId": 36,
                                "startTime": 603760,
                                "endTime": 621260,
                                "text": "从单体角度看，未来的发展空间有限。要进化到下一代真正智能的防火墙，必须在一体化安全运营和体系化的AI体系中发挥作用。"
                            },
                            {
                                "sentenceId": 38,
                                "startTime": 631730,
                                "endTime": 652930,
                                "text": "通过这样的协同，我们能够打通数据日志分析和处置的整个流程，并充分发挥安全产品每种类型、每个层次的特点和优势。从而，我们能为用户提供防火墙一体化的MS云托管服务。"
                            },
                            {
                                "sentenceId": 39,
                                "startTime": 652930,
                                "endTime": 665730,
                                "text": "如果客户在节假日希望将防火墙托管到云端，可以使用安恒的MS云托管服务。"
                            },
                            {
                                "sentenceId": 40,
                                "startTime": 665730,
                                "endTime": 686990,
                                "text": "我们会有专人帮助客户运营。最后，我想讲一下我对防火墙的认知。防火墙作为一个传统产品，我相信它的未来会走向云端和智能，我期待这一天快速到来。"
                            },
                            {
                                "sentenceId": 41,
                                "startTime": 686990,
                                "endTime": 698810,
                                "text": "有一个统一且真正强大的通用安全智能在云端，它能将防火墙、我们的平台和周边产品理解和协同起来，实现真正的运营。"
                            }
                        ]""";

                // 解析参考字符串为 List<Map<String, Object>>
                List<Map> referencesList = JSONArray.parseArray(referenceStr, Map.class);

                // 将 references 列表添加到 data 中
                data.clear();
                data.put("references", referencesList);
                chatPayload = new SsePayload(
                        "references",
                        data,
                        System.currentTimeMillis(),
                        false
                );

                // 发送 references 数据
                sendEvent(emitter, chatPayload);

                // 完成发送
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    private void sendEvent(SseEmitter emitter, SsePayload payload) throws Exception {
        emitter.send(SseEmitter.event()
                .data(payload)
                .name("chat"));
    }
}