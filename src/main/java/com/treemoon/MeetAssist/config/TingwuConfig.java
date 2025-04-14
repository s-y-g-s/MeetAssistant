package com.treemoon.MeetAssist.config;

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

}

