package com.example.chatter.repository;

import com.example.chatter.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    public Optional<UserEntity> findByEmail(String email){
        Query query = new Query(Criteria.where("email").is(email));
        return Optional.ofNullable(mongoTemplate.findOne(query, UserEntity.class));
    }
    public UserEntity save(UserEntity user){
        return mongoTemplate.save(user);
    }
    public UserEntity findById(String id){
        return mongoTemplate.findById(id,UserEntity.class);
    }
    public UserEntity findByUid(String value) {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("uid").is(value)
        );

        return mongoTemplate.findOne(query, UserEntity.class);
    }

    public List<UserEntity> findByUidOrName(String value) {
        Query query = new Query();
        query.addCriteria(
                new Criteria().orOperator(
                        Criteria.where("uid").is(value),
                        Criteria.where("name").is(value)
                )
        );

        return mongoTemplate.find(query, UserEntity.class);
    }
}

