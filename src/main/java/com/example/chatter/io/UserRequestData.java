package com.example.chatter.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequestData {
    private String uid;
    private String name;
    private String language;
    private String code;
    private String url;
}

