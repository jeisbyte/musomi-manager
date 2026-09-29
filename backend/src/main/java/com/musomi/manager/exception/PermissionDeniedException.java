package com.musomi.manager.exception;

import lombok.Getter;

@Getter
public class PermissionDeniedException extends RuntimeException {

    private final ErrorCode errorCode;

    public PermissionDeniedException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PermissionDeniedException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
