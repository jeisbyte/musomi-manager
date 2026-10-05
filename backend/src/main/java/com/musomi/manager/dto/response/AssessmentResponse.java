package com.musomi.manager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AssessmentResponse(
        Long id,
        String title,
        String type,
        LocalDate assessmentDate,
        BigDecimal maxScore,
        String status,
        Long classId,
        String className,
        Long streamId,
        String streamName,
        Long subjectId,
        String subjectName,
        Long termId,
        String termName,
        Integer academicYear,
        Long teacherId,
        String teacherName,
        List<TopicSummary> topics,
        Long studentCount,
        Long enteredCount,
        LocalDateTime createdAt,
        LocalDateTime publishedAt
) {
    public record TopicSummary(Long id, String name) {
    }
}
