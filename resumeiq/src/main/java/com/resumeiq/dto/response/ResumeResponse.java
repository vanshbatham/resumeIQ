package com.resumeiq.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResumeResponse(
        UUID id,
        String fileName,
        int parsedTextLength,
        LocalDateTime uploadedAt
) {
}