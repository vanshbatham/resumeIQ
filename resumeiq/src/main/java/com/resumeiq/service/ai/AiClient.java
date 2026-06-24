package com.resumeiq.service.ai;

/**
 * Abstraction over all LLM providers.
 * Swap implementations by changing configuration — callers are unaffected.
 * <p>
 * Contract for generateStructured: the system prompt MUST contain the word "JSON"
 * and define the expected output schema. The response is parsed directly into
 * the provided type — a malformed AI response throws AiServiceException.
 */
public interface AiClient {

    /**
     * Generate a free-text response.
     * Use for outputs that don't need programmatic parsing (e.g., cover letter body).
     */
    String generateText(String systemPrompt, String userPrompt);

    /**
     * Generate a structured response and deserialize it into the provided type.
     * Uses JSON mode — the model is constrained to return valid JSON.
     */
    <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType);
}