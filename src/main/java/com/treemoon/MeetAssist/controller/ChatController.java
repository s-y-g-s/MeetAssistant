package com.treemoon.MeetAssist.controller;

import com.treemoon.MeetAssist.dto.ChatRequest;
import com.treemoon.MeetAssist.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;


@RestController //表示这是一个 RESTful 控制器，会自动将方法的返回值转换为 JSON 格式。
@RequiredArgsConstructor
@RequestMapping("/Chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public SseEmitter Chat(@Valid @RequestBody ChatRequest chatRequest) {
        return chatService.ChatGet(chatRequest);
    }
}
