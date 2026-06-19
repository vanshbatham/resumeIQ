package com.resumeiq.dto.response;

import java.util.List;

public record AchievementResult(
        int achievementCount,
        List<String> examples,
        boolean hasQuantifiableImpact,
        double score
) {
}