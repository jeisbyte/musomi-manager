package com.musomi.manager.dto.response;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long userId,
        String userName,
        String action,
        String entityType,
        Long entityId,
        String details,
        String ipAddress,
        LocalDateTime createdAt
) {
}
