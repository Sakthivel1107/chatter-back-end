package com.example.chatter.service;


import com.example.chatter.io.ContactRequest;

public interface EmailService {
    void sendEmail(ContactRequest request) throws Exception;
}
