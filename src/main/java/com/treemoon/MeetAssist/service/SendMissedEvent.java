package com.treemoon.MeetAssist.service;

import com.treemoon.MeetAssist.dto.SsePayload;
import com.treemoon.MeetAssist.mapper.EnhanceTransMapper;
import com.treemoon.MeetAssist.pojo.Transcription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SendMissedEvent {

//    private final TranscriptionMapper transcriptionMapper;
    private final TranscriptionService transcriptionService;
    private final EnhanceTransMapper enhanceTransMapper;

    public SseEmitter sendMissedEvent(int lastEventId,String taskKey,SseEmitter emitter){

        // 查询丢失的事件（ID > lastEventId）
        List<Transcription> missedEvents = enhanceTransMapper.selectAllBiggerThanIndex(lastEventId);
        new Thread(() -> {
            for (Transcription transcription : missedEvents) {
                try {
                    SsePayload enhancedPayload = new SsePayload(
                            "missedEnhanced",
                            transcription,
                            System.currentTimeMillis(),
                            true
                    );
                    emitter.send(SseEmitter.event()
                            .name("transcription")
                            .data(enhancedPayload)
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            transcriptionService.createListener(taskKey);
        }).start();

        return emitter;
    }

}


