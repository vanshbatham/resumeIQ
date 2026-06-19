package com.resumeiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record KeywordGapRequest(
        @NotNull(message = "resumeId is required")
        UUID resumeId,

        @NotBlank(message = "Job description text is required")
        @Size(min = 50, message = "Job description must be at least 50 characters")
        String jobDescriptionText
) {
}