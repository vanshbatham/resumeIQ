package com.resumeiq.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResumeUploadResponse(
        UUID id,
        String fileName,
        String storagePath,
        int parsedTextLength,
        String parsedTextPreview,
        LocalDateTime uploadedAt
) {
}