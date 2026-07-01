package com.example.chatter.service;

import com.example.chatter.config.GitHubConfig;
import com.example.chatter.entity.UidEntity;
import com.example.chatter.io.ContactData;
import com.example.chatter.io.UserRequest;
import com.example.chatter.io.UserResponse;
import com.example.chatter.entity.UserEntity;
import com.example.chatter.io.UserResponseData;
import com.example.chatter.repository.UidsRepository;
import com.example.chatter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.util.*;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UidsRepository uidsRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationFacade authenticationFacade;
    @Autowired
    private GitHubConfig config;
    @Autowired
    private MessageService messageService;
    @Override
    public UserResponse registerUser(UserRequest request) {
        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new RuntimeException("Email already exists");
                });
        UserEntity newUser = convertToEntity(request);
        UidEntity uid = new UidEntity(newUser.getUid());
        uidsRepository.save(uid);
        newUser = userRepository.save(newUser);
        return convertToResponse(newUser);
    }
    private String generateUid(){
        final String CHARACTERS =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final SecureRandom RANDOM = new SecureRandom();
        StringBuilder uid = new StringBuilder("@");
        for(int i=0;i < 10;i++){
            uid.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return uid.toString();
    }
    private UserEntity convertToEntity(UserRequest request){
        return UserEntity.builder()
                .uid(generateUid())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .url("https://raw.githubusercontent.com/Sakthivel1107/image-storage/main/images/defaultImage.png")
                .provider(request.getProvider())
                .language("")
                .code("")
                .online(false)
                .contacts(new ArrayList<>())
                .blockedContacts(new ArrayList<>())
                .build();
    }
    private UserResponseData convertToResponseData(UserEntity user){
        return UserResponseData.builder()
                .id(user.getId())
                .uid(user.getUid())
                .name(user.getName())
                .language(user.getLanguage())
                .code(user.getCode())
                .url(user.getUrl())
                .contacts(user.getContacts())
                .blockedContacts(user.getBlockedContacts())
                .lastSeen(user.getLastSeen())
                .build();
    }
    private UserResponse convertToResponse(UserEntity registeredUser){
        return UserResponse.builder()
                .id(registeredUser.getId())
                .name(registeredUser.getName())
                .email(registeredUser.getEmail())
                .build();
    }
    @Override
    public String findByUserId(){
        String loggedInUserEmail = authenticationFacade.getAuthentication().getName();
        UserEntity loggedInUser = userRepository.findByEmail(loggedInUserEmail).orElseThrow(()-> new UsernameNotFoundException("User Not found"));
        return loggedInUser.getId();
    }

    @Override
    public UserEntity loggedInUser() {
        String loggedInUserEmail = authenticationFacade.getAuthentication().getName();
        return userRepository.findByEmail(loggedInUserEmail).orElseThrow(()-> new UsernameNotFoundException("User Not found"));
    }

    public String loggedInUserId() {
        String loggedInUserEmail = authenticationFacade.getAuthentication().getName();
        Optional<UserEntity> user = userRepository.findByEmail(loggedInUserEmail);
        if(user.isPresent()) {
            return user.get().getId();
        }
        return "user not present";
    }

    @Override
    public UserResponseData loadLoggedInUserData(){
        return convertToResponseData(loggedInUser());
    }

    @Override
    public UserEntity updateUser(UserEntity user) {
        user = userRepository.save(user);
        return user;
    }

    @Override
    public String uploadFile( MultipartFile file) {
        try {
            String filenameExtension = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            String key = UUID.randomUUID().toString() + "." + filenameExtension;
            String base64Content = Base64.getEncoder().encodeToString(file.getBytes());
            String apiUrl = "https://api.github.com/repos/"+config.getUserName()+"/"+config.getRepo()+"/contents/images/image"+key;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization","token "+ config.getToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String,String> body  = new HashMap<>();
            body.put("message","upload image");
            body.put("content",base64Content);
            body.put("branch", config.getBranch());
            HttpEntity<Map<String,String>> request = new HttpEntity<>(body,headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.put(apiUrl,request);
            return "https://raw.githubusercontent.com/"+config.getUserName()+"/"+config.getRepo()+"/"+config.getBranch()+"/images/image"+key;
        }
        catch (Exception e){
            throw new RuntimeException("Image upload failed",e);
        }
    }
    @Override
    public void deleteFileByName(String fileName) {
        try{
            RestTemplate restTemplate = new RestTemplate();
            fileName = fileName.substring(fileName.lastIndexOf('/')+1);
            String filePath = "images/"+fileName;
            String apiUrl = "https://api.github.com/repos/"+config.getUserName()+"/"+config.getRepo()+"/contents/"+filePath;
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization","token "+config.getToken());
            HttpEntity<Void> entity = new HttpEntity<>(httpHeaders);
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            String sha = (String)response.getBody().get("sha");
            String deleteBody = """
                    {
                    "message": "delete image %s",
                    "sha": "%s",
                    "branch": "%s"
                    }
                    """.formatted(fileName,sha, config.getBranch());
            HttpHeaders deleteHeaders = new HttpHeaders();
            deleteHeaders.set("Authorization","token "+config.getToken());
            deleteHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> deleteRequest = new HttpEntity<>(deleteBody, deleteHeaders);
            restTemplate.exchange(
                    apiUrl,
                    HttpMethod.DELETE,
                    deleteRequest,
                    String.class
            );
        }
        catch (HttpClientErrorException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<UserResponseData> findUserByUid0rName(String input) {
        List<UserEntity> userList = userRepository.findByUidOrName(input);
        List<UserResponseData> usersResponselist = new ArrayList<>();
        for (UserEntity userEntity : userList) {
            usersResponselist.add(convertToResponseData(userEntity));
        }
        return usersResponselist;
    }

    @Override
    public ContactData convertToContactData(UserEntity userEntity){
        return ContactData.builder()
                .id(userEntity.getId())
                .name(userEntity.getName())
                .uid(userEntity.getUid())
                .language(userEntity.getLanguage())
                .code(userEntity.getCode())
                .url(userEntity.getUrl())
                .messages(messageService.getMessages(userEntity.getId()))
                .online(userEntity.getOnline()!=null?userEntity.getOnline():false)
                .lastSeen(userEntity.getLastSeen())
                .build();
    }

    @Override
    public void removeContact(String contactId) {
        UserEntity userEntity = loggedInUser();
        userEntity.getContacts().remove(contactId);
        userRepository.save(userEntity);
    }

    @Override
    public List<ContactData> getBlockedContactsData() {
        String userId = loggedInUserId();
        UserEntity entity = loggedInUser();
        List<ContactData> blockedContactsData = new ArrayList<>();
        for(String contactId: entity.getBlockedContacts()){
            blockedContactsData.add(getContact(userId,contactId));
        }
        return blockedContactsData;
    }


    @Override
    public String addContact(String uid) {
        UserEntity user = loggedInUser();
        UserEntity contact = userRepository.findByUid(uid);
        user.getContacts().add(contact.getId());
        userRepository.save(user);
        return "Contact Added successfully";
    }

    @Override
    public ContactData getContact(String u1, String u2) {
        ContactData contactData = convertToContactData(userRepository.findById(u2));
        contactData.setMessages(messageService.getMessagesByIds(u1,u2));
        return contactData;
    }

    @Override
    public List<ContactData> userContactsData() {
        UserResponseData user = loadLoggedInUserData();
        List<ContactData> res = new ArrayList<>();
        String u1 = loggedInUserId();
        for(String u2 : user.getContacts()){
            res.add(getContact(u1,u2));
        }
        return res;
    }
}
