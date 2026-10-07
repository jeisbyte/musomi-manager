package com.musomi.manager.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Term details returned to clients. */
public record TermResponse(
        Long id,
        Long academicYearId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isCurrent,
        LocalDateTime createdAt
) {
}
