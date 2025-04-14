package com.treemoon.MeetAssist.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.treemoon.MeetAssist.dto.AnHengRequest;
import com.treemoon.MeetAssist.dto.SsePayload;
import com.treemoon.MeetAssist.mapper.MeetingMapper;
import com.treemoon.MeetAssist.mapper.TranscriptionMapper;
import com.treemoon.MeetAssist.pojo.Meeting;
import com.treemoon.MeetAssist.pojo.Transcription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TranscriptionService {

    private final MeetingMapper meetingMapper;
    private final NlsClient nlsClient;
    private final TranscriptionMapper transcriptionMapper;
    private final AgentService agentService;
    private final SseService sseService;

    // 类成员变量，用于生成唯一序号
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    private String getStream(String taskKey){

        Meeting meeting = meetingMapper.selectAllByTaskKey(taskKey);
        return meeting.getMeetingJoinUrl();

    }



//    private String getAudioUrl(){}

    private void beginRealtimeTrans(String taskKey,String stream,String audioFilePath) throws Exception {

        SpeechTranscriber speechTranscriber = new SpeechTranscriber(
                nlsClient,         // 阿里云NLS客户端
                "default",         // 使用的配置（如采样率、语言模型等）
                createListener(taskKey), // 回调监听器（处理转写结果）
                stream             // 流ID（用于标识当前音频流）
        );
        speechTranscriber.start();  // 启动转写会话

        byte[] buffer = new byte[6400];  // 音频数据缓冲区（假设每次发送6400字节）
        FileInputStream fis = new FileInputStream(audioFilePath);  // 读取本地音频文件
        int len;
        while ((len = fis.read(buffer)) != -1) {  // 循环读取文件数据
            speechTranscriber.send(Arrays.copyOf(buffer, len));  // 发送音频数据块
            Thread.sleep(200);  // 模拟实时采集间隔（200ms）
        }

        speechTranscriber.stop();  // 通知服务器音频流结束
        speechTranscriber.close(); // 关闭转写会话
    }

    public SpeechTranscriberListener createListener(String taskKey) {
        return new SpeechTranscriberListener() {
            @Override
            public void onMessage(String message) {
                System.out.println("onMessage " + message);
                if (message == null || message.trim().isEmpty()) {
                    return;
                }
                SpeechTranscriberResponse response = JSON.parseObject(message, SpeechTranscriberResponse.class);
                if("ResultTranslated".equals(response.getName())) {
                    // 翻译事件输出，您可以在此处进行相关处理
                    System.out.println("--- ResultTranslated ---" + JSON.toJSONString(response, SerializerFeature.PrettyFormat));
                } else {
                    // 原语音识别事件输出，交由父类负责回调
                    super.onMessage(message);
                }
            }

            @Override
            public void onTranscriberStart(SpeechTranscriberResponse response) {
                // task_idf非常重要，但需要说明的是，该task_id是在音频流实时推送和识别过程中的标识，而非会议级别的TaskId
                System.out.println("task_id: " + response.getTaskId() + ", name: " + response.getName() + ", status: " + response.getStatus());
            }

            @Override
            public void onSentenceBegin(SpeechTranscriberResponse response) {
                System.out.println("received onSentenceBegin: " + JSON.toJSONString(response));
            }

            @Override
            public void onSentenceEnd(SpeechTranscriberResponse response) {
                //识别出一句话。服务端会智能断句，当识别到一句话结束时会返回此消息。
                System.out.println("received onSentenceEnd: " + JSON.toJSONString(response));
                System.out.println("task_id: " + response.getTaskId() +
                        ", name: " + response.getName() +
                        // 状态码“20000000”表示正常识别。
                        ", status: " + response.getStatus() +
                        // 句子编号，从1开始递增。
                        ", index: " + response.getTransSentenceIndex() +
                        // 当前的识别结果。
                        ", result: " + response.getTransSentenceText() +
                        // 当前的词模式识别结果。
                        ", words: " + response.getWords() +
                        // 开始时间
                        ", begin_time: " + response.getSentenceBeginTime() +
                        // 当前已处理的音频时长，单位为毫秒。
                        ", time: " + response.getTransSentenceTime());
                // 当前的识别结果(固定的，不再变化的识别结果)
                String text = response.getTransSentenceText();
                // 当前的识别结果(不同于response.getTransSentenceText()， 此处的识别结果可能会出现变化)
                SpeechTranscriberResponse.StashResult stashResult = response.getStashResult();
                // 将上面两段识别结果拼接起来
                String stashText = stashResult == null ? "" : stashResult.getText();
                System.out.println("[onSentenceEnd] text = " + text + " | stashText = " + stashText);


                Transcription transcription = new Transcription();
                transcription.setText(text);
                transcription.setBeginTime(response.getSentenceBeginTime());
                transcription.setTaskKey(taskKey);
                transcription.setIndex(response.getTransSentenceIndex());

                // 发送原始转录结果
                SsePayload rawPayload = new SsePayload(
                        "raw",
                        transcription,
                        System.currentTimeMillis(),
                        true
                );


                transcriptionMapper.addTranscription(transcription);

                sseService.sendTranscription(taskKey, rawPayload);


                // 构建请求参数
                AnHengRequest request = new AnHengRequest();
                request.setSid(UUID.randomUUID().toString()); // 生成唯一会话ID
                request.setId("a15aae3e-0c04-43ee-a3c3-3023ea81402f");     // 智能体ID
                request.setInput(text);

                Transcription transcriptionEnhance = new Transcription();

                // 在调用异步方法的地方
                int currentIndex = requestCounter.getAndIncrement(); // 获取当前调用序号
                int maxRetries = 3; // 最大重试次数（总尝试次数 = 1 + maxRetries）
                AtomicBoolean shouldStop = new AtomicBoolean(true);
                agentService.executeStream(request)
                        .doOnSubscribe(sub ->
                                System.out.println("开始调用 [序号: " + currentIndex + "]")
                        )
                        .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(100)) // 修正重试次数
                                .filter(error ->
                                        error instanceof TimeoutException ||
                                                isRetryableError(error) ||
                                                isNetworkError(error)
                                )
                                .doBeforeRetry(retrySignal ->
                                        System.out.printf("@%d 重试中 [第 %d 次], 错误: %s%n",
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
                                    try {
                                        System.out.println("转录增强：");
//                                        result = result.substring(5).trim();
                                        System.out.println(result);

                                        ObjectMapper mapper = new ObjectMapper();
                                        JsonNode jsonNode = mapper.readTree(result);
                                        JsonNode dataNode = jsonNode.get("data");
//                                        System.out.println(dataNode);
                                        if(dataNode.has("content")){
//                                            String content = dataNode.path("content").asText();
//                                            // 检测停止条件
//                                            if (content.equals("text")) {
//                                                shouldStop.set(false);
//                                                return; // 不传输该块，直接停止
//                                            } else if (content.equals("knowledge")) {
//                                                shouldStop.set(true);
//                                                return;
//                                            }
//                                            // 如果未停止，传输当前 content
//                                            if (!shouldStop.get()) {
//
//                                                transcriptionEnhance.setIndex(currentIndex);
//                                                transcriptionEnhance.setText(content);
//
//                                                // 发送增强结果
//                                                SsePayload enhancedPayload = new SsePayload(
//                                                        "enhanced",
//                                                        transcriptionEnhance,
//                                                        System.currentTimeMillis(),
//                                                        false
//                                                );
//                                                sseService.sendTranscription(taskKey, enhancedPayload);
//                                            }
                                        }else{
                                                JsonNode jsonContent = dataNode.path("results")
                                                        .path("formated_data");

                                            transcriptionEnhance.setCard(jsonContent.path("knowledge_data").toString());
//                                            transcriptionEnhance.setCard(jsonContent.path("knowledge_data").asText());
                                            transcriptionEnhance.setIndex(currentIndex);
//                                            transcriptionEnhance.setText(jsonContent.path("text").toString());
                                            transcriptionEnhance.setText(jsonContent.path("text").asText());
                                            transcriptionEnhance.setBeginTime(transcriptionMapper.selectStartTime(currentIndex));
                                            transcriptionEnhance.setTaskKey(taskKey);
                                            transcriptionMapper.addEnhanceTrans(transcriptionEnhance);

                                                 // 发送cards
                                                SsePayload enhancedPayload = new SsePayload(
                                                        "enhanced",
                                                        transcriptionEnhance,
                                                        System.currentTimeMillis(),
                                                        true
                                                );
                                                sseService.sendTranscription(taskKey, enhancedPayload);
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                },
                                error -> System.err.println("调用[序号: " + currentIndex + "]失败: " + error.getMessage())
                        );



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

            @Override
            public void onTranscriptionResultChange(SpeechTranscriberResponse response) {
                // 识别出中间结果。仅当OutputLevel=2时，才会返回该消息。
                System.out.println("received onTranscriptionResultChange: " + JSON.toJSONString(response));
                System.out.println("task_id: " + response.getTaskId() +
                        ", name: " + response.getName() +
                        // 状态码“20000000”表示正常识别。
                        ", status: " + response.getStatus() +
                        // 句子编号，从1开始递增。
                        ", index: " + response.getTransSentenceIndex() +
                        // 当前的识别结果。
                        ", result: " + response.getTransSentenceText() +
                        // 当前的词模式识别结果。
                        ", words: " + response.getWords() +
                        // 当前已处理的音频时长，单位为毫秒。
                        ", time: " + response.getTransSentenceTime());

                Transcription transcription = new Transcription();
                transcription.setText(response.getTransSentenceText());
                transcription.setBeginTime(response.getTransSentenceTime());
                transcription.setTaskKey(taskKey);
                transcription.setIndex(response.getTransSentenceIndex());

                // 发送原始转录结果
                SsePayload rawPayload = new SsePayload(
                        "raw",
                        transcription,
                        System.currentTimeMillis(),
                        false
                );

                sseService.sendTranscription(taskKey, rawPayload);

            }



            @Override
            public void onTranscriptionComplete(SpeechTranscriberResponse response) {
                // 识别结束，当调用speechTranscriber.stop()之后会收到该事件
                System.out.println("received onTranscriptionComplete: " + JSON.toJSONString(response));
            }

            @Override
            public void onFail(SpeechTranscriberResponse response) {
                // 实时识别出错，请关注错误码，请记录此task_id以便排查
                System.out.println("received onFail: " + JSON.toJSONString(response));
            }
        };
    }



    public void startRealtimeTranscription(String taskKey,String audioFilePath) throws Exception {

        String stream = getStream(taskKey);
        beginRealtimeTrans(taskKey,stream,audioFilePath);


    }
}
