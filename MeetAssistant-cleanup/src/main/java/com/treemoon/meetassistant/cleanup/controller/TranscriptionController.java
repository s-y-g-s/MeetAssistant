package com.treemoon.meetassistant.cleanup.controller;

import com.treemoon.meetassistant.cleanup.servicer.TransConnectService;
import com.treemoon.meetassistant.cleanup.servicer.TranscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@Controller
@RequestMapping("/transcribe")
public class TranscriptionController {

    private final TranscriptionService transcriptionService;
    private final TransConnectService transConnectService;

    @PostMapping(path = "/server/{taskKey}",produces = MediaType.TEXT_EVENT_STREAM_VALUE)

    public SseEmitter transcription(
            @PathVariable String taskKey
    ){
        return transcriptionService.transcription(taskKey);
    }


    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam(required = false, defaultValue = "0") Integer lastEventId, String taskKey) {

        return transConnectService.sendMissedEvent(lastEventId,taskKey);

    }


}
