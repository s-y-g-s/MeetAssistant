package com.treemoon.meetassistant.cleanup.servicer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class SseService {

    // 使用线程安全的Map存储SSE连接（Key=任务ID，Value=SseEmitter）
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 创建新的SSE连接
    public SseEmitter createEmitter(String taskKey) {

        // 关闭旧连接
        if (emitters.containsKey(taskKey)) {
            SseEmitter old = emitters.get(taskKey);
            old.complete();
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
            log.info("{}：SSE连接创建成功",taskKey);

        } catch (IOException e) {

            // 发送初始消息失败的处理
            log.error("初始化SSE连接失败: " + e.getMessage());
            emitter.completeWithError(e); // 标记连接为错误状态,将异常 e 传递给客户端（如果客户端仍可接收）。
            return emitter; // 仍然返回emitter，但已经是错误状态

        }

        // 3. 设置完成回调（主动断开时触发）
        emitter.onCompletion(() -> {
            removeEmitter(taskKey);
        });

        // 4. 设置超时回调（60分钟无数据时触发）
        emitter.onTimeout(() -> {

            handleEmitterError(new TimeoutException("连接超时"),emitter,taskKey);
            removeEmitter(taskKey);

        });

        return emitter;
    }


    // 发送转录数据
    public void sendTransEmitter(String taskKey, Object data) {

        SseEmitter emitter = emitters.get(taskKey);
        if (emitter == null) {
            log.warn("{}:无活跃SSE连接", taskKey);
            return;
        }
        try {
            // 构造SSE格式数据（事件名+JSON内容）
            emitter.send(
                    SseEmitter.event()
                            .name("transcription") // 事件类型标识
                            .data(data)             // 传输内容
            );
            log.info("\n{}发送成功:\n{}", taskKey,data);
        } catch (IOException e) {
            handleEmitterError(e,emitter,taskKey);
        }

    }

    public void sendChatEmitter(String taskKey, Object data){

        log.info("\n{}：\n{}", taskKey,data);
        SseEmitter emitter = emitters.get(taskKey);

        if (emitter == null) {
            log.warn("{}:无活跃SSE连接", taskKey);
            return;
        }

        try {
            // 构造SSE格式数据（事件名+JSON内容）
            emitter.send(
                    SseEmitter.event()
                            .name("chat") // 事件类型标识
                            .data(data)             // 传输内容
            );
        } catch (IOException e) {
            handleEmitterError(e,emitter,taskKey);
        }
    }


    // 清理资源
    public void removeEmitter(String taskKey) {
        SseEmitter emitter = emitters.remove(taskKey);
        if (emitter != null) {
            emitter.complete();
            log.info("SSE连接已经关闭: {}", taskKey);
        }
    }

    //正常结束SSE连接
    public void completeEmitter(SseEmitter emitter) {

        try {
            emitter.send(SseEmitter.event()
                    .name("complete")
                    .data("SSE连接完成")
            );
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

    }

    //发送异常原因
    public void handleEmitterError(Throwable error, SseEmitter emitter,String taskKey) {

        try {
            log.info("{}:对应SSE发生异常为{}",taskKey,error.getMessage());
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(Map.of(
                            "taskKey", taskKey,
                            "message", error.getMessage()
                            )
                    )
            );
            removeEmitter(taskKey);
        } catch (IOException e) {
            log.info("{}:对应SSE异常”{}“发送失败",taskKey,error.getMessage());
            emitter.completeWithError(e);
        }
    }

    public SseEmitter getEmitter(String taskKey){
        return emitters.get(taskKey);
    }

}
