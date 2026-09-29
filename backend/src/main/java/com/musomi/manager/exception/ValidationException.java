package com.musomi.manager.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
public class ValidationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public ValidationException(ErrorCode errorCode) {
        this(errorCode, Collections.emptyMap());
    }

    public ValidationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details != null ? details : Collections.emptyMap();
    }
}
