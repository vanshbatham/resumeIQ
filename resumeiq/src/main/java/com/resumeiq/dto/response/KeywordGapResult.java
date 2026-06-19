package com.resumeiq.dto.response;

import java.util.List;

public record KeywordGapResult(
        List<String> matchedKeywords,
        List<String> missingKeywords,
        List<String> resumeKeywords,
        double matchScore
) {
}