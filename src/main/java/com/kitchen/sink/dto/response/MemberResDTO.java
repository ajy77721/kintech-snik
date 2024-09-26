package com.kitchen.sink.dto.response;

import com.kitchen.sink.enums.MemberStatus;

public record MemberResDTO (
        String id,
        String name,
        String email,
        String phoneNumber,
        MemberStatus status){
}
