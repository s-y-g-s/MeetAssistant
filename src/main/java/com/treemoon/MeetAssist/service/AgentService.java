package com.treemoon.MeetAssist.service;

import com.treemoon.MeetAssist.client.AnHengClient;
import com.treemoon.MeetAssist.dto.AnHengRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service // 标识该类是一个Spring的服务组件，会被Spring容器管理
@RequiredArgsConstructor // 自动生成一个包含所有final字段或带有@NonNull注解字段的构造函数
public class AgentService {

    private final AnHengClient anHengClient; // 通过构造函数注入ChatClient实例，用于与聊天服务交互

    /**
     * 同步执行方法
     * @param request 请求对象，包含调用聊天服务所需的参数
     * @return 返回聊天服务的同步执行结果（字符串形式）
     */
    public String execute(AnHengRequest request) {
        return anHengClient.executeSync(request); // 调用ChatClient的同步执行方法
    }

    /**
     * 流式执行方法
     * @param request 请求对象，包含调用聊天服务所需的参数
     * @return 返回一个Flux流，表示聊天服务的流式响应
     */
    public Flux<String> executeStream(AnHengRequest request) {
        request.setStream(true); // 强制启用流式模式
        return anHengClient.executeStream(request); // 调用ChatClient的流式执行方法
    }

    /**
     * 异步执行方法
     * @param request 请求对象，包含调用聊天服务所需的参数
     * @return 返回一个Mono，表示异步执行的结果（字符串形式）
     */
    public Mono<String> executeAsync(AnHengRequest request){
//        request.setStream(true); // 强制启用流式
        return anHengClient.executeAsync(request); // 调用ChatClient的异步执行方法
    }


//    // 异步流式执行
//    public Flux<String> executeAsyncStream(AnHengRequest request) {
//        request.setStream(true); // 强制启用流式
//        return Flux.defer(() -> chatClient.executeStream(request)) // 使用Flux.defer确保每次订阅时重新生成流
//                .subscribeOn(Schedulers.boundedElastic()); // 异步调度器，确保异步执行
//    }
}