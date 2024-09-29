package com.kitchen.sink.repo;

import com.kitchen.sink.entity.UserSession;
import com.kitchen.sink.aspect.LowerString;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserSessionRepository extends MongoRepository<UserSession, String> {
    Optional<UserSession> findByEmail(  @LowerString String username);
    void deleteByToken( @LowerString String jwt);
}