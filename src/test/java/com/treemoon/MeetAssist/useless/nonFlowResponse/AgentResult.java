package com.treemoon.MeetAssist.useless.nonFlowResponse;

import lombok.Data;

import java.util.Map;
@Data
public class AgentResult {
    private Session session;
    private Map<String, Object> results;
    private TokenUsage tokenUsage;

    // Getters and Setters
}