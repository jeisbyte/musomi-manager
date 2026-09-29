package com.musomi.manager.exception;

import com.musomi.manager.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(ValidationException ex) {
        log.warn("Validation error: {} - details: {}", ex.getMessage(), ex.getDetails());
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ApiResponse.error(ex.getErrorCode(), ex.getDetails()));
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handlePermissionDeniedException(PermissionDeniedException ex) {
        log.warn("Permission denied: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, Object> details = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Method argument validation failed: {}", details);
        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getStatus())
                .body(ApiResponse.error(ErrorCode.VALIDATION_FAILED, details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
        log.error("Unhandled internal server error: ", ex);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR));
    }
}
