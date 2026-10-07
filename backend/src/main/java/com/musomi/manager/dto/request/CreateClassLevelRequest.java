package com.musomi.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request to create a class level. */
public record CreateClassLevelRequest(

        @NotBlank
        @Size(max = 20)
        String name,

        @NotNull
        Integer sortOrder
) {
}
