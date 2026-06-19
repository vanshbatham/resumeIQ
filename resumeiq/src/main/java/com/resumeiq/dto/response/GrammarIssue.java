package com.resumeiq.dto.response;

public record GrammarIssue(
        String type,
        String text,
        String suggestion
) {
}