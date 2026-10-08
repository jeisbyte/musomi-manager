package com.musomi.manager.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAssessmentRequest(
        @NotNull
        Long classId,

        Long streamId,

        @NotNull
        Long subjectId,

        @NotNull
        Long termId,

        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 30)
        String type,

        @NotNull
        LocalDate assessmentDate,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal maxScore,

        @NotNull
        List<Long> topicIds
) {
}
