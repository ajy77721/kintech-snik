package com.kitchen.sink.repo;

import com.kitchen.sink.config.MasterTokenConfig;
import com.kitchen.sink.entity.Member;
import com.kitchen.sink.entity.User;
import com.kitchen.sink.enums.UserStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MongoInitializer {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MasterTokenConfig masterTokenConfig;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        mongoTemplate.indexOps(User.class)
                .ensureIndex(new Index()
                        .on("email", Sort.Direction.ASC)
                        .collation(Collation.of("en").strength(Collation.ComparisonLevel.secondary())));
        mongoTemplate.indexOps(Member.class)
                .ensureIndex(new Index()
                        .on("email", Sort.Direction.ASC)
                        .collation(Collation.of("en").strength(Collation.ComparisonLevel.secondary())));

        User user = fetchUserByEmail(masterTokenConfig.getEmail());
        if (user == null) {
            user = new User();
        }
        user.setName(masterTokenConfig.getName());
        user.setEmail(masterTokenConfig.getEmail());
        user.setPassword(passwordEncoder.encode(masterTokenConfig.getPassword()));
        user.setRoles(masterTokenConfig.getRoles());
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());
        user.setCreatedBy("system");
        user.setUpdatedBy("system");
        mongoTemplate.save(user);
    }

    private User fetchUserByEmail(String email) {
        Query query = new Query();
        query.addCriteria(Criteria.where("email").is(email));
        return mongoTemplate.findOne(query.collation(Collation.of("en").strength(Collation.ComparisonLevel.secondary())), User.class);
    }
}
