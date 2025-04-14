package com.treemoon.MeetAssist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data

public class AnHengRequest {

    @NotNull
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private String sid;

    @NotNull
    private String id;

    private String input;


    private Map<String, Object> inputs = new HashMap<>();



    public void setInputs(String key, Object value) {
        inputs.put(key, value);
    }

    private Boolean stream = true; //流式

    private String order = "routine";



}
