//package com.treemoon.meetassist.controller;
//
//
//import org.springframework.web.bind.annotation.*;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// 这里尝试运行HTTP的POST协议，但是失败
// */
//
//@RestController
//public class CheckController {
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @PostMapping("/api/check")
//    public ResponseEntity<String> handleCheckPost() throws Exception {
//        Map<String, Object> dataMap = new HashMap<>();
//        dataMap.put("Test", "CheckSyncConf170168050****");
//
//        Map<String, Object> responseMap = new HashMap<>();
//        responseMap.put("Code", "0");
//        responseMap.put("Data", dataMap);
//        responseMap.put("Message", "success");
//        responseMap.put("RequestId", "c919a74e337c40e2b9a5de1db003****");
//
//        String jsonResponse = objectMapper.writeValueAsString(responseMap);
//
//        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
//    }
//}