package com.treemoon.MeetAssist.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseService {
    // 使用线程安全的Map存储SSE连接（Key=任务ID，Value=SseEmitter）
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 创建新的SSE连接
    public SseEmitter createTransEmitter(String taskKey) {

        if (emitters.containsKey(taskKey)) {
            SseEmitter old = emitters.get(taskKey);
            old.complete(); // 正常关闭旧连接
            emitters.remove(taskKey);
        }

        // 1. 创建带超时配置的emitter（1小时）
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

        // 2. 存储到Map中
        emitters.put(taskKey, emitter);

        try {
            // 发送初始连接成功消息
            emitter.send(SseEmitter.event()
                    .name("connection")
                    .data("连接成功"));

            System.out.println("Emitter created: " + emitter);
        } catch (IOException e) {
            // 发送初始消息失败的处理
            System.err.println("初始化SSE连接失败: " + e.getMessage());
            removeEmitter(taskKey); // 清理已存储的emitter
            emitter.completeWithError(e); // 标记连接为错误状态
            return emitter; // 仍然返回emitter，但已经是错误状态
        }

        // 3. 设置完成回调（主动断开时触发）
        emitter.onCompletion(() -> {
            System.out.println("SSE连接正常关闭: " + taskKey);
            removeEmitter(taskKey);
        });

        // 4. 设置超时回调（60分钟无数据时触发）
        emitter.onTimeout(() -> {
            System.out.println("SSE连接超时: " + taskKey);
            try {
                emitter.send(SseEmitter.event()
                        .name("timeout")
                        .data("连接超时自动关闭"));
            } catch (IOException e) {
                System.err.println("发送超时通知失败: " + e.getMessage());
            } finally {
                emitter.complete();
                removeEmitter(taskKey);
            }
        });

        return emitter;
    }

    // 发送转录数据
    public void sendTranscription(String taskKey, Object data) {
        log.info("启动转录，taskKey={}", taskKey);
        SseEmitter emitter = emitters.get(taskKey);
        if (emitter == null) {
            log.warn("无活跃SSE连接，taskKey={}", taskKey);
            return;
        }
        System.out.println("开始传输");
        try {
            // 构造SSE格式数据（事件名+JSON内容）
            emitter.send(
                    SseEmitter.event()
                            .name("transcription") // 事件类型标识
                            .data(data)             // 传输内容
            );
        } catch (IOException e) {
            log.error("SSE:transcription发送失败", e);
            removeEmitter(taskKey); // 发送失败时移除失效连接
        }
    }

    // 清理资源
    private void removeEmitter(String taskKey) {
        SseEmitter emitter = emitters.remove(taskKey);
        if (emitter != null) {
            emitter.complete(); // 显式关闭连接
            log.info("Removed and closed SSE emitter for task: {}", taskKey);
        }
    }


//    // 创建一次性 SSE 连接（直接返回 emitter，无需存储）
//    public SseEmitter chatEmitter(SsePayload data) {
//        // 1. 创建不带超时的 emitter（或根据需要设置短超时，如 30 秒）
//        SseEmitter emitter = new SseEmitter(60_000L);
//
//        // 2. 异步生成大模型回答并发送
//        CompletableFuture.runAsync(() -> {
//            try {
//
//                // 发送回答并立即关闭连接
//                emitter.send(
//                        SseEmitter.event()
//                                .name("chat")   // 事件类型
//                                .data(data)     // 回答内容
//                );
////                emitter.complete();      // 关键：发送后立即断开
//
//                log.info("回答已发送并关闭连接");
//
//            } catch (IOException e) {
//                // 异常处理
//                log.error("SSE chat处理失败", e);
//                emitter.completeWithError(e);
//            }
//        });
//        emitter.complete();
//
//        // 3. 可选：设置超时兜底（防止异步任务未完成时客户端未断开）
//        emitter.onTimeout(() -> {
//            log.warn("SSE chat连接超时");
//            emitter.complete();
//        });
//
//        return emitter;
//    }

}

