package com.treemoon.MeetAssist.mapper;

import com.treemoon.MeetAssist.pojo.Meeting;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MeetingMapper {

    void addMeeting(Meeting meeting);
    Meeting selectAllByTaskKey(String taskKey);
    int deleteMeetingByTaskKey(String taskKey);


}
