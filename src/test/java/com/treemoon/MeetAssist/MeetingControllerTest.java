//package com.treemoon.MeetAssist;
//
//import com.alibaba.fastjson.JSONObject;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.treemoon.MeetAssist.controller.MeetingController;
//import com.treemoon.MeetAssist.dto.SubmitTaskRequest;
//import com.treemoon.MeetAssist.service.TingwuService;
//import lombok.RequiredArgsConstructor;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(MeetingController.class)
//@RequiredArgsConstructor
//public class MeetingControllerTest {
//
//    private final MockMvc mockMvc;
//    private final TingwuService tingwuService;
//    private final ObjectMapper objectMapper;
//
//    @Test
//    public void testCreateMeeting_Success() throws Exception {
//        // 1. 准备请求参数
//        SubmitTaskRequest request = new SubmitTaskRequest();
//        request.setTaskKey("test1");
////        JSONObject parameters = new JSONObject();
////        parameters.put("audioUrl", "D:\\桌面\\MeetAssist-main\\src\\main\\resources\\audio\\test_1.pcm");
////        parameters.put("outputFormat", "txt");
////        request.setParameters(parameters);
//
//        // 2. 模拟服务层返回
//        JSONObject mockResponse = new JSONObject();
//        mockResponse.put("meetingId", "12345");
//        when(tingwuService.createMeeting(any(SubmitTaskRequest.class))).thenReturn(mockResponse);
//
//        // 3. 执行请求并验证结果
//        mockMvc.perform(post("/meetings")  // 根据实际 Controller 的 @PostMapping 路径修改
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.meetingId").value("12345"));
//    }
//
//    @Test
//    public void testCreateMeeting_InvalidRequest() throws Exception {
//        // 测试无效请求（例如缺少必要字段）
//        SubmitTaskRequest invalidRequest = new SubmitTaskRequest(); // 缺少 appKey、taskKey 等字段
//
//        mockMvc.perform(post("/meetings")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidRequest)))
//                .andExpect(status().isBadRequest()); // 预期 400 错误
//    }
//}