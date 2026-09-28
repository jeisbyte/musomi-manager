package com.musomi.desktop.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musomi.desktop.api.ApiException;
import com.musomi.desktop.model.dto.LoginResponse;
import com.musomi.desktop.model.dto.UserResponse;

/**
 * Mock API response provider used when {@code AppConfig.isMockMode()} is active.
 *
 * <p>Supplies predefined mock datasets for authentication and user sessions,
 * enabling UI workflow testing without an active backend service.
 */
public final class MockData {

    private static final Logger log = LoggerFactory.getLogger(MockData.class);

    private static final UserResponse TEACHER_USER = new UserResponse(
            5L,
            "teacher1",
            "Mr. Okello",
            "okello@school.ug",
            "+256700000000",
            "TEACHER",
            1L,
            "St. Mary's Secondary School"
    );

    private MockData() {}

    /**
     * Dispatches mock responses for supported HTTP method and path combinations.
     *
     * @param <T>    expected return type
     * @param method HTTP method (e.g., "GET", "POST")
     * @param path   endpoint request path
     * @param body   request body object (optional)
     * @param type   expected response class type
     * @return mock response data cast to type {@code T}, or {@code null}
     * @throws ApiException if no mock handler exists for the given path
     */
    public static <T> T respond(String method, String path, Object body, Class<T> type) {
        log.debug("Mock response for {} {}", method, path);

        String normalizedMethod = method != null ? method.trim().toUpperCase() : "";
        String normalizedPath = path != null ? path.trim() : "";
        if (normalizedPath.startsWith("/api/v1")) {
            normalizedPath = normalizedPath.substring(7);
        }
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }

        String key = normalizedMethod + " " + normalizedPath;

        return switch (key) {
            case "POST /auth/login" -> cast(
                    new LoginResponse(
                            "mock-token-abc123",
                            Instant.now().plus(Duration.ofHours(8)),
                            TEACHER_USER
                    ),
                    type
            );
            case "GET /auth/me" -> cast(TEACHER_USER, type);
            case "POST /auth/logout" -> null;
            default -> throw new ApiException(
                    "MOCK_NOT_IMPLEMENTED",
                    "Mock response not implemented for " + method + " " + path,
                    Map.of("method", method != null ? method : "", "path", path != null ? path : "")
            );
        };
    }

    private static <T> T cast(Object value, Class<T> type) {
        if (value == null || type == null || type == Void.class) {
            return null;
        }
        return type.cast(value);
    }
}
