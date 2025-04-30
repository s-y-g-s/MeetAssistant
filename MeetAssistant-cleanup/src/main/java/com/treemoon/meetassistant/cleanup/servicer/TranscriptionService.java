package com.treemoon.meetassistant.cleanup.servicer;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;
import com.treemoon.meetassistant.cleanup.config.TingwuConfig;
import com.treemoon.meetassistant.cleanup.dto.SsePayload;
import com.treemoon.meetassistant.cleanup.mapper.MeetingMapper;
import com.treemoon.meetassistant.cleanup.mapper.TranscriptionMapper;
import com.treemoon.meetassistant.cleanup.pojo.Meeting;
import com.treemoon.meetassistant.cleanup.pojo.Transcription;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TranscriptionService {

    private final TingwuConfig tingwuConfig;
    private final TranscriptionMapper transcriptionMapper;
    private final SseService sseService;
    private final EnhanceTransService enhanceTransService;
    private final MeetingMapper meetingMapper;
    private final TingWuMeetingService tingWuMeetingService;

    public SseEmitter transcription(String taskKey){
        SseEmitter emitter = sseService.createEmitter(taskKey);
        CompletableFuture.runAsync(() -> startTranscription(taskKey));
        return emitter;
    }

    public void startTranscription(String taskKey) {


        try {

            Meeting meeting = meetingMapper.selectAllByTaskKey(taskKey);
            SpeechTranscriber speechTranscriber = new SpeechTranscriber(
                    tingwuConfig.nlsClient(),
                    "default",
                    createListener(taskKey),
                    meeting.getMeetingJoinUrl()
            );

            speechTranscriber.start();// 启动转写会话

            // 使用本地文件模拟 真实场景下音频流实时采集
            byte[] buffer = new byte[6400];  // 音频数据缓冲区（假设每次发送6400字节）
            FileInputStream fis = new FileInputStream(meeting.getAudioResource());  // 读取本地音频文件
            int len;
            while ((len = fis.read(buffer)) != -1) {  // 循环读取文件数据
                speechTranscriber.send(Arrays.copyOf(buffer, len));  // 发送音频数据块
                Thread.sleep(100L);  // 模拟实时采集间隔
            }

            speechTranscriber.stop();  // 通知服务器音频流结束
            speechTranscriber.close(); // 关闭转写会话
            log.info(taskKey+"转录任务已完成");
            tingWuMeetingService.stopMeeting(taskKey);

        } catch (Exception e) {
            log.error(taskKey+"转录出错："+e.getMessage(),e);
        }

    }

    public SpeechTranscriberListener createListener(String taskKey) {
        return new SpeechTranscriberListener() {
            @Override
            public void onMessage(String message) {
//                log.info("onMessage " + message);
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
                log.info("\ntaskKey转录开始: " + taskKey + ", \ntask_id: " + response.getTaskId() + ", \nname: " + response.getName() + ", \nstatus: " + response.getStatus());
            }

            @Override
            public void onSentenceBegin(SpeechTranscriberResponse response) {
                log.info("检测到句子开始: " + JSON.toJSONString(response));
            }

            @Override
            //识别出一句话。服务端会智能断句，当识别到一句话结束时会返回此消息。
            public void onSentenceEnd(SpeechTranscriberResponse response) {

                log.info("检测到句子结束，返回response: " + JSON.toJSONString(response));

                // 当前的识别结果(固定的，不再变化的识别结果)
                Transcription transcription = getTranscription(response);
                transcription.setTaskKey(taskKey);

                // 组装Emitter
                SsePayload rawPayload = new SsePayload(
                        "raw",
                        transcription,
                        System.currentTimeMillis(),
                        true
                );

                transcriptionMapper.addTranscription(transcription);
                sseService.sendTransEmitter(taskKey, rawPayload);

                enhanceTransService.enhanceTrans(taskKey,response.getTransSentenceText());

            }

            private static Transcription getTranscription(SpeechTranscriberResponse response) {

                Transcription transcription = new Transcription();
                transcription.setTextContent(response.getTransSentenceText());
                transcription.setStartTime(response.getSentenceBeginTime());
                transcription.setSentenceIndex(response.getTransSentenceIndex());

                return transcription;

            }

            @Override
            public void onTranscriptionResultChange(SpeechTranscriberResponse response) {
                // 识别出中间结果。仅当OutputLevel=2时，才会返回该消息。
                log.info("识别中间结果: " + JSON.toJSONString(response));

                // 当前的识别结果(固定的，不再变化的识别结果)
                Transcription transcription = new Transcription();
                transcription.setTextContent(response.getTransSentenceText());
                transcription.setStartTime(response.getTransSentenceTime());
                transcription.setSentenceIndex(response.getTransSentenceIndex());
                transcription.setTaskKey(taskKey);

                // 发送原始转录结果
                SsePayload rawPayload = new SsePayload(
                        "raw",
                        transcription,
                        System.currentTimeMillis(),
                        false
                );

                sseService.sendTransEmitter(taskKey, rawPayload);

            }


            @Override
            public void onTranscriptionComplete(SpeechTranscriberResponse response) {
                // 识别结束，当调用speechTranscriber.stop()之后会收到该事件
                log.info("转录结束: " + JSON.toJSONString(response));
            }

            @Override
            public void onFail(SpeechTranscriberResponse response) {
                // 实时识别出错，请关注错误码，请记录此task_id以便排查
                log.info("转录出错: " + JSON.toJSONString(response));
            }
        };
    }

}
