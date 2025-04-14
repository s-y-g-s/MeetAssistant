package com.treemoon.MeetAssist.mapper;

import com.treemoon.MeetAssist.pojo.Transcription;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface TranscriptionMapper {

    void addTranscription(Transcription transcription);
    void addEnhanceTrans(Transcription transcription);
    int selectStartTime(int sentenceId);

    List<Transcription>getAll();



}
