package com.musomi.manager.dto.request;

import com.musomi.manager.entity.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank
        @Size(max = 100)
        String username,

        @NotBlank
        @Size(min = 8)
        String password,

        @NotBlank
        String fullName,

        String email,

        String phone,

        @NotNull
        Role role
) {
}