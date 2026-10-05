package com.musomi.manager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScoreResponse(
        Long scoreId,
        Long studentId,
        BigDecimal score,
        String feedback,
        String grade,
        LocalDateTime savedAt
) {
}
