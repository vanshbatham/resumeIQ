package com.resumeiq.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AnalyzeRequest(
        @NotNull(message = "resumeId is required")
        UUID resumeId
) {
}