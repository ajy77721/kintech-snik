package com.kitchen.sink.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kitchen.sink.enums.UserRole;
import com.kitchen.sink.enums.UserStatus;

import java.util.Set;

public record UserResDTO (
        String id,
        String name,
        String email,
         String phoneNumber,
        Set<UserRole> roles,
        UserStatus status
){
}
