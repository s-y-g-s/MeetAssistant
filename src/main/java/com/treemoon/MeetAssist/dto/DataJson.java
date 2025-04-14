//package com.treemoon.MeetAssist.dto;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import lombok.Data;
//
//@Data
//public class DataJson {
//
//    private int sentenceId;
//    private String text;
//
//    @JsonInclude(JsonInclude.Include.NON_DEFAULT) // 只有在 card 不为 null 时才序列化
//    private int startTime;
//    @JsonInclude(JsonInclude.Include.NON_DEFAULT) // 只有在 card 不为 null 时才序列化
//    private int endTime;
//    @JsonInclude(JsonInclude.Include.NON_NULL) // 只有在 card 不为 null 时才序列化
//    private String card;
//
//}
