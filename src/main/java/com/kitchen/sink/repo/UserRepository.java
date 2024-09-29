package com.kitchen.sink.repo;

import com.kitchen.sink.entity.User;
import com.kitchen.sink.validation.LowerString;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(@LowerString String email);

    boolean existsByEmail(@LowerString String email);
}