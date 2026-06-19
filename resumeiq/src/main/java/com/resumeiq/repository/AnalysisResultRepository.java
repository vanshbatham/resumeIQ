package com.resumeiq.repository;

import com.resumeiq.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, UUID> {
    Optional<AnalysisResult> findTopByResumeIdOrderByCreatedAtDesc(UUID resumeId);
}