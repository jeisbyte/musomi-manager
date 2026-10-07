package com.musomi.manager.dto.response;

import java.time.LocalDateTime;

/** Login attempt details returned by the API. */
public record LoginHistoryResponse(
        Long id,
        Long userId,
        String userName,
        String usernameAttempted,
        Boolean success,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt
) {}
