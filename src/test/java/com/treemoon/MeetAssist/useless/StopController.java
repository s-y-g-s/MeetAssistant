//package com.treemoon.MeetAssist.controller;
//
//import com.alibaba.fastjson.JSONObject;
//import com.aliyuncs.CommonRequest;
//import com.aliyuncs.CommonResponse;
//import com.aliyuncs.DefaultAcsClient;
//import com.aliyuncs.IAcsClient;
//import com.aliyuncs.exceptions.ClientException;
//import com.aliyuncs.http.FormatType;
//import com.aliyuncs.http.MethodType;
//import com.aliyuncs.http.ProtocolType;
//import com.aliyuncs.profile.DefaultProfile;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import javax.validation.Valid;
//
//@RestController
//public class StopController {
//
//    @PostMapping("/MeetingAssist/{taskKey}/stop")
//
//
//
//    public void stopTask() throws ClientException {
//        CommonRequest request = createCommonRequest("tingwu.cn-beijing.aliyuncs.com", "2023-09-30", ProtocolType.HTTPS, MethodType.PUT, "/openapi/tingwu/v2/tasks");
//        request.putQueryParameter("type", "realtime");
//        // 必须设置 operation=stop
//        request.putQueryParameter("operation", "stop");
//
//        JSONObject root = new JSONObject();
//        JSONObject input = new JSONObject();
////        input.put("TaskId", "请输入实时会议的TaskId");
////        root.put("Input", input);
//        System.out.println(root.toJSONString());
//        request.setHttpContent(root.toJSONString().getBytes(), "utf-8", FormatType.JSON);
//
//        // TODO 请通过环境变量设置您的AccessKeyId、AccessKeySecret
//        DefaultProfile profile = DefaultProfile.getProfile("cn-beijing", System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID"), System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));
//        IAcsClient client = new DefaultAcsClient(profile);
//        CommonResponse response = client.getCommonResponse(request);
//        System.out.println(response.getData());
//    }
//
//    public ResponseEntity<?> stopMeeting(
//            @PathVariable String taskId,
//            @Valid @RequestBody StopTaskRequest request
//    ) {
//
//        request.setTaskId(taskId);
//        try {
//            return ResponseEntity.ok(tingwuService.stopMeeting(request));
//        } catch (Exception e) {
//            throw new RuntimeException("停止会议失败", e);
//        }
//    }
//}
//
