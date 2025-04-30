package com.treemoon.meetassistant.cleanup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SsePayload {

    // "raw"
    // "enhanced"
    // “chat”
    private String type;
    private Object data;
    private Long timestamp;
    private boolean is_final;

}