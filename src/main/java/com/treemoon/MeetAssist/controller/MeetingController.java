package com.treemoon.MeetAssist.controller;

import com.aliyuncs.CommonResponse;
import com.aliyuncs.exceptions.ClientException;
import com.treemoon.MeetAssist.service.TingwuService;
import com.treemoon.MeetAssist.service.TransDataService;
import com.treemoon.MeetAssist.service.TranscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController //表示这是一个 RESTful 控制器，会自动将方法的返回值转换为 JSON 格式。
@RequiredArgsConstructor
@RequestMapping("/MeetingAssist/")
public class MeetingController {

    private final TingwuService tingwuService;
    private final TransDataService transDataService;
    private final TranscriptionService transcriptionService;
    @PostMapping("/{taskKey}")
    public ResponseEntity<?> createMeeting(
            @PathVariable String taskKey
    ) {
        try {
            return ResponseEntity.ok(tingwuService.createMeeting(taskKey));
        } catch (Exception e) {
            throw new RuntimeException("创建会议失败", e);
        }
    }


//    @PostMapping("/transcribe/{taskKey}")
//    public ResponseEntity<String> transcribeAudio(@PathVariable String taskKey) {
//        try {
//            String audioFilePath="D:\\output_audio.pcm";
//            transcriptionService.startRealtimeTranscription(taskKey,audioFilePath);
//            return ResponseEntity.ok("转写成功.");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
    @PostMapping("/transcribe/{taskKey}")
    public ResponseEntity<String> transcribeAudio(
            @PathVariable String taskKey,
            @RequestParam String audioFilePath
    ) {
        try {
            // 保持原有逻辑
            transcriptionService.startRealtimeTranscription(taskKey, audioFilePath);
            return ResponseEntity.ok("转写已启动");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("转写启动失败: " + e.getMessage());
        }
    }


    @PostMapping("/transStop/{taskKey}")
    public CommonResponse transcribeStop(@PathVariable String taskKey) throws ClientException {
        try {
            transDataService.getProcessedData();
            transDataService.getRawData();
            return tingwuService.stopMeeting(taskKey);
        }catch (Exception e){
            return null;
        }


    }


}