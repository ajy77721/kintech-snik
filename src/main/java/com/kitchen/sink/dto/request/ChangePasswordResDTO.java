package com.kitchen.sink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordResDTO(
        @NotBlank(message = "currentPassword cannot be blank")
        @Size(min = 6, max = 20, message = "currentPassword must be between 8 and 20 characters")
        String currentPassword,
        @NotBlank(message = "newPassword cannot be blank")
        @Size(min = 6, max = 20, message = "newPassword must be between 8 and 20 characters")
        String newPassword) {
}
