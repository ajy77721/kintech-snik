package com.kitchen.sink.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.kitchen.sink.enums.MemberStatus;
import com.kitchen.sink.validation.LowerCaseStringDeserializer;
import com.kitchen.sink.validation.LowerCaseStringSerializer;

public record MemberResDTO (
        String id,
        String name,
        @JsonSerialize(using = LowerCaseStringSerializer.class)
        @JsonDeserialize(using = LowerCaseStringDeserializer.class)
        String email,
        String phoneNumber,
        MemberStatus status){
}
