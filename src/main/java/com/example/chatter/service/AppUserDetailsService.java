package com.example.chatter.service;

import com.example.chatter.entity.UserEntity;
import com.example.chatter.repository.UserRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
            return new User(user.getEmail(), user.getPassword(), Collections.emptyList());
    }
}
