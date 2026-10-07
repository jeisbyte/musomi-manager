package com.musomi.manager.dto.response;

/** Stream details returned to clients. */
public record StreamResponse(
        Long id,
        Long classId,
        String name,
        Integer capacity,
        Boolean isActive
) {
}
