package com.example.chatter.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class GitHubConfig {
    @Value("${github.username}")
    private String userName;
    @Value("${github.repo}")
    private String repo;
    @Value("${github.branch}")
    private String branch;
    @Value("${github.token}")
    private String token;
}
