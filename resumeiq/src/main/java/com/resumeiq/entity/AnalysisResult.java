package com.resumeiq.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "analysis_results")
@Getter
@Setter
@NoArgsConstructor
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(name = "ats_score")
    private Double atsScore;

    @Column(name = "readability_score")
    private Double readabilityScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "readability_data", columnDefinition = "jsonb")
    private String readabilityData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "section_completeness", columnDefinition = "jsonb")
    private String sectionCompleteness;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_verb_analysis", columnDefinition = "jsonb")
    private String actionVerbAnalysis;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quantifiable_achievements", columnDefinition = "jsonb")
    private String quantifiableAchievements;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "grammar_issues", columnDefinition = "jsonb")
    private String grammarIssues;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "keyword_gap_analysis", columnDefinition = "jsonb")
    private String keywordGapAnalysis;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}