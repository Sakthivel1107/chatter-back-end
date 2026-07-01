package com.example.chatter.repository;

import com.example.chatter.entity.MessageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    public MessageEntity findById(String id){
        return mongoTemplate.findById(id, MessageEntity.class);
    }

    public void deleteById(String id){
        Query query = new Query();
        query.addCriteria(Criteria.where("id").eq(id));
        mongoTemplate.remove(query,MessageEntity.class);
    }

    public MessageEntity save(MessageEntity messageEntity){
        return mongoTemplate.save(messageEntity);
    }

    public List<MessageEntity> getConversation(
            String user1,
            String user2
    ) {

        Criteria criteria = new Criteria().orOperator(
                new Criteria().andOperator(
                        Criteria.where("senderId").is(user1),
                        Criteria.where("receiverId").is(user2),
                        Criteria.where("senderMsg").ne("")
                ),
                new Criteria().andOperator(
                        Criteria.where("senderId").is(user2),
                        Criteria.where("receiverId").is(user1),
                        Criteria.where("receiverMsg").ne(null)
                )
        );

        Query query = new Query(criteria);
        System.out.println(user1 +" "+user2);
        query.with(Sort.by(Sort.Direction.ASC, "timestamp"));
        return mongoTemplate.find(query,MessageEntity.class);
    }

    public void updateStatus(String receiverId,String status){
        Query query = new Query();
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("receiverId").is(receiverId),
                Criteria.where("seen").is("offline")
        );
        query.addCriteria(criteria);
        Update update = new Update();
        update.set("seen", status);

        mongoTemplate.updateMulti(
                query,
                update,
                MessageEntity.class
        );
    }

    public void updateMessagesStatus(String senderId,String receiverId){
        System.out.println("senderId: " +senderId);
        System.out.println("ReceiverId: "+receiverId);
        Query query = new Query();
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("senderId").is(senderId),
                Criteria.where("receiverId").is(receiverId)
        );
        query.addCriteria(criteria);
        Update update = new Update();
        update.set("seen","seen");
        mongoTemplate.updateMulti(query,update,MessageEntity.class);
    }

    public void updateMessage(MessageEntity message){
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(message.getId()));
        Update update = new Update();
        update.set("seen", message.getSeen());
        update.set("receiverMsg",message.getReceiverMsg());
        update.set("senderMsg",message.getSenderMsg());
        mongoTemplate.updateFirst(
                query,
                update,
                MessageEntity.class
        );
    }

    public void deleteAllMessages(String u1,String u2){
        Query query = new Query();
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("senderId").eq(u1),
                Criteria.where("receiverId").eq(u2)
        );
        query.addCriteria(criteria);
        Update update = new Update();
        update.set("senderMsg","");
        mongoTemplate.updateMulti(query,update,MessageEntity.class);
        Query query1 = new Query();
        Criteria criteria1 = new Criteria().andOperator(
                Criteria.where("senderId").eq(u2),
                Criteria.where("receiverId").eq(u1)
        );
        query1.addCriteria(criteria1);
        Update update1 = new Update();
        update1.set("receiverMsg",null);
        mongoTemplate.updateMulti(query1,update1,MessageEntity.class);
    }
}
