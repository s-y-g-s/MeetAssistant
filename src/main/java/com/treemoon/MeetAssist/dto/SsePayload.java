package com.treemoon.MeetAssist.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SsePayload {
    private String type;  // "raw"/"enhanced"
    private Object data;
    private Long timestamp;
    private boolean is_final;
}