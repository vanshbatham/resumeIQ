package com.resumeiq.service.analysis;

import com.resumeiq.dto.response.KeywordGapResult;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class KeywordAnalyzer {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for", "of",
            "with", "by", "from", "as", "is", "was", "are", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "will", "would", "could", "should",
            "may", "might", "this", "that", "these", "those", "i", "we", "you", "he", "she",
            "they", "it", "my", "our", "your", "his", "her", "their", "its", "not", "no",
            "so", "yet", "if", "also", "about", "into", "through", "after", "before",
            "experience", "work", "strong", "excellent", "good", "ability", "must", "required",
            "preferred", "including", "able", "various", "using", "across", "minimum"
    );

    private static final Set<String> SKILL_DICTIONARY = Set.of(
            "java", "python", "javascript", "typescript", "kotlin", "scala", "go", "rust",
            "c++", "c#", "ruby", "php", "swift", "r", "sql", "bash", "html", "css",
            "spring", "spring boot", "spring security", "spring cloud", "hibernate",
            "react", "angular", "vue", "nodejs", "express", "django", "flask", "fastapi",
            ".net", "next.js",
            "postgresql", "mysql", "mongodb", "redis", "elasticsearch", "cassandra",
            "dynamodb", "oracle", "sqlite", "neo4j", "kafka", "rabbitmq",
            "aws", "azure", "gcp", "docker", "kubernetes", "terraform", "jenkins",
            "ci/cd", "git", "github", "gitlab", "linux", "nginx", "ansible",
            "rest", "graphql", "microservices", "api", "oauth", "jwt", "tdd",
            "agile", "scrum", "jira", "maven", "gradle",
            "machine learning", "deep learning", "tensorflow", "pytorch", "pandas",
            "numpy", "spark", "hadoop", "tableau", "power bi",
            "distributed systems", "system design", "event sourcing", "cqrs",
            "solid", "design patterns", "clean architecture"
    );

    public KeywordGapResult analyze(String resumeText, String jobDescriptionText) {
        List<String> jdKeywords = extractKeywords(jobDescriptionText, 25);
        List<String> resumeKeywords = extractKeywords(resumeText, 25);

        // Dictionary-matched skills from JD (handles multi-word phrases)
        String jdLower = jobDescriptionText.toLowerCase();
        Set<String> dictSkills = SKILL_DICTIONARY.stream()
                .filter(jdLower::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> allJdKeywords = new LinkedHashSet<>(jdKeywords);
        allJdKeywords.addAll(dictSkills);

        String resumeLower = resumeText.toLowerCase();
        List<String> matched = allJdKeywords.stream()
                .filter(kw -> resumeLower.contains(kw.toLowerCase()))
                .distinct().toList();

        List<String> missing = allJdKeywords.stream()
                .filter(kw -> !resumeLower.contains(kw.toLowerCase()))
                .distinct().toList();

        double matchScore = allJdKeywords.isEmpty() ? 0 :
                Math.round(((double) matched.size() / allJdKeywords.size()) * 100 * 10.0) / 10.0;

        return new KeywordGapResult(matched, missing, resumeKeywords, matchScore);
    }

    private List<String> extractKeywords(String text, int topN) {
        List<String> tokens = tokenize(text);
        Map<String, Long> freq = tokens.stream()
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));

        int total = tokens.size();
        return freq.entrySet().stream()
                .filter(e -> e.getKey().length() > 2)
                .sorted((a, b) -> {
                    double tfidfA = ((double) a.getValue() / total) * idfBoost(a.getKey());
                    double tfidfB = ((double) b.getValue() / total) * idfBoost(b.getKey());
                    return Double.compare(tfidfB, tfidfA);
                })
                .limit(topN)
                .map(Map.Entry::getKey)
                .toList();
    }

    private double idfBoost(String term) {
        return SKILL_DICTIONARY.contains(term) ? 3.0 : 1.5;
    }

    private List<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("[^a-zA-Z0-9+#./]+"))
                .map(String::trim)
                .filter(t -> t.length() > 2 && !STOP_WORDS.contains(t))
                .toList();
    }
}