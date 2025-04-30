package com.treemoon.meetassistant.cleanup.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ChatData {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String text;

    private String message_id;
    private String timestamp;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Map<String, Object>> references;

}
