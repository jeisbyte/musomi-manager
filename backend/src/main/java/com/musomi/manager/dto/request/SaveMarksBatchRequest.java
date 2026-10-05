package com.musomi.manager.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveMarksBatchRequest(
        @NotEmpty
        List<ScoreEntry> scores
) {
    public record ScoreEntry(
            Long scoreId,

            @NotNull
            Long studentId,

            @DecimalMin(value = "0")
            BigDecimal score,

            @Size(max = 200)
            String feedback
    ) {
    }
}
