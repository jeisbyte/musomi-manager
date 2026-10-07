package com.musomi.manager.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveMarksRequest(
        @NotNull
        @DecimalMin(value = "0")
        BigDecimal score,

        @Size(max = 200)
        String feedback
) {
}
