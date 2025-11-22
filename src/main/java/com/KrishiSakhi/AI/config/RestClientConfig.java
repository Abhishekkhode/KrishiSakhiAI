package com.KrishiSakhi.AI.config;

// In Main Backend App: RestClientConfig.java

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    // Define the base URL for your GenAI service
    private static final String AI_SERVICE_URL = "http://localhost:9090/api/schemes";

    @Bean
    public RestClient aiRestClient() {
        return RestClient.builder()
                .baseUrl(AI_SERVICE_URL)
                .build();
    }
}