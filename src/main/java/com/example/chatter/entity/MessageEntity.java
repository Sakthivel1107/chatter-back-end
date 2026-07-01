package com.example.chatter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "messages")
@Builder
public class MessageEntity {
    @Id
    private String id;
    private String senderMsg;
    private String receiverMsg;
    private String senderId;
    private String receiverId;
    private Long timestamp;
    private String seen;
    private Boolean edited;
}
