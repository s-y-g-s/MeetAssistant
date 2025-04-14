//package com.treemoon.MeetAssist.dto;
//
//import lombok.Data;
//
//import java.time.Instant;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//// 上下文对象
//@Data
//class TranscriptionContext {
//    private final String taskKey;
//    private List<TranscriptDTO> transcripts = new CopyOnWriteArrayList<>();
//    private TaskStatus status = TaskStatus.RUNNING;
//    private Instant startTime = Instant.now();
//    private Exception error;
//
//    public synchronized void appendTranscript(TranscriptDTO dto) {
//        transcripts.add(dto);
//    }
//
//    // 状态变更方法...
//}
