package com.resumeiq.dto.response;

import java.util.List;

public record ActionVerbResult(
        int totalBullets,
        int strongVerbCount,
        int weakVerbCount,
        List<String> weakBullets,
        double score
) {
}