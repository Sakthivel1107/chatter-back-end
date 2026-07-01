package com.example.chatter.controller;

import com.example.chatter.entity.UserEntity;
import com.example.chatter.io.GoogleLoginRequest;
import com.example.chatter.io.UserRequest;
import com.example.chatter.repository.UserRepository;
import com.example.chatter.service.TranslationService;
import com.example.chatter.util.JwtUtil;
import com.example.chatter.io.AuthenticationRequest;
import com.example.chatter.io.AuthenticationResponse;
import com.example.chatter.service.AppUserDetailsService;
import com.example.chatter.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserRepository userRepository;
    private final TranslationService translationService;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @PostMapping("/login")
    @CrossOrigin("*")
    public AuthenticationResponse login(@RequestBody AuthenticationRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
        final UserDetails userDetails = appUserDetailsService.loadUserByUsername(request.getEmail());
        final String jwtToken = jwtUtil.generateToken(userDetails);
        return new AuthenticationResponse(request.getEmail(), jwtToken);
    }

    @GetMapping("/validateToken")
    @CrossOrigin("*")
    public ResponseEntity<String> validateToken(){
        return ResponseEntity.ok("Token is valid");
    }

    @PostMapping("/google-login")
    @CrossOrigin("*")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request){
        try{
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance()).setAudience(Collections.singletonList(clientId)).build();
            GoogleIdToken googleIdToken = verifier.verify(request.getIdToken());

            if(googleIdToken == null){
                return ResponseEntity.badRequest().body("Invlaid Google token");
            }
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String name = (String)payload.get("name");
            Optional<UserEntity> optionalUser = userRepository.findByEmail(email);
            UserEntity user;
            if(optionalUser.isPresent()){
             user = optionalUser.get();
            }else {
                user = new UserEntity();
                user.setEmail(email);
                user.setName(name);
                user.setPassword(null);
                user.setProvider("GOOGLE");
                userRepository.save(user);
            }
            final UserDetails userDetails = appUserDetailsService.loadUserByUsername(user.getEmail());
            final String jwtToken = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(Map.of(
                    "email",email,
                    "token",jwtToken
            ));
        }
        catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
