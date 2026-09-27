package com.musomi.manager.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.musomi.manager.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private T data;
    private ErrorResponse error;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return error(errorCode, Collections.emptyMap());
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, Map<String, Object> details) {
        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.name(),
                errorCode.getMessage(),
                details != null ? details : Collections.emptyMap()
        );
        return new ApiResponse<>(null, errorResponse);
    }
}
