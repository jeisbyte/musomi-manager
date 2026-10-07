package com.musomi.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGuardianRequest(

        @NotBlank
        @Size(max = 200)
        String name,

        @NotBlank
        @Size(max = 50)
        String relationship,

        @NotBlank
        @Size(max = 50)
        String phone,

        @Size(max = 100)
        String email,

        Boolean isPrimary
) {
}
