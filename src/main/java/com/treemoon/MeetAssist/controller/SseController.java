package com.treemoon.MeetAssist.controller;

import com.aliyuncs.CommonResponse;
import com.treemoon.MeetAssist.service.SendMissedEvent;
import com.treemoon.MeetAssist.service.SseService;
import com.treemoon.MeetAssist.service.TingwuService;
import com.treemoon.MeetAssist.service.TranscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequiredArgsConstructor
public class SseController {
    private final SseService sseService;
    private final TranscriptionService transcriptionService;
    private final TingwuService tingwuService;
    private final SendMissedEvent sendMissedEvent;

    @GetMapping(value = "/run" , produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeAndStartTranscription(
//            @PathVariable String taskKey,
            @RequestParam String audioFilePath) {

        String taskKey = "meeting1";
        SseEmitter emitter = sseService.createTransEmitter(taskKey);
        AtomicBoolean isActive = new AtomicBoolean(true); // 状态跟踪

        try {
            tingwuService.createMeeting(taskKey);
            Map<String, Object> data = new HashMap<>();
            data.put("type", "meeting-created");
            data.put("message", "会议已创建，开始转录");
            data.put("taskKey", taskKey); // 将 taskKey 包含在数据中
            safeSend(emitter, "meeting-created", data);

            // 回调中更新状态
            emitter.onCompletion(() -> {
                isActive.set(false);
                autoStopTranscription(taskKey); // 增加连接完成时的停止
            });
            emitter.onTimeout(() -> {
                System.err.println("SSE 连接超时");
                emitter.complete();
            });

            // 异步任务
            CompletableFuture.runAsync(() -> {
                try {
                    transcriptionService.startRealtimeTranscription(taskKey, audioFilePath);
                    safeSend(emitter, "transcription-completed", "转录完成");
                } catch (Exception e) {
                    safeSend(emitter, "error", e.getMessage());
                } finally {
                    emitter.complete(); // 最终关闭
                }
            });

            return emitter;
        } catch (Exception e) {
            safeSend(emitter, "error", "初始化失败: " + e.getMessage());
            emitter.completeWithError(e);
            throw new RuntimeException("SSE 初始化失败", e);
        }
    }

    /** 安全的发送方法（自动处理关闭状态） */
    private void safeSend(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException | IllegalStateException e) {
            System.err.println("发送失败（连接可能已关闭）: " + e.getMessage());
        }
    }

    // 新增统一的停止方法
    private void autoStopTranscription(String taskKey) {
        try {
            CommonResponse result = tingwuService.stopMeeting(taskKey);
            System.out.println("自动停止转录结果: " + result);
        } catch (Exception e) {
            System.err.println("自动停止转录失败: " + e.getMessage());
        }
    }


    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam(required = false, defaultValue = "0") Integer lastEventId,String taskKey) {
        SseEmitter emitter = sseService.createTransEmitter(taskKey);
        return sendMissedEvent.sendMissedEvent(lastEventId,taskKey,emitter);

    }

//    @GetMapping
}