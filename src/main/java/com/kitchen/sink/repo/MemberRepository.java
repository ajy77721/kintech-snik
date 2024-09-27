package com.kitchen.sink.repo;

import com.kitchen.sink.entity.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, String> {
    boolean existsById(String id);

    void deleteByEmail(String email);

    Optional<Member> findByEmail(String email);
}
