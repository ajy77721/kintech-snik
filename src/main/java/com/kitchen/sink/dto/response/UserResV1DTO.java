package com.kitchen.sink.dto.response;

import com.kitchen.sink.enums.UserRole;
import com.kitchen.sink.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;


public record UserResV1DTO(
        String id,
        String name,
        String email,
        Set<UserRole> roles,
        LocalDateTime createdTime,
        LocalDateTime updatedTime,
        String createdBy,
        String updatedBy,
        UserStatus status
) {}