package com.musomi.manager.dto.response;

import java.time.LocalDateTime;

/**
 * Academic year as returned by the API.
 */
public record AcademicYearResponse(
        Long id,
        Integer year,
        Boolean isCurrent,
        LocalDateTime createdAt
) {}