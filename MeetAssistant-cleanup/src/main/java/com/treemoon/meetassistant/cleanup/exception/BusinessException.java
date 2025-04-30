package com.treemoon.meetassistant.cleanup.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorType errorType;

    public BusinessException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

}