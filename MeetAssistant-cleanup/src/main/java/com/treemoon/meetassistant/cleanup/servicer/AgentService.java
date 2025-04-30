package com.treemoon.meetassistant.cleanup.servicer;

import com.treemoon.meetassistant.cleanup.client.AnHengClient;
import com.treemoon.meetassistant.cleanup.dto.AnHengRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AnHengClient anHengClient;

    public String execute(AnHengRequest request) {
        return anHengClient.executeSync(request);
    }

    public Flux<String> executeStream(AnHengRequest request) {
        request.setStream(true);
        return anHengClient.executeStream(request);
    }

    public Mono<String> executeAsync(AnHengRequest request){
        return anHengClient.executeAsync(request);
    }

}