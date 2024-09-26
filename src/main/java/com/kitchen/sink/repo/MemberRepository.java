package com.kitchen.sink.repo;

import com.kitchen.sink.entity.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MemberRepository extends MongoRepository<Member, String> {
    boolean existsById(String id);
}
