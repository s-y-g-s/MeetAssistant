package com.treemoon.MeetAssist.config;

import com.alibaba.nls.client.protocol.NlsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class NlsClientConfig {
    @Bean
    public NlsClient nlsClient() {
        return new NlsClient("default");
    }
}
