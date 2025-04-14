package com.treemoon.MeetAssist.config;

import com.treemoon.MeetAssist.dto.ErrorResponse;
import com.treemoon.MeetAssist.exception.BusinessException;
import com.treemoon.MeetAssist.exception.ErrorType;
import com.aliyuncs.exceptions.ClientException;
import jakarta.validation.ConstraintViolationException;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理业务异常
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse response = new ErrorResponse(
                400,
                ex.getErrorType().name(),
                ex.getMessage(),
                null
        );
        return ResponseEntity.badRequest().body(response);
    }

    // 对ConstraintViolationException的处理（查询参数校验）
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String errorMsg = ex.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("；"));

        ErrorResponse response = new ErrorResponse(
                400,
                ErrorType.INVALID_PARAMETER.name(),
                "参数校验失败：" + errorMsg,
                null
        );
        return ResponseEntity.badRequest().body(response);
    }

    // 处理听悟API异常
    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ErrorResponse> handleClientException(ClientException ex) {
        ErrorResponse response = new ErrorResponse(
                502,
                ErrorType.TINGWU_API_FAILURE.name(),
                "第三方服务调用失败",
                ex.getErrMsg()
        );
        return ResponseEntity.status(502).body(response);
    }


    // 处理参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("；"));

        ErrorResponse response = new ErrorResponse(
                400,
                ErrorType.INVALID_PARAMETER.name(),
                "参数校验失败：" + errorMsg,
                null
        );
        return ResponseEntity.badRequest().body(response);
    }

}
