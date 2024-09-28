package com.kitchen.sink.dto.request;

import com.kitchen.sink.enums.UserRole;
import com.kitchen.sink.enums.UserStatus;
import com.kitchen.sink.validation.CreateGroup;
import com.kitchen.sink.validation.RegisterGroup;
import com.kitchen.sink.validation.UpdateGroup;
import jakarta.validation.constraints.*;

import java.util.Set;

public record UserReqDTO(
        @Null(groups = CreateGroup.class, message = "id must be null for create operation")
        @NotBlank(groups = UpdateGroup.class, message = "id must not be null for Update operation")
        String id,

        @NotBlank(groups = {CreateGroup.class, UpdateGroup.class}, message = "name cannot be blank")
        @NotNull(groups = UpdateGroup.class, message = "name must not be null for Update operation")
        String name,

        @NotBlank(groups = {CreateGroup.class, UpdateGroup.class}, message = "email cannot be blank")
        String email,

        @NotBlank(message = "Phone number is mandatory", groups = {CreateGroup.class, UpdateGroup.class})
        @Size(min = 10, max = 12, message = "Phone number must be between 10 and 12 digits", groups = {CreateGroup.class, UpdateGroup.class})
        @Pattern(regexp = "\\d{10,12}", message = "Phone number must be numeric", groups = {CreateGroup.class, UpdateGroup.class})
        String phoneNumber,

        @NotBlank(groups = {CreateGroup.class}, message = "password cannot be blank")
        @Null(groups = {UpdateGroup.class}, message = "password must be null for Update operation")
        @Size(min = 6, max = 20, message = "password must be between 6 and 20 characters",groups = {CreateGroup.class})
        String password,

        Set<UserRole> roles,
        @NotNull(groups = {CreateGroup.class}, message = "status cannot be null")
        UserStatus status
) {
}
