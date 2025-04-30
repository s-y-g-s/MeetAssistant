package com.treemoon.meetassistant.cleanup.servicer;


import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.MethodType;
import com.treemoon.meetassistant.cleanup.dto.TingWuMeetingRequest;
import com.treemoon.meetassistant.cleanup.mapper.MeetingMapper;
import com.treemoon.meetassistant.cleanup.pojo.Meeting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class TingWuMeetingService {

    private final TingwuRequestBuilder tingwuRequestBuilder;
    private final TingwuResponseHandler tingwuResponseHandler;
    private final MeetingMapper meetingMapper;

    public ResponseEntity<?> createMeeting(String audioResource) {

        TingWuMeetingRequest request = new TingWuMeetingRequest();

        String taskKey = "trans-" + UUID.randomUUID();

        while(meetingMapper.PlagiarismCheckTaskKey(taskKey)){
            taskKey = "trans-" + UUID.randomUUID();
            log.info("taskKey已存在，重新赋值：{}",taskKey);
        }

        request.setTaskKey(taskKey);

        CommonRequest commonRequest = tingwuRequestBuilder.buildBaseRequest(
                "/openapi/tingwu/v2/tasks",
                MethodType.PUT
        );

        commonRequest.putQueryParameter("type", "realtime");

        //使用 .getBytes() 将其转换为字节数组，以便作为 HTTP 请求的内容。
        //utf-8指定了请求内容的字符编码，确保数据在传输过程中不会因字符集问题导致乱码。
        //FormatType告知服务器请求体的内容格式是 JSON，方便服务器正确解析。
        commonRequest.setHttpContent(
                tingwuRequestBuilder.buildSubmitContent(request).getBytes(),
                "utf-8",
                FormatType.JSON
        );

        try {
            JSONObject response = tingwuResponseHandler.parseResponse(
                    tingwuRequestBuilder.acsClient().getCommonResponse(commonRequest)
            );
            log.info("成功创建的任务响应：{}", response);
            saveMeeting(response, audioResource);
            return ResponseEntity.ok(response);
        } catch (ClientException e) {
            log.error("创建任务失败，请求参数: {}", commonRequest, e);
            throw new RuntimeException("创建任务失败", e); // 或自定义业务异常
        }
    }

    private void saveMeeting(JSONObject response, String audioResource) {

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
        meeting.setAudioResource(audioResource);

        // 3. 调用 Mapper 插入数据
        meetingMapper.addMeeting(meeting);
    }

    public ResponseEntity<?> stopMeeting(String taskKey) {

        String taskId=meetingMapper.selectAllByTaskKey(taskKey).getTaskId();

        CommonRequest commonRequest = tingwuRequestBuilder.buildBaseRequest(
                "/openapi/tingwu/v2/tasks",
                MethodType.PUT
        );
        commonRequest.putQueryParameter("type", "realtime");
        // 设置 operation=stop
        commonRequest.putQueryParameter("operation", "stop");

        JSONObject root = new JSONObject();
        JSONObject input = new JSONObject();
        input.put("TaskId", taskId);
        root.put("Input", input);
        commonRequest.setHttpContent(
                root.toJSONString().getBytes(),
                "utf-8",
                FormatType.JSON
        );

        try {

            JSONObject response = tingwuResponseHandler.parseResponse(
                    tingwuRequestBuilder.acsClient().getCommonResponse(commonRequest)
            );
            log.info("删除任务的响应：{}", response);
            if(response.getJSONObject("Data").getString("TaskStatus").equals("COMPLETED")){
                meetingMapper.deleteMeetingByTaskKey(taskKey);
                log.info("任务已经完成，已删除");
            }
            log.info("任务删除准备中，请稍后查看");
            return ResponseEntity.ok(response);

        } catch (ClientException e) {

            log.error("删除任务失败，请求参数: {}", commonRequest, e);
            throw new RuntimeException("删除任务失败", e); // 或自定义业务异常

        }

    }

}
