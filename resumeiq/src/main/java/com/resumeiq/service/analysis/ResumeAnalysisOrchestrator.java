package com.resumeiq.service.analysis;

import com.resumeiq.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ResumeAnalysisOrchestrator {

    private final AtsScorer atsScorer;
    private final SectionCompletenessChecker sectionChecker;
    private final ReadabilityScorer readabilityScorer;
    private final ActionVerbAnalyzer actionVerbAnalyzer;
    private final AchievementDetector achievementDetector;
    private final GrammarChecker grammarChecker;

    public OrchestratedResult analyze(String parsedText) {
        double atsScore = atsScorer.score(parsedText);
        SectionCompletenessResult sections = sectionChecker.check(parsedText);
        ReadabilityResult readability = readabilityScorer.score(parsedText);
        ActionVerbResult actionVerbs = actionVerbAnalyzer.analyze(parsedText);
        AchievementResult achievements = achievementDetector.detect(parsedText);
        List<GrammarIssue> grammar = grammarChecker.check(parsedText);

        double grammarScore = computeGrammarScore(grammar);
        double overallScore = computeOverallScore(
                atsScore, sections.completenessScore(), readability.fleschReadingEase(),
                actionVerbs.score(), achievements.score(), grammarScore);

        return new OrchestratedResult(overallScore, atsScore, readability,
                sections, actionVerbs, achievements, grammar);
    }

    private double computeGrammarScore(List<GrammarIssue> issues) {
        return Math.round(Math.max(0, 100 - issues.size() * 5.0) * 10.0) / 10.0;
    }

    private double computeOverallScore(double ats, double sections, double readability,
                                       double actionVerbs, double achievements, double grammar) {
        double weighted = ats * 0.30
                + sections * 0.20
                + readability * 0.15
                + actionVerbs * 0.15
                + achievements * 0.10
                + grammar * 0.10;
        return Math.round(Math.min(100, Math.max(0, weighted)) * 10.0) / 10.0;
    }

    public record OrchestratedResult(
            double overallScore,
            double atsScore,
            ReadabilityResult readability,
            SectionCompletenessResult sectionCompleteness,
            ActionVerbResult actionVerbs,
            AchievementResult achievements,
            List<GrammarIssue> grammarIssues
    ) {
    }
}