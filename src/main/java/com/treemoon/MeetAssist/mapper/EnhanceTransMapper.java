package com.treemoon.MeetAssist.mapper;

import com.treemoon.MeetAssist.pojo.Transcription;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EnhanceTransMapper {

    List<Transcription> getJson();

    List<Transcription>selectAllBiggerThanIndex(int index);

}
