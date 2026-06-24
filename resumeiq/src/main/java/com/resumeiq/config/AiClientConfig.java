package com.resumeiq.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiClientConfig {

    @Bean("groqRestClient")
    public RestClient groqRestClient(AiProperties props) {
        AiProperties.Groq groq = props.getGroq();
        return RestClient.builder()
                .baseUrl(groq.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + groq.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}