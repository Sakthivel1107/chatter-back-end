package com.example.chatter.service;

import com.example.chatter.entity.MessageEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MessageService {
    MessageEntity findById(String id);
    void deleteById(String id);
    MessageEntity createMessage(MessageEntity messageEntity);
    List<MessageEntity> getMessages(String u2);
    List<MessageEntity> getMessagesByIds(String u1,String u2);
    void updateMessagesStatus(String senderId,String receiverId);
    void updateMsg(MessageEntity messageEntity);
    void deleteAllMessages(String u1,String u2);
}
