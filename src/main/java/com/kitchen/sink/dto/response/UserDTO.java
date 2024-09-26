package com.kitchen.sink.dto.response;

import com.kitchen.sink.enums.UserRole;
import com.kitchen.sink.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record UserDTO(
        String id,
        String name,
        String email,
        String phoneNumber,
        Set<UserRole> roles,
        UserStatus status,
        String CreatedBy,
        String UpdatedBy,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
