package com.kitchen.sink.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordReqDTO(
        @NotBlank(message = "Id cannot be blank")
        String id,
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 6, max = 20, message = "password must be between 6 and 20 characters")
        String password) {
}
