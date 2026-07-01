package com.example.chatter.service;

import com.example.chatter.entity.UserEntity;
import com.example.chatter.io.ContactData;
import com.example.chatter.io.UserRequest;
import com.example.chatter.io.UserResponse;
import com.example.chatter.io.UserResponseData;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface UserService {
    UserResponse registerUser(UserRequest request);
    String findByUserId();
    UserEntity loggedInUser();
    UserResponseData loadLoggedInUserData();
    UserEntity updateUser(UserEntity user);
    String uploadFile(MultipartFile file);
    void deleteFileByName(String fileName);
    List<UserResponseData> findUserByUid0rName(String input);
    String addContact(String uid);
    ContactData getContact(String senderId,String receiverId);
    List<ContactData> userContactsData();
    String loggedInUserId();
    ContactData convertToContactData(UserEntity userEntity);
    void removeContact(String contactId);
    List<ContactData> getBlockedContactsData();
}
