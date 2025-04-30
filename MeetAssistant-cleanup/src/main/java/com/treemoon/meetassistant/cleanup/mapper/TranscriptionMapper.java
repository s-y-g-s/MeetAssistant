package com.treemoon.meetassistant.cleanup.mapper;


import com.treemoon.meetassistant.cleanup.pojo.Transcription;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TranscriptionMapper {

    void addTranscription(Transcription transcription);

    int selectStartTimeBySentenceIndex(int sentenceId);

    List<Transcription> getAllByTaskKey(String taskKey);

}
