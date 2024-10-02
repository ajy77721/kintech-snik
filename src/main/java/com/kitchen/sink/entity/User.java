package com.kitchen.sink.entity;

import com.kitchen.sink.enums.UserRole;
import com.kitchen.sink.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;
import java.util.Set;

@Document(collection = "users")
@Data
public class User  {

    @Id
    private String id;
    @NotNull
    private String name;
    @NotNull
    @Indexed(unique = true)
    @Email
    private String email;
    @NotNull
    private String password;
    @Indexed(unique = true)
    @Size(min = 12, max = 12)
    @Digits(fraction = 0, integer = 12)
    @Column(name = "phone_number")
    private String phoneNumber;
    @NotNull
    private Set<UserRole> roles;
    private LocalDateTime createdTime;
    private LocalDateTime lastModifiedTime;
    private String createdBy;
    private String lastModifiedBy;
    @NotNull
    private UserStatus status;
}
