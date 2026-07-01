package com.example.chatter.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseData {
    private String id;
    private String uid;
    private String name;
    private String language;
    private String code;
    private String url;
    private long lastSeen;
    private List<String> contacts;
    private List<String> blockedContacts;
}
