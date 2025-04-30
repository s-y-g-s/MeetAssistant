package com.treemoon.meetassistant.cleanup.config;


import com.alibaba.nls.client.protocol.NlsClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tingwu")
@Data
public class TingwuConfig {

    private String domain;
    private String version;
    private String region;
    private String accessKeyId;
    private String accessKeySecret;
    private String appKey;

    //初始化阿里云的语音识别SDK（NlsClient）
    public NlsClient nlsClient(){
        return new NlsClient("default");
    }

}

