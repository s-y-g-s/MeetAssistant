//package com.treemoon.MeetAssist.service;
//
//import com.treemoon.MeetAssist.dto.TranscriptionContext;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//import java.util.Optional;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Service
//@RequiredArgsConstructor
//public class MeetingTaskManager {
//    private final Map<String, TranscriptionContext> activeTasks = new ConcurrentHashMap<>();
//    private final TranscriptionService transcriptionService;
//
//    @Async("transcriptionThreadPool")
//    public void startTask(String taskKey, String audioPath) {
//        TranscriptionContext context = new TranscriptionContext(taskKey);
//        activeTasks.put(taskKey, context);
//
//        try {
//            transcriptionService.startTranscription(taskKey, audioPath, context::appendTranscript);
//            context.markCompleted();
//        } catch (Exception e) {
//            context.markFailed(e);
//        } finally {
//            // 可根据需求决定是否自动移除
//            // activeTasks.remove(taskKey);
//        }
//    }
//
//    public Optional<TranscriptionContext> getContext(String taskKey) {
//        return Optional.ofNullable(activeTasks.get(taskKey));
//    }
//}
//
