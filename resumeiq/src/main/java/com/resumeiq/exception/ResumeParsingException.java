package com.resumeiq.exception;

public class ResumeParsingException extends RuntimeException {
    public ResumeParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}