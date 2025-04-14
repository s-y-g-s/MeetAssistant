package com.treemoon.MeetAssist.config;

import com.treemoon.MeetAssist.utils.SignGenerate;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "anheng")
public class AnHengConfig {

    private String appKey;
    private String appSecret;
    // 动态生成签名
    public String getSign() {
        try {
            return SignGenerate.getSign(appKey, appSecret);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate sign", e);
        }
    }

}
