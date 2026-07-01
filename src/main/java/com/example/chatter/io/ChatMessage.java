package com.example.chatter.io;


import lombok.Data;

@Data
public class ChatMessage {
    private String senderId;
    private String receiverId;
    private String senderMsg;
    private String seen;
}
