package com.cryptopal.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @NotBlank(message = "current password is required")
        String currentPassword,

        @NotBlank(message = "new password is required")
        @Size(min = 6, max = 100, message = "new password must be at least 6 characters")
        String newPassword
) {
}
