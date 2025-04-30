package com.treemoon.meetassistant.cleanup.controller;


import com.aliyuncs.CommonResponse;
import com.treemoon.meetassistant.cleanup.servicer.TingWuMeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController //表示这是一个 RESTful 控制器，会自动将方法的返回值转换为 JSON 格式。
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/Server")
public class MeetingController {

    private final TingWuMeetingService tingWuMeetingService;

    @PostMapping("/CreateMeeting")
    public ResponseEntity<?> createMeeting(
            @RequestParam String audioResource
    )
    {return tingWuMeetingService.createMeeting(audioResource);}

    @PostMapping("/StopMeeting/{taskKey}")
    public ResponseEntity<?> transcribeStop(@PathVariable String taskKey)
    {return tingWuMeetingService.stopMeeting(taskKey);}

}