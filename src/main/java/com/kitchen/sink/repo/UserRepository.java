package com.kitchen.sink.repo;

import com.kitchen.sink.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User>  findByEmail(String email);
    boolean existsByEmail(String email);
}