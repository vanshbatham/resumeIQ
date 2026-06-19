package com.resumeiq.dto.response;

public record ReadabilityResult(
        double fleschReadingEase,
        double gradeLevel,
        String interpretation,
        int wordCount,
        int sentenceCount
) {
}