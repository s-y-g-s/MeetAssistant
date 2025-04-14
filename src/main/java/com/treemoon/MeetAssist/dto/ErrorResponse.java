package com.treemoon.MeetAssist.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;       // HTTP状态码
    private String errorType; // 错误类型标识（对应ErrorType枚举）
    private String message;   // 可读错误信息
    private String details;   // 调试详情（生产环境建议关闭）
}