package com.musomi.desktop.model.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generic standard API response envelope matching API_CONTRACT.md conventions.
 *
 * @param <T>  the payload type for successful responses
 * @param data payload when the request succeeds; null on failure
 * @param error error details when the request fails; null on success
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiResponse<T>(
    @JsonProperty("data")
    T data,

    @JsonProperty("error")
    ErrorBody error
) {

    /**
     * Nested record representing error payload.
     *
     * @param code    application error code (e.g., "INVALID_CREDENTIALS")
     * @param message human-readable error description
     * @param details optional map of field-level or contextual error details
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorBody(
        @JsonProperty("code")
        String code,

        @JsonProperty("message")
        String message,

        @JsonProperty("details")
        Map<String, Object> details
    ) {

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public Map<String, Object> getDetails() {
            return details;
        }
    }

    /**
     * Creates a successful ApiResponse containing data.
     *
     * @param <T>  the payload type
     * @param data response payload
     * @return successful ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null);
    }

    /**
     * Creates an error ApiResponse with code, message, and details.
     *
     * @param <T>     the payload type
     * @param code    application error code
     * @param message human-readable message
     * @param details map of contextual error details
     * @return error ApiResponse
     */
    public static <T> ApiResponse<T> error(String code, String message, Map<String, Object> details) {
        return new ApiResponse<>(null, new ErrorBody(code, message, details));
    }

    /**
     * Creates an error ApiResponse with code and message, but without extra details.
     *
     * @param <T>     the payload type
     * @param code    application error code
     * @param message human-readable message
     * @return error ApiResponse
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(null, new ErrorBody(code, message, null));
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isError() {
        return error != null;
    }

    public T getData() {
        return data;
    }

    public ErrorBody getError() {
        return error;
    }
}
