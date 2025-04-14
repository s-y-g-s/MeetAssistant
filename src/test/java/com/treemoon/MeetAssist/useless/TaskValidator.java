//package com.treemoon.meetassist.service;
//
//
//import dto.com.TreeMoon.MeetAssist.SubmitTaskRequest;
//import exception.com.TreeMoon.MeetAssist.BusinessException;
//import exception.com.TreeMoon.MeetAssist.ErrorType;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class TaskValidator {
//    public void validateSubmit(SubmitTaskRequest request) {
//
//        if (request.getSampleRate() != 16000 && request.getSampleRate() != 8000) {
//            throw new BusinessException(ErrorType.INVALID_PARAMETER,
//                    "采样率只支持8000或16000");
//        }
//    }
//}