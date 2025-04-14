package com.treemoon.MeetAssist;

import com.treemoon.MeetAssist.service.TransDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class test {

    @Autowired
    private TransDataService transDataService; // 直接注入Spring管理的Bean

    @Test
    public void testGetRawData() {

        transDataService.getRawData();
        // 进行测试断言
    }
}