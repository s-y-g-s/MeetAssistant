package com.treemoon.MeetAssist;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author tingwu2023
 * @desc 演示了通过OpenAPI 创建实时记录的调用方式。
 */
@SpringBootTest
public class SubmitRealtimeMeetingTaskTest {

    @Test
    public void testSummitRealtimeMeetingTask() throws ClientException {
        CommonRequest request = createCommonRequest("tingwu.cn-beijing.aliyuncs.com", "2023-09-30", ProtocolType.HTTPS, MethodType.PUT, "/openapi/tingwu/v2/tasks");
        request.putQueryParameter("type", "realtime");

        JSONObject root = new JSONObject();
        root.put("ApiKey", "输入您在听悟管控台创建的Apikey");

        JSONObject input = new JSONObject();
        input.fluentPut("SourceLanguage", "cn").fluentPut("Format", "pcm").fluentPut("SampleRate", 16000).fluentPut("TaskKey", "task" + System.currentTimeMillis());
        root.put("Input", input);

        JSONObject parameters = initRequestParameters();
        root.put("Parameters", parameters);

        System.out.println(root.toJSONString());
        request.setHttpContent(root.toJSONString().getBytes(), "utf-8", FormatType.JSON);

        // TODO 请通过环境变量设置您的AccessKeyId、AccessKeySecret
        DefaultProfile profile = DefaultProfile.getProfile("cn-beijing", System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID"), System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));
        IAcsClient client = new DefaultAcsClient(profile);
        CommonResponse response = client.getCommonResponse(request);
        System.out.println(response.getData());
        JSONObject body = JSONObject.parseObject(response.getData());
        JSONObject data = (JSONObject) body.get("Data");
        System.out.println("TaskId = " + data.getString("TaskId"));
        System.out.println("MeetingJoinUrl = " + data.getString("MeetingJoinUrl"));
    }

    private static JSONObject initRequestParameters() {
        JSONObject parameters = new JSONObject();

        // 音视频转换： 可选
        JSONObject transcoding = new JSONObject();
        //transcoding.put("TargetAudioFormat", "mp3");
        //transcoding.put("SpectrumEnabled", false);
        parameters.put("Transcoding", transcoding);

        // 语音识别
        JSONObject transcription = new JSONObject();
        transcription.put("DiarizationEnabled", true);
        JSONObject speakerCount = new JSONObject();
        speakerCount.put("SpeakerCount", 2);
        transcription.put("Diarization", speakerCount);
        parameters.put("Transcription", transcription);

        // 翻译： 可选
        JSONObject translation = new JSONObject();
        JSONArray langArry = new JSONArray();
        langArry.add("en");
        translation.put("TargetLanguages", langArry);
        parameters.put("Translation", translation);
        parameters.put("TranslationEnabled", true);

        // 章节速览： 可选
        parameters.put("AutoChaptersEnabled", false);

        // 智能纪要： 可选
        parameters.put("MeetingAssistanceEnabled", false);

        // 摘要相关： 可选
        parameters.put("SummarizationEnabled", true);
        JSONObject summarization = new JSONObject();
        JSONArray types = new JSONArray().fluentAdd("Paragraph").fluentAdd("Conversational").fluentAdd("QuestionsAnswering").fluentAdd("MindMap");
        summarization.put("Types", types);
        parameters.put("Summarization", summarization);

        // PPT抽取： 不可选，实时记录没有视频信息，无法进行ppt功能
        // parameters.put("PptExtractionEnabled", false);

        // 口语书面化：可选
        parameters.put("TextPolishEnabled", false);

        return parameters;
    }
    public static CommonRequest createCommonRequest(String domain, String version, ProtocolType protocolType, MethodType method, String uri) {
        CommonRequest request = new CommonRequest();
        request.setSysDomain(domain);
        request.setSysVersion(version);
        request.setSysProtocol(protocolType);
        request.setSysMethod(method);
        request.setSysUriPattern(uri);
        request.setHttpContentType(FormatType.JSON);
        return request;
    }
}