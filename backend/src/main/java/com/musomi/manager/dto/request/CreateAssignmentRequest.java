package com.musomi.manager.dto.request;

import jakarta.validation.constraints.NotNull;

/** Request to assign a teacher to a subject and class. */
public record CreateAssignmentRequest(
        @NotNull Long teacherId,
        @NotNull Long subjectId,
        @NotNull Long classId,
        Long streamId,
        @NotNull Long academicYearId
) {}
