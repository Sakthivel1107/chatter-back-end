package com.example.chatter.service;

import com.example.chatter.io.ContactRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService{
    @Value("${RESEND_API_KEY}")
    private String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    @Override
    public void sendEmail(ContactRequest request) throws Exception {
        try{
            String url = "https://api.resend.com/emails";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("from", "Chatter <onboarding@resend.dev>");
            body.put("to", List.of("sakthivel62628@gmail.com"));
            body.put("subject", "Query from Chatter user");
            body.put("text",
                    "Name: " + request.getName() + "\n" +
                            "Email: " + request.getEmail() + "\n\n" +
                            "Message:\n" + request.getMessage()
            );

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            throw new Exception("Email sending failed: " + e.getMessage());
        }
    }
}
