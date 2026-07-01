package com.example.chatter.controller;

import com.example.chatter.io.ContactRequest;
import com.example.chatter.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/mail")
public class MailController {
    @Autowired
    private EmailService emailService;
    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestBody ContactRequest contactRequest){
        try{
            emailService.sendEmail(contactRequest);
            return ResponseEntity.ok("Message sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Message Sending failed");
        }
    }
}
