package com.treemoon.meetassistant.cleanup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChatRequest {

    private String input;
    @JsonProperty("isInMeeting") // 指定 JSON 字段名为 "isInMeeting"
    private boolean isInMeeting;


}
