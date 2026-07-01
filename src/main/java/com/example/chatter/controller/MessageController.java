package com.example.chatter.controller;


import com.example.chatter.entity.MessageEntity;
import com.example.chatter.entity.UserEntity;
import com.example.chatter.repository.UserRepository;
import com.example.chatter.service.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@AllArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final UserRepository userRepository;

    @GetMapping("/getMessages")
    public ResponseEntity<?> getMessages(@RequestParam String receiverId){
        try{
            return ResponseEntity.ok(messageService.getMessages(receiverId));
        } catch (Exception e) {
            {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while fetching messages");
            }
        }
    }

}
