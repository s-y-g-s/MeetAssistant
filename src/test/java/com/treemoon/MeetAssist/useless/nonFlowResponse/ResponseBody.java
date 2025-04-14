package com.treemoon.MeetAssist.useless.nonFlowResponse;

import lombok.Data;

@Data
public class ResponseBody {
    private String code;
    private String msg;
    private AgentResult data;

    // Getters and Setters
}
