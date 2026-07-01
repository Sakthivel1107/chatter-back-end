package com.example.chatter.io;

import com.example.chatter.entity.MessageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactData {
    private String id;
    private String name;
    private String code;
    private String uid;
    private String language;
    private String url;
    private List<MessageEntity> messages = new ArrayList<>();
    private long lastSeen;
    private boolean online;
    private boolean currentChat=false;
}
