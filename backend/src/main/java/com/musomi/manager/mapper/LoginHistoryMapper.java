package com.musomi.manager.mapper;

import java.util.List;

import com.musomi.manager.dto.response.LoginHistoryResponse;
import com.musomi.manager.entity.LoginHistory;

/** Maps login history entities to response DTOs. */
public final class LoginHistoryMapper {

    private LoginHistoryMapper() {
    }

    /** Maps a login history record to its response DTO. */
    public static LoginHistoryResponse toResponse(LoginHistory entity) {
        if (entity == null) {
            return null;
        }
        return new LoginHistoryResponse(
                entity.getId(),
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getUser() != null ? entity.getUser().getFullName() : null,
                entity.getUsernameAttempted(),
                entity.getSuccess(),
                entity.getIpAddress(),
                entity.getUserAgent(),
                entity.getCreatedAt()
        );
    }

    /** Maps login history records to response DTOs. */
    public static List<LoginHistoryResponse> toResponseList(List<LoginHistory> entities) {
        return entities.stream()
                .map(LoginHistoryMapper::toResponse)
                .toList();
    }
}
