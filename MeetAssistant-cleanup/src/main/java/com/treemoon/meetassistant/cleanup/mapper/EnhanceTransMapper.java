package com.treemoon.meetassistant.cleanup.mapper;


import com.treemoon.meetassistant.cleanup.pojo.Transcription;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EnhanceTransMapper {

    List<Transcription>getAllByTaskKey(String taskKey);
    void addEnhanceTrans(Transcription transcription);
    List<Transcription>selectAllBiggerThanIndex(int index, String taskKey);

}
