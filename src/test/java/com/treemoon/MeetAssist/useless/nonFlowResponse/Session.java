package com.treemoon.MeetAssist.useless.nonFlowResponse;

import lombok.Data;

import java.util.List;
@Data
public class Session {
    private String id;
    private List<Message> messages;

    // Getters and Setters
}