package com.example.chatter.controller;

import com.example.chatter.entity.MessageEntity;
import com.example.chatter.entity.UserEntity;
import com.example.chatter.io.ChatMessage;
import com.example.chatter.io.ContactData;
import com.example.chatter.io.Ids;
import com.example.chatter.io.SocketResponse;
import com.example.chatter.repository.UserRepository;
import com.example.chatter.service.MessageService;
import com.example.chatter.service.PresenceService;
import com.example.chatter.service.TranslationService;
import com.example.chatter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin("*")
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final PresenceService presenceService;
    private final TranslationService translationService;
    private final UserService userService;

    @MessageMapping("/chat")
    public void sendMessage(ChatMessage message) {
        String senderId = message.getSenderId();
        String receiverId = message.getReceiverId();
        UserEntity receiver = userRepository.findById(receiverId);
        String senderCode = userRepository.findById(senderId).getCode();
        String receiverCode = userRepository.findById(receiverId).getCode();
        String receiverMsg;
        if(senderCode.equals(receiverCode)){
            receiverMsg = message.getSenderMsg();
        }else {
            receiverMsg = translationService.translate(message.getSenderMsg(),senderCode,receiverCode);
        }
        MessageEntity entity = MessageEntity.builder()
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .senderMsg(message.getSenderMsg())
                .receiverMsg(receiverMsg)
                .timestamp(System.currentTimeMillis())
                .seen(message.getSeen())
                .edited(false)
                .build();
        if( (!receiver.getBlockedContacts().isEmpty()) && receiver.getBlockedContacts().contains(senderId)){
            entity.setReceiverMsg(null);
            messageService.createMessage(entity);
            messagingTemplate.convertAndSend(
                    "/queue/" + senderId,
                    new SocketResponse("CREATED",entity)
            );
            return;
        }

        entity = messageService.createMessage(entity);
        if(!receiver.getContacts().contains(senderId)){
            receiver.getContacts().add(senderId);
            userRepository.save(receiver);
            ContactData contactData = userService.getContact(receiverId,senderId);
            List<MessageEntity> messages = contactData.getMessages();
            contactData.setMessages(messages);
            messagingTemplate.convertAndSend(
                    "/queue/" + receiverId,
                    new SocketResponse("LOAD",contactData)
            );
            messagingTemplate.convertAndSend(
                    "/queue/" + senderId,
                    new SocketResponse("CREATED",null)
            );
            return;
        }
        messagingTemplate.convertAndSend(
                "/queue/" + senderId,
                new SocketResponse("CREATED",entity)
        );
        messagingTemplate.convertAndSend(
                "/queue/" + receiverId,
                new SocketResponse("MESSAGE",entity)
        );
    }
    @MessageMapping("/seen")
    public void seen(Ids ids){
        UserEntity receiver = userRepository.findById(ids.getSenderId());
        if(!receiver.getBlockedContacts().isEmpty() && receiver.getBlockedContacts().contains(ids.getReceiverId()))
            return;
        messageService.updateMessagesStatus(ids.getSenderId(),ids.getReceiverId());
        messagingTemplate.convertAndSend(
                "/queue/"+ ids.getSenderId(),
                new SocketResponse("SEEN",ids.getReceiverId())
        );
    }
    @MessageMapping("/off")
    public void leaved(Ids ids){
        UserEntity receiver = userRepository.findById(ids.getSenderId());
        if(!receiver.getBlockedContacts().isEmpty() && receiver.getBlockedContacts().contains(ids.getReceiverId()))
            return;
        messagingTemplate.convertAndSend("/queue/"+ids.getSenderId(),new SocketResponse("UNSEEN",ids.getReceiverId()));
    }
    @MessageMapping("/update")
    public void update(MessageEntity message){
        MessageEntity orgMsg = messageService.findById(message.getId());
        if(orgMsg.getReceiverMsg() == null || orgMsg.getReceiverMsg().isEmpty()){
            MessageEntity entity = MessageEntity.builder()
                    .id(message.getId())
                    .senderId(message.getSenderId())
                    .receiverId(orgMsg.getReceiverId())
                    .senderMsg(message.getSenderMsg())
                    .receiverMsg(orgMsg.getReceiverMsg())
                    .timestamp(message.getTimestamp())
                    .seen(message.getSeen())
                    .edited(true)
                    .build();
            messageService.createMessage(entity);
            messagingTemplate.convertAndSend("/queue/"+message.getSenderId(),new SocketResponse("UPDATED",entity));
            return;
        }
        String senderId = message.getSenderId();
        String receiverId = message.getReceiverId();
        String senderCode = userRepository.findById(senderId).getCode();
        String receiverCode = userRepository.findById(receiverId).getCode();
        String receiverMsg;
        if(senderCode.equals(receiverCode)){
            receiverMsg = message.getSenderMsg();
        }else {
            receiverMsg = translationService.translate(message.getSenderMsg(),senderCode,receiverCode);
        }
        MessageEntity entity = MessageEntity.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .senderMsg(message.getSenderMsg())
                .receiverMsg(receiverMsg)
                .timestamp(message.getTimestamp())
                .seen(message.getSeen())
                .edited(true)
                .build();

        messageService.updateMsg(entity);
        messagingTemplate.convertAndSend("/queue/"+receiverId,new SocketResponse("UPDATE",entity));
        messagingTemplate.convertAndSend("/queue/"+senderId,new SocketResponse("UPDATED",entity));
    }

    @MessageMapping("/deleteForEveryone")
    public void deleteForEveryone(String id){
        MessageEntity entity = messageService.findById(id);
        entity.setSenderMsg("");
        messageService.createMessage(entity);
        if(entity.getReceiverMsg()!=null){
            entity.setReceiverMsg("");
            messagingTemplate.convertAndSend("/queue/"+entity.getReceiverId(),new SocketResponse("DELETEFOREVERYONE",entity));
        }
        entity.setReceiverMsg("");
        messagingTemplate.convertAndSend("/queue/"+entity.getSenderId(),new SocketResponse("DELETEDFOREVERYONE",entity));
    }

    @MessageMapping("/deleteForMeFromSender")
    public void deleteForMeFromSender(String id){
        MessageEntity entity = messageService.findById(id);
        entity.setSenderMsg("");
        if(entity.getReceiverMsg() == null || entity.getReceiverMsg().isEmpty())
        {
            messageService.deleteById(id);
        }
        else{
            messageService.createMessage(entity);
        }
        messagingTemplate.convertAndSend("/queue/"+entity.getSenderId(),new SocketResponse("DELETEDFORMEFROMSENDER",entity));
    }

    @MessageMapping("/deleteForMeFromReceiver")
    public void deleteForMeFromReceiver(String id){
        MessageEntity entity = messageService.findById(id);
        entity.setReceiverMsg(null);
        if(entity.getSenderMsg().isEmpty()){
            messageService.deleteById(id);
        }
        else{
            messageService.createMessage(entity);
        }
        messagingTemplate.convertAndSend("/queue/"+entity.getReceiverId(),new SocketResponse("DELETEDFORMEFROMRECEIVER",entity));
    }
}