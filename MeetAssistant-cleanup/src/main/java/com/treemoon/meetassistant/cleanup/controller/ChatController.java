package com.treemoon.meetassistant.cleanup.controller;

import com.treemoon.meetassistant.cleanup.dto.ChatRequest;
import com.treemoon.meetassistant.cleanup.servicer.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.awt.*;

@RestController //表示这是一个 RESTful 控制器，会自动将方法的返回值转换为 JSON 格式。
@RequiredArgsConstructor
@RequestMapping("/MeetingAssistant")
public class ChatController {

    private final ChatService chatService;

    @PostMapping(path = "/Chat",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter Chat(@Valid @RequestBody ChatRequest chatRequest) {
        return chatService.Chat(chatRequest);
    }
}
