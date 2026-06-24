package com.resumeiq.exception;

public class AiRateLimitException extends RuntimeException {

    public AiRateLimitException() {
        super("AI service is temporarily rate-limited. Please wait a moment and try again.");
    }
}