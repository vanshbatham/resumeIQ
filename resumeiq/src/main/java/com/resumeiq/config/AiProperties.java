package com.resumeiq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
@Getter
@Setter
public class AiProperties {

    private String provider = "groq";
    private Groq groq = new Groq();

    @Getter
    @Setter
    public static class Groq {
        private String apiKey = "";
        private String model = "llama-3.3-70b-versatile";
        private String baseUrl = "https://api.groq.com/openai/v1";
        private double temperature = 0.1;
        private int maxTokens = 2000;
    }
}