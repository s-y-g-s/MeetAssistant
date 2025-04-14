//package com.treemoon.MeetAssist;
//
//import com.treemoon.MeetAssist.mapper.TranscriptionMapper;
//import com.treemoon.MeetAssist.pojo.Transcription;
//import lombok.RequiredArgsConstructor;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//@RequiredArgsConstructor
//public class TranscriptionTest {
//    private final TranscriptionMapper transcriptionMapper; // 确保字段为final
//
//    @Test
//    public void testAddTranscription() {
//        Transcription transcription = new Transcription();
//        transcription.setText("example text");
//        transcription.setStashText("example stash text");
//        transcription.setBeginTime("12345");
//        transcription.setTaskKey("task-12345");
//
//        transcriptionMapper.addTranscription(transcription);
//    }
//}