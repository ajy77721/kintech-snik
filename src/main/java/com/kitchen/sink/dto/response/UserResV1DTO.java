package com.kitchen.sink.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.kitchen.sink.enums.UserRole;
import com.kitchen.sink.enums.UserStatus;
import com.kitchen.sink.validation.LowerCaseStringDeserializer;
import com.kitchen.sink.validation.LowerCaseStringSerializer;

import java.time.LocalDateTime;
import java.util.Set;


public record UserResV1DTO(
        String id,
        String name,
        @JsonSerialize(using = LowerCaseStringSerializer.class)
        @JsonDeserialize(using = LowerCaseStringDeserializer.class)
        String email,
        String phoneNumber,
        Set<UserRole> roles,
        LocalDateTime createdTime,
        LocalDateTime lastModifiedTime,
        String createdBy,
        String lastModifiedBy,
        UserStatus status
) {
}