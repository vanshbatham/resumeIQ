package com.resumeiq.service.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeiq.config.AiProperties;
import com.resumeiq.exception.AiRateLimitException;
import com.resumeiq.exception.AiServiceException;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroqAiClient implements AiClient {

    @Qualifier("groqRestClient")
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AiProperties aiProperties;

    @Override
    @RateLimiter(name = "groq-api")
    public String generateText(String systemPrompt, String userPrompt) {
        log.debug("generateText | model={}", aiProperties.getGroq().getModel());
        ChatRequest request = buildRequest(systemPrompt, userPrompt, false);
        return callGroq(request);
    }

    @Override
    @RateLimiter(name = "groq-api")
    public <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType) {
        log.debug("generateStructured | model={} | responseType={}",
                aiProperties.getGroq().getModel(), responseType.getSimpleName());
        ChatRequest request = buildRequest(systemPrompt, userPrompt, true);
        String json = callGroq(request);
        return parseJson(json, responseType);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private ChatRequest buildRequest(String systemPrompt, String userPrompt, boolean jsonMode) {
        AiProperties.Groq groq = aiProperties.getGroq();
        return new ChatRequest(
                groq.getModel(),
                List.of(
                        new ChatMessage("system", systemPrompt),
                        new ChatMessage("user", userPrompt)
                ),
                groq.getTemperature(),
                groq.getMaxTokens(),
                jsonMode ? new ResponseFormat("json_object") : null
        );
    }

    private String callGroq(ChatRequest request) {
        try {
            ChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(ChatResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new AiServiceException("Empty response received from AI provider.");
            }

            String content = response.choices().getFirst().message().content();
            log.debug("Groq call succeeded | content_length={}", content != null ? content.length() : 0);
            return content;

        } catch (HttpClientErrorException e) {
            int status = e.getStatusCode().value();
            if (status == 401) throw new AiServiceException(
                    "Invalid Groq API key. Set the GROQ_API_KEY environment variable.");
            if (status == 429) throw new AiRateLimitException();
            if (status == 400) throw new AiServiceException(
                    "Bad request to AI provider. Check prompt format. Detail: " + e.getResponseBodyAsString());
            throw new AiServiceException("AI provider error " + status + ": " + e.getStatusText());

        } catch (HttpServerErrorException e) {
            log.warn("Groq server error: {}", e.getStatusCode());
            throw new AiServiceException("AI provider is temporarily unavailable. Please try again shortly.");

        } catch (RestClientException e) {
            log.error("Network error calling Groq: {}", e.getMessage());
            throw new AiServiceException("Network error connecting to AI provider.");
        }
    }

    private <T> T parseJson(String raw, Class<T> responseType) {
        try {
            String json = stripMarkdownFences(raw.strip());
            return objectMapper.readValue(json, responseType);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response: {}", raw);
            throw new AiServiceException(
                    "AI response could not be parsed as JSON. Raw response logged at ERROR level.");
        }
    }

    private String stripMarkdownFences(String text) {
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```[a-z]*\\n?", "");
            if (text.endsWith("```")) text = text.substring(0, text.length() - 3).strip();
        }
        return text;
    }

    // -------------------------------------------------------------------------
    // Internal request / response DTOs (Groq / OpenAI chat completions format)
    // -------------------------------------------------------------------------

    private record ChatRequest(
            String model,
            List<ChatMessage> messages,
            double temperature,
            @JsonProperty("max_tokens") int maxTokens,
            @JsonProperty("response_format") ResponseFormat responseFormat
    ) {
    }

    private record ChatMessage(String role, String content) {
    }

    private record ResponseFormat(String type) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatResponse(List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(ChatMessage message) {
    }
}