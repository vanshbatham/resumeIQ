package com.resumeiq.util;

import org.springframework.stereotype.Component;

@Component
public class TextCleaningUtil {

    public String clean(String rawText) {
        if (rawText == null || rawText.isBlank()) return "";

        return rawText
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "")  // control chars except \n \r \t
                .replaceAll("\\r\\n|\\r", "\n")                               // normalize line endings
                .replaceAll("[ \\t]+", " ")                                    // collapse horizontal whitespace
                .replaceAll("\\n{3,}", "\n\n")                                 // cap consecutive blank lines at 2
                .lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .reduce("", (a, b) -> a.isEmpty() ? b : a + "\n" + b)
                .trim();
    }
}