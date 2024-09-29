package com.kitchen.sink.entity;

import com.kitchen.sink.enums.UserRole;
import com.kitchen.sink.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
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

    private String phoneNumber;
    @NotNull
    private Set<UserRole> roles;
    private LocalDateTime createdTime;
    private LocalDateTime lastModifiedTime;
    private String createdBy;
    private String lastModifiedBy;
    private UserStatus status;
}
