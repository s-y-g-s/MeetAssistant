package com.treemoon.meetassistant.cleanup.pojo;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class Transcription {

    private int startTime;
    private String textContent;
    private int sentenceIndex ;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String taskKey;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int id ;
    //为转录增强服务
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String card;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int endTime;

}
