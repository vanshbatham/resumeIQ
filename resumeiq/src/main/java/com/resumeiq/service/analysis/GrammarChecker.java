package com.resumeiq.service.analysis;

import com.resumeiq.dto.response.GrammarIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GrammarChecker {

    private record Rule(Pattern pattern, String type, String suggestion) {
    }

    private static final List<Rule> RULES = List.of(
            new Rule(
                    Pattern.compile(
                            "\\b(responsible for|responsibilities include|duties included?|tasked with|was involved in)\\b",
                            Pattern.CASE_INSENSITIVE),
                    "WEAK_PHRASE",
                    "Replace with a strong action verb (e.g., 'Led', 'Built', 'Delivered')"
            ),
            new Rule(
                    Pattern.compile("\\b(was|were|been)\\s+[a-z]+ed\\b", Pattern.CASE_INSENSITIVE),
                    "PASSIVE_VOICE",
                    "Prefer active voice — state what you did, not what was done"
            ),
            new Rule(
                    Pattern.compile("\\b(I|me|my|myself|we|our|us)\\b"),
                    "FIRST_PERSON_PRONOUN",
                    "Remove first-person pronouns from a resume — they are implied"
            ),
            new Rule(
                    Pattern.compile("\\betc\\.?\\b", Pattern.CASE_INSENSITIVE),
                    "VAGUE_LANGUAGE",
                    "Replace 'etc.' with a specific example or remove"
            ),
            new Rule(
                    Pattern.compile("\\b(very|really|quite|basically|generally|usually|sometimes)\\b",
                            Pattern.CASE_INSENSITIVE),
                    "FILLER_WORD",
                    "Remove filler words — they weaken the impact of your statements"
            )
    );

    public List<GrammarIssue> check(String parsedText) {
        List<GrammarIssue> issues = new ArrayList<>();

        for (String line : parsedText.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.length() < 5 || issues.size() >= 20) break;

            // Long sentence check
            if (trimmed.split("\\s+").length > 35) {
                issues.add(new GrammarIssue(
                        "LONG_SENTENCE",
                        trimmed.length() > 100 ? trimmed.substring(0, 100) + "..." : trimmed,
                        "Break into shorter, punchier statements — recruiters scan, not read"
                ));
                continue;
            }

            for (Rule rule : RULES) {
                Matcher m = rule.pattern().matcher(trimmed);
                if (m.find()) {
                    issues.add(new GrammarIssue(
                            rule.type(),
                            trimmed.length() > 100 ? trimmed.substring(0, 100) + "..." : trimmed,
                            rule.suggestion()
                    ));
                    break; // one issue per line
                }
            }
        }

        return issues;
    }
}