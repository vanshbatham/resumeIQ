package com.resumeiq.exception;

public class ScannedPdfException extends RuntimeException {
    public ScannedPdfException() {
        super("This PDF appears to be scanned or image-based. " +
                "Please upload a text-based PDF or DOCX file.");
    }
}