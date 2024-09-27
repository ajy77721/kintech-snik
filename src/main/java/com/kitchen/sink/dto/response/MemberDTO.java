package com.kitchen.sink.dto.response;

import com.kitchen.sink.enums.MemberStatus;

import java.time.LocalDateTime;

public record MemberDTO(
        String id,
        String name,
        String email,
        String phoneNumber,
        LocalDateTime createdTime,
        LocalDateTime lastModifiedTime,
        String createdBy,
        String lastModifiedBy,
        MemberStatus status,
        String approvedBy,
        LocalDateTime approvedTime
        ) {
}
