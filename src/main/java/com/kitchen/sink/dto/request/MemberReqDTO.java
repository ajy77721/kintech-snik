package com.kitchen.sink.dto.request;

import com.kitchen.sink.enums.MemberStatus;
import com.kitchen.sink.validation.CreateGroup;
import com.kitchen.sink.validation.RegisterGroup;
import com.kitchen.sink.validation.UpdateGroup;
import jakarta.validation.constraints.*;

public record MemberReqDTO(

        @NotNull(groups = UpdateGroup.class)
        String id,

        @NotBlank(message = "Name is mandatory", groups = {CreateGroup.class, UpdateGroup.class, RegisterGroup.class})
        @Size(min = 1, max = 25, message = "Name must be between 1 and 25 characters", groups = {CreateGroup.class, UpdateGroup.class, RegisterGroup.class})
        @Pattern(regexp = "[^0-9]*", message = "Name must not contain numbers", groups = {CreateGroup.class, UpdateGroup.class, RegisterGroup.class})
        String name,

        @NotBlank(message = "Email is mandatory", groups = {CreateGroup.class, UpdateGroup.class, RegisterGroup.class})
        @Email(message = "Email should be valid", groups = {CreateGroup.class, UpdateGroup.class, RegisterGroup.class})
        String email,

        @NotBlank(message = "Phone number is mandatory", groups = {CreateGroup.class, UpdateGroup.class, RegisterGroup.class})
        @Size(min = 10, max = 12, message = "Phone number must be between 10 and 12 digits", groups = {CreateGroup.class, UpdateGroup.class, RegisterGroup.class})
        @Pattern(regexp = "\\d{10,12}", message = "Phone number must be numeric", groups = {CreateGroup.class, UpdateGroup.class, RegisterGroup.class})
        String phoneNumber,

        @NotBlank(message = "password  is mandatory", groups = {CreateGroup.class, RegisterGroup.class})
        @Null(message ="password should be null" , groups = {UpdateGroup.class})
        String password,

        @Null(message ="memberStatus should be null" ,groups = RegisterGroup.class)
        @NotNull(message = "memberStatus is mandatory", groups = {CreateGroup.class})
        MemberStatus status
) {
}
