package com.treemoon.MeetAssist.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class Transcription {


    private int beginTime;
    private String text;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String taskKey;
    private int index ;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int id ;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String card;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int endTime;
}
