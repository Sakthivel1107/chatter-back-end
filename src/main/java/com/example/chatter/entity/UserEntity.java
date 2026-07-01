package com.example.chatter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "users")
public class UserEntity {
    @Id
    private String id;
    private String uid;
    private String name;
    private String email;
    private String password;
    private String url;
    private String language;
    private String code;
    private String provider;
    private Boolean online;
    private long lastSeen;
    private List<String> contacts;
    private List<String> blockedContacts;
}
