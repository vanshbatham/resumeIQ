package com.resumeiq.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AnalysisResponse(
        UUID id,
        UUID resumeId,
        double overallScore,
        double atsScore,
        ReadabilityResult readability,
        SectionCompletenessResult sectionCompleteness,
        ActionVerbResult actionVerbs,
        AchievementResult achievements,
        List<GrammarIssue> grammarIssues,
        LocalDateTime analyzedAt
) {
}