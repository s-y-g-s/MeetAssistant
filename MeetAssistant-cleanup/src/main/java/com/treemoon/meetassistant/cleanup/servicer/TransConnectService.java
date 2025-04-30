package com.treemoon.meetassistant.cleanup.servicer;


import com.treemoon.meetassistant.cleanup.dto.SsePayload;
import com.treemoon.meetassistant.cleanup.mapper.EnhanceTransMapper;
import com.treemoon.meetassistant.cleanup.pojo.Transcription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TransConnectService {

    private final TranscriptionService transcriptionService;
    private final EnhanceTransMapper enhanceTransMapper;
    private final SseService sseService;


    public SseEmitter sendMissedEvent(int lastEventId, String taskKey){

        SseEmitter emitter = sseService.createEmitter(taskKey);

        // 查询丢失的事件（ID > lastEventId）
        List<Transcription> missedEvents = enhanceTransMapper.selectAllBiggerThanIndex(lastEventId ,taskKey);
        CompletableFuture.runAsync(() -> {
            for (Transcription transcription : missedEvents) {
                SsePayload enhancedPayload = new SsePayload(
                        "missedEnhanced",
                        transcription,
                        System.currentTimeMillis(),
                        true
                );

                sseService.sendTransEmitter(taskKey,enhancedPayload);

            }
        });

        CompletableFuture.runAsync(() -> {
            transcriptionService.createListener(taskKey);
        });

        return emitter;
    }

}


