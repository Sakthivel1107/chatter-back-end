package com.example.chatter.repository;

import com.example.chatter.entity.UidEntity;
import com.example.chatter.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class UidsRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    public UidEntity save(UidEntity uidEntity){
        return mongoTemplate.save(uidEntity);
    }

    public boolean check(String uid){
        Query query = new Query();
        query.addCriteria(
                Criteria.where("uid").is(uid)
        );

        UidEntity uidEntity = mongoTemplate.findOne(query, UidEntity.class);
        if(uidEntity == null){
            return true;
        }
        return !(uidEntity.getUid().equals(uid));
    }

    public void deleteUidByUid(String uid){
        Query query = new Query();
        query.addCriteria(
                Criteria.where("uid").is(uid)
        );
        mongoTemplate.remove(query,UidEntity.class);
    }
}
