package com.kitchen.sink.config;

import com.kitchen.sink.enums.UserRole;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@ConfigurationProperties(prefix = "master-token-user")
@Configuration
@Data
public class MasterTokenConfig {
    private String name;
    private String password;
    private String email;
    private String phoneNumber;
    private Set<UserRole> roles;
}
