package com.resumeiq.service.analysis;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class AtsScorer {

    private static final Set<String> STANDARD_HEADERS = Set.of(
            "experience", "education", "skills", "summary", "projects",
            "certifications", "objective", "profile", "employment", "work history",
            "technical skills", "achievements", "awards"
    );

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(\\+?\\d[\\d\\s\\-().]{7,14}\\d)");

    public double score(String parsedText) {
        String[] lines = parsedText.split("\n");

        double contact = scoreContactInfo(parsedText);      // 0–25
        double structure = scoreSectionHeaders(lines);         // 0–30
        double formatting = scoreFormatting(parsedText, lines); // 0–25
        double length = scoreLength(parsedText);            // 0–20

        return Math.min(100, Math.max(0, contact + structure + formatting + length));
    }

    private double scoreContactInfo(String text) {
        double score = 0;
        if (EMAIL_PATTERN.matcher(text).find()) score += 15;
        if (PHONE_PATTERN.matcher(text).find()) score += 10;
        return score;
    }

    private double scoreSectionHeaders(String[] lines) {
        long matched = STANDARD_HEADERS.stream()
                .filter(header -> Arrays.stream(lines).anyMatch(line -> {
                    String l = line.trim().toLowerCase();
                    return l.length() < 45 && (l.equals(header) || l.startsWith(header));
                }))
                .count();
        return Math.min(30.0, matched * 7.5);
    }

    private double scoreFormatting(String text, String[] lines) {
        double score = 25;

        // table/column artifacts — pipe chars and box-drawing characters
        long artifacts = text.chars()
                .filter(c -> c == '|' || c == '+' || c == 9632 || c == 9642 || c == 9643)
                .count();
        if (artifacts > 10) score -= 10;
        if (artifacts > 30) score -= 10;

        // very short interleaved lines signal column layout
        long shortLines = Arrays.stream(lines)
                .filter(l -> !l.isBlank() && l.trim().length() < 4)
                .count();
        if (shortLines > 5) score -= 5;

        return Math.max(0, score);
    }

    private double scoreLength(String text) {
        int words = text.split("\\s+").length;
        if (words < 150) return 0;
        if (words < 300) return 8;
        if (words <= 1200) return 20;   // optimal range
        if (words <= 2000) return 12;
        return 4;                             // too verbose
    }
}