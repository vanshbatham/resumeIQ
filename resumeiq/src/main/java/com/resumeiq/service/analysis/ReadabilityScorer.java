package com.resumeiq.service.analysis;

import com.resumeiq.dto.response.ReadabilityResult;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Component
public class ReadabilityScorer {

    private static final Set<String> ABBREVIATIONS = Set.of(
            "mr", "mrs", "ms", "dr", "prof", "sr", "jr", "vs", "etc", "inc", "ltd", "corp", "co"
    );

    public ReadabilityResult score(String text) {
        if (text == null || text.isBlank()) {
            return new ReadabilityResult(0, 0, "No content to analyze", 0, 0);
        }

        String[] words = tokenizeWords(text);
        int wordCount = words.length;
        int sentenceCount = countSentences(text);
        int syllableCount = Arrays.stream(words).mapToInt(this::countSyllables).sum();

        if (wordCount == 0 || sentenceCount == 0) {
            return new ReadabilityResult(0, 0, "Insufficient content", wordCount, sentenceCount);
        }

        double wordsPerSentence = (double) wordCount / sentenceCount;
        double syllablesPerWord = (double) syllableCount / wordCount;

        double flesch = 206.835 - 1.015 * wordsPerSentence - 84.6 * syllablesPerWord;
        double grade = 0.39 * wordsPerSentence + 11.8 * syllablesPerWord - 15.59;

        flesch = Math.round(Math.max(0, Math.min(100, flesch)) * 10.0) / 10.0;
        grade = Math.round(Math.max(1, grade) * 10.0) / 10.0;

        return new ReadabilityResult(flesch, grade, interpret(flesch), wordCount, sentenceCount);
    }

    private String[] tokenizeWords(String text) {
        return Arrays.stream(text.split("[\\s\\n]+"))
                .map(w -> w.replaceAll("[^a-zA-Z]", ""))
                .filter(w -> !w.isEmpty())
                .toArray(String[]::new);
    }

    private int countSentences(String text) {
        String[] fragments = text.split("[.!?]+");
        int count = 0;
        for (String fragment : fragments) {
            String trimmed = fragment.trim();
            if (trimmed.length() > 5) {
                String[] parts = trimmed.split("\\s+");
                String lastWord = parts[parts.length - 1].toLowerCase().replaceAll("[^a-z]", "");
                if (!ABBREVIATIONS.contains(lastWord)) count++;
            }
        }
        long bulletLines = Arrays.stream(text.split("\n"))
                .filter(line -> line.trim().matches("^[•\\-*►▸◦‣].*") && line.trim().length() > 10)
                .count();
        return Math.max(1, count + (int) (bulletLines / 2));
    }

    private int countSyllables(String word) {
        word = word.toLowerCase().replaceAll("[^a-z]", "");
        if (word.isEmpty()) return 0;

        int count = 0;
        boolean prev = false;

        for (char c : word.toCharArray()) {
            boolean vowel = "aeiouy".indexOf(c) >= 0;
            if (vowel && !prev) count++;
            prev = vowel;
        }
        if (word.endsWith("e") && count > 1) count--;
        if (word.endsWith("le") && word.length() > 2 && !("aeiouy".indexOf(word.charAt(word.length() - 3)) >= 0))
            count++;

        return Math.max(1, count);
    }

    private String interpret(double score) {
        if (score >= 80) return "Very easy to read";
        if (score >= 70) return "Easy to read";
        if (score >= 60) return "Standard — appropriate for a resume";
        if (score >= 50) return "Fairly difficult — consider simplifying language";
        if (score >= 30) return "Difficult — simplify sentence structure";
        return "Very difficult — significant simplification needed";
    }
}