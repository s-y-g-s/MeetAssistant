package com.treemoon.MeetAssist.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AliyunConfig {
    @Bean
    public IAcsClient acsClient(TingwuConfig tingwuConfig) {
        DefaultProfile profile = DefaultProfile.getProfile(
                tingwuConfig.getRegion(),
                tingwuConfig.getAccessKeyId(),
                tingwuConfig.getAccessKeySecret()
        );
        return new DefaultAcsClient(profile);
    }
}

