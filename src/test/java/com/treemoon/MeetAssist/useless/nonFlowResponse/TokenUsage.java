package com.treemoon.MeetAssist.useless.nonFlowResponse;

import lombok.Data;

@Data
public class TokenUsage {
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;

    // Getters and Setters
}