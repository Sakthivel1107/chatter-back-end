package com.example.chatter.service;

import com.example.chatter.entity.MessageEntity;
import com.example.chatter.entity.UserEntity;
import com.example.chatter.repository.MessageRepository;
import com.example.chatter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MessageServiceImpl implements MessageService{
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Override
    public MessageEntity findById(String id) {
        return messageRepository.findById(id);
    }

    @Override
    public void deleteById(String id) {
        messageRepository.deleteById(id);
    }

    @Override
    public MessageEntity createMessage(MessageEntity messageEntity) {
        return messageRepository.save(messageEntity);
    }

    @Override
    public List<MessageEntity> getMessages(String u2) {
        try{
            String loggedInUserEmail = authenticationFacade.getAuthentication().getName();
            Optional<UserEntity> user = userRepository.findByEmail(loggedInUserEmail);
            String u1 = user.get().getId();
            return messageRepository.getConversation(u1, u2);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<MessageEntity> getMessagesByIds(String u1, String u2) {
            return messageRepository.getConversation(u1,u2);
    }

    @Override
    public void updateMessagesStatus(String senderId, String receiverId) {
        messageRepository.updateMessagesStatus(senderId,receiverId);
    }

    @Override
    public void updateMsg(MessageEntity messageEntity) {
        messageRepository.updateMessage(messageEntity);
    }

    @Override
    public void deleteAllMessages(String u1, String u2) {
        messageRepository.deleteAllMessages(u1,u2);
    }
}
