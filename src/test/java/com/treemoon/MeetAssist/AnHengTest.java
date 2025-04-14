package com.treemoon.MeetAssist;

import com.treemoon.MeetAssist.client.AnHengClient;
import com.treemoon.MeetAssist.dto.AnHengRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MeetAssistApplication.class}) // 替换为实际启动类
public class AnHengTest {

    @Autowired
    private AnHengClient realAnHengClient;

    @Test
    public void testRealConnection() {
        // 1. 构建真实请求参数
        AnHengRequest request = new AnHengRequest();

        request.setSid(UUID.randomUUID().toString());
        request.setId("a64a474a-d669-475a-bfac-06940fada3b4");
        request.setInput("{ \"input\": \"会议的主题是什么？\", \"isInMeeting\": true}");
        request.setStream(false);

        String response = realAnHengClient.executeSync(request);

        System.out.println("Response: " + response);


    }
}