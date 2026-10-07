package com.musomi.manager.dto.response;

/** Teacher assignment details returned by the API. */
public record AssignmentResponse(
        Long id,
        Long teacherId,
        String teacherName,
        Long subjectId,
        String subjectName,
        Long classId,
        String className,
        Long streamId,
        String streamName,
        Long academicYearId,
        Integer academicYear
) {}
