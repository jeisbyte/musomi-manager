package com.musomi.manager.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record MarkGridResponse(
        AssessmentSummary assessment,
        List<StudentRow> students,
        List<ScoreRow> scores
) {
    public record AssessmentSummary(
            Long id,
            String title,
            String type,
            BigDecimal maxScore,
            String status,
            String subjectName,
            String className,
            String streamName,
            String termName,
            Integer academicYear,
            List<String> topics
    ) {
    }

    public record StudentRow(
            Long id,
            String admissionNumber,
            String fullName
    ) {
    }

    public record ScoreRow(
            Long scoreId,
            Long studentId,
            BigDecimal score,
            String feedback,
            String grade
    ) {
    }
}
