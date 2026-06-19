package com.resumeiq.service.analysis;

import com.resumeiq.dto.response.AchievementResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class AchievementDetector {

    private static final List<Pattern> PATTERNS = List.of(
            Pattern.compile("\\d+(\\.\\d+)?\\s*%"),
            Pattern.compile("\\$\\s*[\\d,]+\\.?\\d*[KkMmBb]?"),
            Pattern.compile("₹\\s*[\\d,]+\\.?\\d*[KkMmBb]?"),
            Pattern.compile("\\b\\d+[KkMmBb]\\b"),
            Pattern.compile(
                    "\\b\\d+\\s+(users?|customers?|clients?|employees?|engineers?|team members?|people|stakeholders)\\b",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile(
                    "\\b(increased|decreased|improved|reduced|grew|cut|boosted|saved|eliminated|generated|achieved)\\b.{0,60}\\d+",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b\\d+\\+?\\s+(years?|months?|weeks?)\\b",
                    Pattern.CASE_INSENSITIVE)
    );

    public AchievementResult detect(String parsedText) {
        List<String> examples = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String line : parsedText.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.length() < 10 || seen.contains(trimmed)) continue;

            boolean matched = PATTERNS.stream().anyMatch(p -> p.matcher(trimmed).find());
            if (matched) {
                String display = trimmed.length() > 120 ? trimmed.substring(0, 120) + "..." : trimmed;
                examples.add(display);
                seen.add(trimmed);
                if (examples.size() >= 8) break;
            }
        }

        int count = examples.size();
        double score = count == 0 ? 10 : count == 1 ? 40 : count == 2 ? 60
                                                           : count == 3 ? 75 : count <= 5 ? 90 : 100;

        return new AchievementResult(count, examples, count > 0, score);
    }
}