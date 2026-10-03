package com.musomi.manager.dto.response;

/** Subject details returned to clients. */
public record SubjectResponse(
        Long id,
        String code,
        String name,
        String description,
        Boolean isCore,
        Boolean isActive
) {
}
