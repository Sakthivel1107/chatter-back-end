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

    @PostMapping("/auth/google")
    @CrossOrigin("*")
    public ResponseEntity<?> googleLogin(
            @RequestBody GoogleLoginRequest request) {

        try {

            // 1. Verify Google ID token
            GoogleIdTokenVerifier verifier =
                    new GoogleIdTokenVerifier.Builder(
                            new NetHttpTransport(),
                            GsonFactory.getDefaultInstance()
                    )
                            .setAudience(
                                    Collections.singletonList(clientId)
                            )
                            .build();

            GoogleIdToken googleIdToken =
                    verifier.verify(request.getIdToken());

            if (googleIdToken == null) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "message", "Invalid Google ID token"
                        ));
            }

            // 2. Get Google user information
            GoogleIdToken.Payload payload =
                    googleIdToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // 3. Find existing Chatter user
            Optional<UserEntity> optionalUser =
                    userRepository.findByEmail(email);

            UserEntity user;

            if (optionalUser.isPresent()) {

                user = optionalUser.get();

            } else {

                // 4. Create new Google user
                user = userService.createGoogleUser(
                        email,
                        name
                );
            }

            // 5. Load Spring Security user
            UserDetails userDetails =
                    appUserDetailsService
                            .loadUserByUsername(user.getEmail());

            // 6. Generate your Chatter JWT
            String jwtToken =
                    jwtUtil.generateToken(userDetails);

            // 7. Send JWT to React
            return ResponseEntity.ok(
                    Map.of(
                            "email", user.getEmail(),
                            "name", user.getName(),
                            "token", jwtToken
                    )
            );

        } catch (GeneralSecurityException | IOException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Google authentication failed"
                    ));
        }
    }

}
