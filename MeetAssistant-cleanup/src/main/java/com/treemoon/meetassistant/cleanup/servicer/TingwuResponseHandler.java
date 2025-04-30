package com.treemoon.meetassistant.cleanup.servicer;


import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonResponse;
import com.treemoon.meetassistant.cleanup.exception.BusinessException;
import com.treemoon.meetassistant.cleanup.exception.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TingwuResponseHandler {
    public JSONObject parseResponse(CommonResponse response) {
        JSONObject result = JSONObject.parseObject(response.getData());
        validateResponse(result);
        return result;
    }

    private void validateResponse(JSONObject response) {

        // 检查"Code"
        if (response.containsKey("Success") && !response.getBoolean("Success")) {
            throw new BusinessException(ErrorType.TINGWU_API_FAILURE,
                response.getString("Message"));
        }

        // 对空响应的处理
        if (response.isEmpty()) {
            throw new BusinessException(ErrorType.TINGWU_API_FAILURE,
                "API返回空响应");
        }

    }
}