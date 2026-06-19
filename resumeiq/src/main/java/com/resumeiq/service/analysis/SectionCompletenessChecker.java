package com.resumeiq.service.analysis;

import com.resumeiq.dto.response.SectionCompletenessResult;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class SectionCompletenessChecker {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");

    private static final List<String> SUMMARY_KW = List.of("summary", "objective", "profile", "about", "overview", "professional summary");
    private static final List<String> EXPERIENCE_KW = List.of("experience", "work experience", "employment", "work history", "professional experience", "career", "positions held");
    private static final List<String> EDUCATION_KW = List.of("education", "academic", "qualifications", "degree", "studies");
    private static final List<String> SKILLS_KW = List.of("skills", "technical skills", "competencies", "technologies", "tools", "expertise");
    private static final List<String> PROJECTS_KW = List.of("projects", "personal projects", "key projects", "portfolio");
    private static final List<String> CERTIFICATIONS_KW = List.of("certifications", "certificates", "credentials", "licenses", "courses");
    private static final List<String> ACHIEVEMENTS_KW = List.of("achievements", "accomplishments", "awards", "honors", "recognition");

    public SectionCompletenessResult check(String parsedText) {
        String[] lines = parsedText.split("\n");

        String header = String.join(" ", Arrays.copyOfRange(lines, 0, Math.min(15, lines.length)));
        boolean contact = EMAIL_PATTERN.matcher(header).find();
        boolean summary = detectSection(lines, SUMMARY_KW);
        boolean experience = detectSection(lines, EXPERIENCE_KW);
        boolean education = detectSection(lines, EDUCATION_KW);
        boolean skills = detectSection(lines, SKILLS_KW);
        boolean projects = detectSection(lines, PROJECTS_KW);
        boolean certifications = detectSection(lines, CERTIFICATIONS_KW);
        boolean achievements = detectSection(lines, ACHIEVEMENTS_KW);

        double score = computeScore(contact, summary, experience, education, skills,
                projects, certifications, achievements);

        return new SectionCompletenessResult(contact, summary, experience, education, skills,
                projects, certifications, achievements, score);
    }

    private boolean detectSection(String[] lines, List<String> keywords) {
        return Arrays.stream(lines).anyMatch(line -> {
            String l = line.trim().toLowerCase();
            return l.length() < 50 && keywords.stream().anyMatch(l::contains);
        });
    }

    private double computeScore(boolean contact, boolean summary, boolean experience,
                                boolean education, boolean skills, boolean projects,
                                boolean certifications, boolean achievements) {
        int requiredPresent = (contact ? 1 : 0) + (experience ? 1 : 0)
                + (education ? 1 : 0) + (skills ? 1 : 0);
        int optionalPresent = (summary ? 1 : 0) + (projects ? 1 : 0)
                + (certifications ? 1 : 0) + (achievements ? 1 : 0);

        double requiredScore = (requiredPresent / 4.0) * 80;
        double optionalScore = (optionalPresent / 4.0) * 20;
        return Math.round((requiredScore + optionalScore) * 10.0) / 10.0;
    }
}