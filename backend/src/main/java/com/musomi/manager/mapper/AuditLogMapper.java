package com.musomi.manager.mapper;

import java.util.List;

import com.musomi.manager.dto.response.AuditLogResponse;
import com.musomi.manager.entity.AuditLog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Maps audit log entities to response DTOs. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuditLogMapper {

    /** Maps an audit log to its response DTO. */
    public static AuditLogResponse toResponse(AuditLog log) {
        if (log == null) {
            return null;
        }
        return new AuditLogResponse(
                log.getId(),
                log.getUser() != null ? log.getUser().getId() : null,
                log.getUser() != null ? log.getUser().getFullName() : null,
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails(),
                log.getIpAddress(),
                log.getCreatedAt()
        );
    }

    /** Maps audit logs to response DTOs. */
    public static List<AuditLogResponse> toResponseList(List<AuditLog> logs) {
        return logs.stream()
                .map(AuditLogMapper::toResponse)
                .toList();
    }
}
