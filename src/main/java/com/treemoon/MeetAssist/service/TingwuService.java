package com.treemoon.MeetAssist.service;

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
import com.treemoon.MeetAssist.config.TingwuConfig;
import com.treemoon.MeetAssist.dto.SubmitTaskRequest;
import com.treemoon.MeetAssist.exception.BusinessException;
import com.treemoon.MeetAssist.exception.ErrorType;
import com.treemoon.MeetAssist.mapper.MeetingMapper;
import com.treemoon.MeetAssist.pojo.Meeting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TingwuService {
    private final IAcsClient acsClient;
    private final TingwuRequestBuilder requestBuilder;
    private final TingwuResponseHandler responseHandler;
    private final MeetingMapper meetingMapper;
    private final TingwuConfig tingwuConfig;
    private final TransDataService transDataService;




    /**
     * 将任务数据存入数据库
     * @param response 外部接口返回的响应数据（包含 TaskId、TaskKey 等字段）
     */
    public void saveMeeting(JSONObject response) {

        // 1. 从响应中提取数据
        JSONObject data = response.getJSONObject("Data"); // 获取 Data 对象
        String taskId = data.getString("TaskId");         // 获取 TaskId
        String taskKey = data.getString("TaskKey");       // 获取 TaskKey
        String meetingJoinUrl = data.getString("MeetingJoinUrl"); // 获取 MeetingJoinUrl
        String requestId = response.getString("RequestId");       // 获取 RequestId

        // 2. 构建 Task 对象
        Meeting meeting = new Meeting();
        meeting.setTaskId(taskId);
        meeting.setTaskKey(taskKey);
        meeting.setMeetingJoinUrl(meetingJoinUrl);
        meeting.setRequestId(requestId);

        // 3. 调用 Mapper 插入数据
//        meetingMapper.addMeeting(meeting);
    }

    public JSONObject createMeeting(String taskKey) throws ClientException {

        SubmitTaskRequest request = new SubmitTaskRequest();

        request.setTaskKey(taskKey);

        if (request.getSampleRate() != 16000 && request.getSampleRate() != 8000) {
            throw new BusinessException(ErrorType.INVALID_PARAMETER,
                    "采样率只支持8000或16000");
        }

        CommonRequest commonRequest = requestBuilder.buildBaseRequest(
                "/openapi/tingwu/v2/tasks",
                MethodType.PUT
        );
        commonRequest.putQueryParameter("type", "realtime");
        commonRequest.setHttpContent(
                requestBuilder.buildSubmitContent(request).getBytes(),
                "utf-8",
                FormatType.JSON
        );

        log.info("Creating meeting task: {}", request.getTaskKey());

//        System.out.println(commonRequest);
        JSONObject response = responseHandler.parseResponse(acsClient.getCommonResponse(commonRequest));
        System.out.println("create meeting response:"+response);

        saveMeeting(response);

        return response;
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



    public CommonResponse stopMeeting(String taskKey) throws ClientException {

        String taskId=meetingMapper.selectAllByTaskKey(taskKey).getTaskId();

        CommonRequest request = createCommonRequest("tingwu.cn-beijing.aliyuncs.com", "2023-09-30", ProtocolType.HTTPS, MethodType.PUT, "/openapi/tingwu/v2/tasks");
        request.putQueryParameter("type", "realtime");
        // 必须设置 operation=stop
        request.putQueryParameter("operation", "stop");

        JSONObject root = new JSONObject();
        JSONObject input = new JSONObject();
        input.put("TaskId", taskId);
        root.put("Input", input);
//        System.out.println(root.toJSONString());
        request.setHttpContent(root.toJSONString().getBytes(), "utf-8", FormatType.JSON);

        // TODO 请通过环境变量设置您的AccessKeyId、AccessKeySecret
        DefaultProfile profile = DefaultProfile.getProfile("cn-beijing", tingwuConfig.getAccessKeyId(), System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));
        IAcsClient client = new DefaultAcsClient(profile);
        CommonResponse response = client.getCommonResponse(request);
        System.out.println(response);


        transDataService.getProcessedData();
        transDataService.getRawData();
        meetingMapper.deleteMeetingByTaskKey(taskKey);

        return response;
    }
}