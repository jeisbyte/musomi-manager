package com.musomi.manager.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(

        @NotBlank
        String fullName,

        String email,

        String phone
) {
}