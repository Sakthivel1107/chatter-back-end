package com.example.chatter.io;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class GoogleLoginRequest {
    private String idToken;
}
