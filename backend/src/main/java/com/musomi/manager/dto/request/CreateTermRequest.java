package com.musomi.manager.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request to create a term. */
public record CreateTermRequest(

        @NotNull
        Long academicYearId,

        @NotBlank
        @Size(max = 50)
        String name,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate
) {
}
