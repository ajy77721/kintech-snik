package com.kitchen.sink.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordReqDTO(
        @NotBlank(message = "Id cannot be blank")
        String id,
        @NotBlank(message = "Password cannot be blank")
        String password) {
}
