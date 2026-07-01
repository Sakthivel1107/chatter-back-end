package com.example.chatter.io;

import lombok.Data;

@Data
public class TranslationResponse {
    private int status;
    private String query;
    private String business_message;
    private String translateTo;
    private String translation;
}
