package com.treemoon.meetassistant.cleanup.mapper;


import com.treemoon.meetassistant.cleanup.pojo.Meeting;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MeetingMapper {

    void addMeeting(Meeting meeting);
    Meeting selectAllByTaskKey(String taskKey);
    int deleteMeetingByTaskKey(String taskKey);
    Boolean PlagiarismCheckTaskKey(String taskKey);


}
