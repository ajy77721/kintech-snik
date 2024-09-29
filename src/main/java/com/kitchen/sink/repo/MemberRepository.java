package com.kitchen.sink.repo;

import com.kitchen.sink.entity.Member;
import com.kitchen.sink.aspect.LowerString;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, String> {
    boolean existsById(String id);

    void deleteByEmail(@Param("email") @LowerString String email);

    Optional<Member> findByEmail(@Param("email")  @LowerString String email);

    boolean existsByEmail(@Param("email")  @LowerString String email);
}