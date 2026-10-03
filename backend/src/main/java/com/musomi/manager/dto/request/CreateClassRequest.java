package com.musomi.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request to create a class. */
public record CreateClassRequest(

        @NotBlank
        @Size(max = 50)
        String name,

        @NotNull
        Long classLevelId,

        @NotNull
        Long academicYearId,

        Long classTeacherId
) {
}
