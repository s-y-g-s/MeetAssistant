//package com.treemoon.meetassist.service;
///*
//打算弃用
// */
//import com.alibaba.fastjson.JSONObject;
//import com.aliyuncs.CommonRequest;
//import com.aliyuncs.CommonResponse;
//import com.aliyuncs.IAcsClient;
//import com.aliyuncs.exceptions.ClientException;
//import com.aliyuncs.http.FormatType;
//import com.aliyuncs.http.MethodType;
//import com.aliyuncs.http.ProtocolType;
//import dto.com.TreeMoon.MeetAssist.SubmitTaskRequest;
//import dto.com.TreeMoon.MeetAssist.StopTaskRequest;
//import exception.com.TreeMoon.MeetAssist.BusinessException;
//import exception.com.TreeMoon.MeetAssist.ErrorType;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Service
//public class TingwuService_original {
//
//    @Value("${tingwu.domain}")
//    private String domain;
//
//    @Value("${tingwu.version}")
//    private String version;
//
//    private final IAcsClient acsClient;
//
//    public TingwuService_original(IAcsClient acsClient) {
//        this.acsClient = acsClient;
//    }
//
//    public JSONObject createMeeting(SubmitTaskRequest request) throws ClientException {
//        validateSubmitRequest(request);
//        CommonRequest commonRequest = buildBaseRequest(
//                "/openapi/tingwu/v2/tasks",
//                MethodType.PUT
//        );
//        commonRequest.putQueryParameter("type", "realtime");
//        commonRequest.setHttpContent(
//                buildSubmitContent(request).getBytes(),
//                "utf-8",
//                FormatType.JSON
//        );
//        return parseResponse(acsClient.getCommonResponse(commonRequest));
//    }
//
//    public JSONObject stopMeeting(StopTaskRequest request) throws ClientException {
//        CommonRequest commonRequest = buildBaseRequest(
//                "/openapi/tingwu/v2/tasks",
//                MethodType.PUT
//        );
//        commonRequest.putQueryParameter("type", "realtime");
//        commonRequest.putQueryParameter("operation", "stop");
//        commonRequest.setHttpContent(
//                buildStopContent(request).getBytes(),
//                "utf-8",
//                FormatType.JSON
//        );
//        return parseResponse(acsClient.getCommonResponse(commonRequest));
//    }
//
//    private void validateSubmitRequest(SubmitTaskRequest request) {
//        if (request.getAppKey() == null || request.getAppKey().isEmpty()) {
//            throw new BusinessException(ErrorType.INVALID_PARAMETER, "AppKey不能为空");
//        }
//    }
//
//    private CommonRequest buildBaseRequest(String uri, MethodType method) {
//        CommonRequest request = new CommonRequest();
//        request.setSysDomain(domain);
//        request.setSysVersion(version);
//        request.setSysProtocol(ProtocolType.HTTPS);
//        request.setSysMethod(method);
//        request.setSysUriPattern(uri);
//        return request;
//    }
//
//    private String buildSubmitContent(SubmitTaskRequest request) {
//        JSONObject root = new JSONObject();
//        root.put("AppKey", request.getAppKey());
//
//        JSONObject input = new JSONObject();
//        input.put("SourceLanguage", request.getSourceLanguage());
//        input.put("Format", request.getFormat());
//        input.put("SampleRate", request.getSampleRate());
//        input.put("TaskKey", request.getTaskKey());
//        root.put("Input", input);
//
//        root.put("Parameters", request.getParameters());
//        return root.toJSONString();
//    }
//
//    private String buildStopContent(StopTaskRequest request) {
//        JSONObject root = new JSONObject();
//        JSONObject input = new JSONObject();
//        input.put("TaskId", request.getTaskId());
//        root.put("Input", input);
//        return root.toJSONString();
//    }
//
//    private JSONObject parseResponse(CommonResponse response) {
//        return JSONObject.parseObject(response.getData());
//    }
//}