package com.treemoon.MeetAssist.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class SubmitTaskRequest {

    private String sourceLanguage = "cn";
    private String format = "pcm";
    private Integer sampleRate = 16000;// 也可能为8000
    private Integer outputLevel = 2 ;
    private boolean DiarizationEnabled = true;
    private boolean ProgressiveCallbacksEnabled = false;
    private Integer SpeakerCount= 2 ;

    @NotBlank(message = "任务标识不能为空")
    private String taskKey;

    private boolean AutoChaptersEnabled = true;//章节速览功能，包括：议程标题和议程摘要
    private boolean MeetingAssistanceEnabled = true;//关键词、待办事项、重点内容、场景识别
    private boolean SummarizationEnabled = true;//是否开启摘要总结功能。
    /**
     * 如果开启摘要功能，需要设置摘要类型。支持设置1个或多个。
     * Paragraph（全文摘要）
     * Conversational（发言总结）
     * QuestionsAnswering（问答回顾）
     * MindMap（思维导图）
     */
    private List<String> summarizationTypes = List.of("Paragraph", "Conversational", "QuestionsAnswering", "MindMap");


}