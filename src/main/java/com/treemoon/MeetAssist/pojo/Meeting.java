package com.treemoon.MeetAssist.pojo;

import lombok.Data;

@Data
public class Meeting {
    private Integer id;
    private String taskId          ;
    private String taskKey         ;
    private String meetingJoinUrl ;
    private String requestId       ;

}
