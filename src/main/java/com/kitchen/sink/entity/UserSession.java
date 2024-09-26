package com.kitchen.sink.entity;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "user_sessions")
@Data
@Builder
public class UserSession {

    @Id
    private String id;
    private String username;
    private String token;
    private LocalDateTime createdTime;
}
