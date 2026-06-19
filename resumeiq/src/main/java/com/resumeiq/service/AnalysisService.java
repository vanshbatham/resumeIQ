package com.resumeiq.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeiq.dto.response.*;
import com.resumeiq.entity.AnalysisResult;
import com.resumeiq.entity.Resume;
import com.resumeiq.exception.ResourceNotFoundException;
import com.resumeiq.exception.ResumeParsingException;
import com.resumeiq.repository.AnalysisResultRepository;
import com.resumeiq.repository.ResumeRepository;
import com.resumeiq.service.analysis.KeywordAnalyzer;
import com.resumeiq.service.analysis.ResumeAnalysisOrchestrator;
import com.resumeiq.service.analysis.ResumeAnalysisOrchestrator.OrchestratedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final ResumeRepository resumeRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final ResumeAnalysisOrchestrator orchestrator;
    private final KeywordAnalyzer keywordAnalyzer;
    private final ObjectMapper objectMapper;

    @Transactional
    public AnalysisResponse analyze(UUID resumeId, UUID requestingUserId) {
        Resume resume = loadResume(resumeId);

        if (resume.getParsedText() == null || resume.getParsedText().isBlank()) {
            throw new ResumeParsingException("Resume has no parsed text to analyze", null);
        }

        OrchestratedResult result = orchestrator.analyze(resume.getParsedText());

        AnalysisResult entity = new AnalysisResult();
        entity.setResume(resume);
        entity.setOverallScore(result.overallScore());
        entity.setAtsScore(result.atsScore());
        entity.setReadabilityScore(result.readability().fleschReadingEase());
        entity.setReadabilityData(serialize(result.readability()));
        entity.setSectionCompleteness(serialize(result.sectionCompleteness()));
        entity.setActionVerbAnalysis(serialize(result.actionVerbs()));
        entity.setQuantifiableAchievements(serialize(result.achievements()));
        entity.setGrammarIssues(serialize(result.grammarIssues()));
        analysisResultRepository.save(entity);

        return buildResponse(entity, result);
    }

    @Transactional(readOnly = true)
    public AnalysisResponse getLatest(UUID resumeId, UUID requestingUserId) {
        loadResume(resumeId); // ownership check

        AnalysisResult entity = analysisResultRepository
                .findTopByResumeIdOrderByCreatedAtDesc(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AnalysisResult", "for resume " + resumeId));

        return buildResponseFromEntity(entity);
    }

    @Transactional
    public KeywordGapResult analyzeKeywordGap(UUID resumeId, String jobDescriptionText,
                                              UUID requestingUserId) {
        Resume resume = loadResume(resumeId);
        KeywordGapResult result = keywordAnalyzer.analyze(
                resume.getParsedText(), jobDescriptionText);

        // Persist into the latest analysis result if one exists
        analysisResultRepository.findTopByResumeIdOrderByCreatedAtDesc(resumeId)
                .ifPresent(ar -> {
                    ar.setKeywordGapAnalysis(serialize(result));
                    analysisResultRepository.save(ar);
                });

        return result;
    }

    private Resume loadResume(UUID resumeId) {
        return resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", resumeId.toString()));
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<GrammarIssue> deserializeList(String json) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, GrammarIssue.class));
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private AnalysisResponse buildResponse(AnalysisResult entity, OrchestratedResult result) {
        return new AnalysisResponse(
                entity.getId(), entity.getResume().getId(),
                entity.getOverallScore(), entity.getAtsScore(),
                result.readability(), result.sectionCompleteness(),
                result.actionVerbs(), result.achievements(),
                result.grammarIssues(), entity.getCreatedAt()
        );
    }

    AnalysisResponse buildResponseFromEntity(AnalysisResult entity) {
        return new AnalysisResponse(
                entity.getId(), entity.getResume().getId(),
                entity.getOverallScore() != null ? entity.getOverallScore() : 0,
                entity.getAtsScore() != null ? entity.getAtsScore() : 0,
                deserialize(entity.getReadabilityData(), ReadabilityResult.class),
                deserialize(entity.getSectionCompleteness(), SectionCompletenessResult.class),
                deserialize(entity.getActionVerbAnalysis(), ActionVerbResult.class),
                deserialize(entity.getQuantifiableAchievements(), AchievementResult.class),
                deserializeList(entity.getGrammarIssues()),
                entity.getCreatedAt()
        );
    }
}