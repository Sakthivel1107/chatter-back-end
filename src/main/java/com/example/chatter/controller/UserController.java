package com.example.chatter.controller;

import com.example.chatter.entity.UidEntity;
import com.example.chatter.entity.UserEntity;
import com.example.chatter.io.*;
import com.example.chatter.repository.UidsRepository;
import com.example.chatter.repository.UserRepository;
import com.example.chatter.service.MessageService;
import com.example.chatter.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserService userService;
    private final UidsRepository uidsRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final UserRepository userRepository;
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody UserRequest request){
        return userService.registerUser(request);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UserRequestData userRequestData){
        try{
            UserEntity user = userService.loggedInUser();
            user.setUrl(userRequestData.getUrl());
            user.setName(userRequestData.getName());
            user.setUid(userRequestData.getUid());
            user.setLanguage(userRequestData.getLanguage());
            user.setCode(userRequestData.getCode());
            user = userService.updateUser(user);
            List<String> contactsId = user.getContacts();
            SocketResponse response = new SocketResponse("USERUPDATE",userService.convertToContactData(user));
            for(String contactId : contactsId){
                messagingTemplate.convertAndSend(
                        "/queue/"+contactId,response
                );
            }
            return ResponseEntity.ok(user);
        }
        catch(Exception e){
            throw new RuntimeException("error while updating user");
        }
    }
    @PutMapping("/updateImage")
    public ResponseEntity<String> updateUserData(@RequestParam("file") MultipartFile file) throws Exception {
        try{
            UserEntity user = userService.loggedInUser();
            String imageUrl = userService.uploadFile(file);
            if(!user.getUrl().equals("https://raw.githubusercontent.com/Sakthivel1107/image-storage/main/images/defaultImage.png"))
                userService.deleteFileByName(user.getUrl());
            user.setUrl(imageUrl);
            user = userService.updateUser(user);
            List<String> contactsId = user.getContacts();
            SocketResponse response = new SocketResponse("USERUPDATE",userService.convertToContactData(user));
            for(String contactId : contactsId){
                messagingTemplate.convertAndSend(
                        "/queue/"+contactId,response
                );
            }
            return ResponseEntity.ok(user.getUrl());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user");
        }
    }

    @GetMapping("/getUser")
    public ResponseEntity<?> searchUser(@RequestParam String input){
        try{
            return ResponseEntity.ok(userService.findUserByUid0rName(input));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User not found");
        }
    }


    @GetMapping("/user")
    public ResponseEntity<?> getUser(){
        try{
            return ResponseEntity.ok(userService.loadLoggedInUserData());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while retrieving user");
        }
    }

    @GetMapping("/loggedInUserContacts")
    public ResponseEntity<?> loggedInUserContacts(){
        try{
            return ResponseEntity.ok(userService.userContactsData());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while getting contacts data");
        }
    }

    @PostMapping("/addContact")
    public ResponseEntity<String> addContact(@RequestBody Map<String,String> contact){
        try{
            return ResponseEntity.ok(userService.addContact(contact.get("uid")));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while adding contact");
        }
    }

    @GetMapping("/verifyUid")
    public ResponseEntity<Boolean> verifyUid(@RequestParam String uid){
        try {
            boolean result = uidsRepository.check(uid);
            if(result){
                UidEntity uidEntity = new UidEntity(uid);
                uidsRepository.deleteUidByUid(uid);
                uidsRepository.save(uidEntity);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @GetMapping("/getById")
    public ResponseEntity<?> getById(@RequestParam String id){
        try{
            return ResponseEntity.ok(userService.getContact(userService.loggedInUserId(),id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PutMapping("/removeContact")
    public ResponseEntity<String> removeContact(@RequestParam String contactId){
        try {
            userService.removeContact(contactId);
            return ResponseEntity.ok("Contact Removed");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while removing contact");
        }
    }

    @PutMapping("/deleteAllMessages")
    public ResponseEntity<String> removeMessages(@RequestParam String contactId){
        try{
            messageService.deleteAllMessages(userService.loggedInUserId(),contactId);
            return ResponseEntity.ok("Messages Deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while deleting contact messages");
        }
    }

    @PutMapping("/deleteContactAndMessages")
    public ResponseEntity<String> deleteContactAndMessages(@RequestParam String contactId){
        try{
            messageService.deleteAllMessages(userService.loggedInUserId(),contactId);
            userService.removeContact(contactId);
            return ResponseEntity.ok("Contact and Messages Deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while deleting contact and messages");
        }
    }

    @PutMapping("/blockContact")
    public ResponseEntity<String> blockContact(@RequestParam String contactId){
        try{
            UserEntity user = userService.loggedInUser();
            user.getContacts().remove(contactId);
            user.getBlockedContacts().add(contactId);
            userRepository.save(user);
            return ResponseEntity.ok("Blocked Successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while blocking contact");
        }
    }

    @PutMapping("/unblockContact")
    public ResponseEntity<?> unblockContact(@RequestParam String contactId){
        try{
            UserEntity user = userService.loggedInUser();
            user.getBlockedContacts().remove(contactId);
            user.getContacts().add(contactId);
            userRepository.save(user);
            return ResponseEntity.ok(userService.getContact(userService.loggedInUserId(),contactId).getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while unblocking");
        }
    }

    @GetMapping("/getBlockedContactsData")
    public ResponseEntity<?> getBlockedContactsData(){
        try{
            return ResponseEntity.ok(userService.getBlockedContactsData());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while getting blocked contacts data");
        }
    }

}
