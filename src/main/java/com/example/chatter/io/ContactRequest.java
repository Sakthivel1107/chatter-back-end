package com.example.chatter.io;

import lombok.Data;

@Data
public class ContactRequest {
    private String name;
    private String email;
    private String message;
}
