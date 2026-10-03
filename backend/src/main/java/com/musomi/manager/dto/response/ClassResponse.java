package com.musomi.manager.dto.response;

import java.time.LocalDateTime;

/** Class details returned to clients. */
public record ClassResponse(
        Long id,
        String name,
        Long classLevelId,
        Long academicYearId,
        Long classTeacherId,
        Boolean isActive,
        LocalDateTime createdAt
) {
}
