package com.kitchen.sink.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.kitchen.sink.enums.UserRole;
import com.kitchen.sink.enums.UserStatus;
import com.kitchen.sink.validation.CreateGroup;
import com.kitchen.sink.validation.LowerCaseStringDeserializer;
import com.kitchen.sink.validation.LowerCaseStringSerializer;
import com.kitchen.sink.validation.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.Set;

public record UserDTO(
        @Null(groups = CreateGroup.class, message = "id must be null for create operation")
        @NotBlank(groups = UpdateGroup.class, message = "id must not be null for Update operation")
        String id,

        @NotBlank(groups = {CreateGroup.class, UpdateGroup.class}, message = "name cannot be blank")
        @NotNull(groups = UpdateGroup.class, message = "name must not be null for Update operation")
        String name,

        @NotBlank(groups = {CreateGroup.class, UpdateGroup.class}, message = "email cannot be blank")
        @JsonSerialize(using = LowerCaseStringSerializer.class)
        @JsonDeserialize(using = LowerCaseStringDeserializer.class)
        String email,

        @NotBlank(groups = {CreateGroup.class, UpdateGroup.class}, message = "password cannot be blank")
        String password,

        Set<UserRole> roles,
        UserStatus  status
) {
}
