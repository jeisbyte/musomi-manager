package com.musomi.desktop.api;

import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Exception thrown when a backend API request fails or an HTTP network error occurs.
 *
 * <p>Encapsulates the standard error response defined in ERROR_CODES.md and API_CONTRACT.md:
 * <pre>{@code
 * {
 *   "error": {
 *     "code": "...",
 *     "message": "...",
 *     "details": { ... }
 *   }
 * }
 * }</pre>
 */
public class ApiException extends RuntimeException {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DEFAULT_NETWORK_ERROR_CODE = "NETWORK_ERROR";

    private final String code;
    private final Map<String, Object> details;

    /**
     * Constructs an ApiException with an error code, message, and details map.
     *
     * @param code    application error code (e.g., "SCORE_EXCEEDS_MAX")
     * @param message human-readable error description
     * @param details optional contextual key-value details
     */
    public ApiException(String code, String message, Map<String, Object> details) {
        super(message);
        this.code = code != null ? code : "UNKNOWN_ERROR";
        this.details = details != null ? Collections.unmodifiableMap(details) : Collections.emptyMap();
    }

    /**
     * Constructs an ApiException with an error code and message.
     *
     * @param code    application error code
     * @param message human-readable error description
     */
    public ApiException(String code, String message) {
        this(code, message, null);
    }

    /**
     * Constructs an ApiException for network and transport errors.
     * Sets code to "NETWORK_ERROR".
     *
     * @param message error description
     * @param cause   underlying network or I/O exception
     */
    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.code = DEFAULT_NETWORK_ERROR_CODE;
        this.details = Collections.emptyMap();
    }

    /**
     * Factory method to build an ApiException from an HTTP response.
     * Parses the JSON error body according to the API contract if present.
     *
     * @param response the HTTP response
     * @return populated ApiException
     */
    public static ApiException from(HttpResponse<String> response) {
        if (response == null) {
            return new ApiException("UNKNOWN_ERROR", "Unknown server error", null);
        }
        return from(response.statusCode(), response.body());
    }

    /**
     * Factory method to build an ApiException from an HTTP status code and response body string.
     *
     * @param statusCode HTTP status code
     * @param body       raw response body
     * @return populated ApiException
     */
    public static ApiException from(int statusCode, String body) {
        if (body != null && !body.isBlank()) {
            try {
                JsonNode root = MAPPER.readTree(body);
                JsonNode errorNode = root.has("error") ? root.get("error") : root;

                if (errorNode != null && !errorNode.isNull()) {
                    String code = errorNode.hasNonNull("code")
                            ? errorNode.get("code").asText()
                            : "HTTP_" + statusCode;

                    String message = errorNode.hasNonNull("message")
                            ? errorNode.get("message").asText()
                            : "Request failed with HTTP status " + statusCode;

                    Map<String, Object> details = null;
                    if (errorNode.hasNonNull("details") && errorNode.get("details").isObject()) {
                        details = MAPPER.convertValue(
                                errorNode.get("details"),
                                new TypeReference<Map<String, Object>>() {}
                        );
                    }

                    return new ApiException(code, message, details);
                }
            } catch (Exception ignored) {
                // Fall back if response body is not structured JSON
            }
        }

        return new ApiException("HTTP_" + statusCode, "HTTP request failed with status " + statusCode, null);
    }

    /**
     * Returns the application error code.
     *
     * @return error code string
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the error details map. Never null.
     *
     * @return unmodifiable map of error details
     */
    public Map<String, Object> getDetails() {
        return details;
    }
}
