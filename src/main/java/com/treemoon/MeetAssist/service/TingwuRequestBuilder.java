package com.treemoon.MeetAssist.service;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.treemoon.MeetAssist.config.TingwuConfig;
import com.treemoon.MeetAssist.dto.SubmitTaskRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TingwuRequestBuilder {
    private final TingwuConfig config;

    public CommonRequest buildBaseRequest(String uri, MethodType method) {
        CommonRequest request = new CommonRequest();
        request.setSysDomain(config.getDomain());
        request.setSysVersion(config.getVersion());
        request.setSysProtocol(ProtocolType.HTTPS);
        request.setSysMethod(method);
        request.setSysUriPattern(uri);
        return request;
    }

    public String buildSubmitContent(SubmitTaskRequest request) {
        return new JSONObject()
                .fluentPut("AppKey", config.getAppKey())
                .fluentPut("Input", new JSONObject()
                        .fluentPut("SourceLanguage", request.getSourceLanguage())
                        .fluentPut("Format", request.getFormat())
                        .fluentPut("SampleRate", request.getSampleRate())
                        .fluentPut("TaskKey", request.getTaskKey())
                        .fluentPut("ProgressiveCallbacksEnabled", request.isProgressiveCallbacksEnabled()))
                .fluentPut("Parameters", new JSONObject()
                        .fluentPut("Transcription",new JSONObject()
                                .fluentPut("OutputLevel",request.getOutputLevel())
                                .fluentPut("DiarizationEnabled",request.isDiarizationEnabled())
                                .fluentPut("Diarization", new JSONObject() // 嵌套对象
                                        .fluentPut("SpeakerCount", request.getSpeakerCount())))
                        .fluentPut("AutoChaptersEnabled",request.isAutoChaptersEnabled())
                        .fluentPut("MeetingAssistanceEnabled",request.isMeetingAssistanceEnabled())
                        .fluentPut("SummarizationEnabled",request.isSummarizationEnabled())
                        .fluentPut("Summarization",new JSONObject()
                                .fluentPut("Types",request.getSummarizationTypes())
                        )
                )

                .toJSONString();
    }

}