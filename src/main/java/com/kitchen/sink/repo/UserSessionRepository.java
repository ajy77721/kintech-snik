package com.kitchen.sink.repo;

import com.kitchen.sink.entity.UserSession;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserSessionRepository extends MongoRepository<UserSession, String> {
    UserSession findByUsername(String username);
    void deleteByToken(String jwt);
}