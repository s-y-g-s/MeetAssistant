package com.treemoon.meetassistant.cleanup.servicer;


import com.aliyuncs.CommonRequest;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.treemoon.meetassistant.cleanup.config.TingwuConfig;
import com.treemoon.meetassistant.cleanup.dto.TingWuMeetingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.aliyuncs.http.MethodType;
import com.alibaba.fastjson.JSONObject;

@Component
@RequiredArgsConstructor
public class TingwuRequestBuilder {

    private final TingwuConfig tingwuConfig;

    public IAcsClient acsClient() {
        DefaultProfile profile = DefaultProfile.getProfile(
                tingwuConfig.getRegion(),
                tingwuConfig.getAccessKeyId(),
                tingwuConfig.getAccessKeySecret()
        );
        return new DefaultAcsClient(profile);
    }

//    buildBaseRequest 负责通用配置（如 URL、Headers）。
//    buildSubmitContent 负责业务数据（如表单字段、JSON Body）。
    public CommonRequest buildBaseRequest(String uri, MethodType method) {

        CommonRequest request = new CommonRequest();
        request.setSysDomain(tingwuConfig.getDomain());
        request.setSysVersion(tingwuConfig.getVersion());
        request.setSysProtocol(ProtocolType.HTTPS);
        request.setSysMethod(method);
        request.setSysUriPattern(uri);
        return request;

    }

    public String buildSubmitContent(TingWuMeetingRequest request) {

        return new JSONObject()
                .fluentPut("AppKey", tingwuConfig.getAppKey())
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
                                .fluentPut("Diarization", new JSONObject()
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