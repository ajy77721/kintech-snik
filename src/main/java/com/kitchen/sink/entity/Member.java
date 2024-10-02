package com.kitchen.sink.entity;

import com.kitchen.sink.enums.MemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "members")
@Data
public class Member {
    @Id
    private String id;

    @NotNull
    @Size(min = 1, max = 25)
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    private String name;

    @NotNull
    @NotEmpty
    @Email
    @Indexed(unique = true)
    private String email;

    @NotNull
    @Size(min = 12, max = 12)
    @Digits(fraction = 0, integer = 12)
    @Column(name = "phone_number")
    @Indexed(unique = true)
    private String phoneNumber;
    @NotNull
    private String password;

    @NotNull
    private LocalDateTime createdTime;
    private LocalDateTime lastModifiedTime;
    @NotNull
    private String createdBy;
    private String lastModifiedBy;
    @NotNull
    private MemberStatus status;
    private String approvedBy;
    private LocalDateTime approvedTime;
}
