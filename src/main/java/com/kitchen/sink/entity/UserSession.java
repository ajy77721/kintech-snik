package com.kitchen.sink.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "user_sessions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {
    @Id
    private String id;
    private String email;
    private String token;
    private LocalDateTime createdTime;
}
