package com.resumeiq.exception;

public class UnsupportedFileTypeException extends RuntimeException {
    public UnsupportedFileTypeException(String contentType) {
        super("Unsupported file type: '" + contentType + "'. Only PDF and DOCX files are accepted.");
    }
}