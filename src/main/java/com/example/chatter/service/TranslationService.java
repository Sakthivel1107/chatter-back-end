package com.example.chatter.service;

import com.example.chatter.io.TranslationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class TranslationService {

    @Value("${rapid_api_key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://free-google-translator.p.rapidapi.com")
            .build();

    public String translate(String text, String from, String to) {

        TranslationResponse translationResponse = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/external-api/free-google-translator")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .queryParam("query", text)
                        .build())
                .header("x-rapidapi-key", apiKey)
                .header("x-rapidapi-host",
                        "free-google-translator.p.rapidapi.com")
                .header("Content-Type", "application/json")
                .bodyValue(Map.of("translate", "rapidapi"))
                .retrieve()
                .bodyToMono(TranslationResponse.class)
                .block();
        return translationResponse.getTranslation();
    }
}