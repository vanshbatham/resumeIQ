package com.resumeiq.controller;

import com.resumeiq.dto.request.AnalyzeRequest;
import com.resumeiq.dto.request.KeywordGapRequest;
import com.resumeiq.dto.response.AnalysisResponse;
import com.resumeiq.dto.response.KeywordGapResult;
import com.resumeiq.security.UserPrincipal;
import com.resumeiq.service.AnalysisService;
import com.resumeiq.service.ReportGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis (Free)", description = "Rule-based resume analysis — available to all users")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final ReportGenerationService reportService;

    @PostMapping("/analyze")
    @Operation(summary = "Run full rule-based analysis on a resume")
    public AnalysisResponse analyze(@Valid @RequestBody AnalyzeRequest request, @AuthenticationPrincipal UserPrincipal principal) {

        UUID userId = principal != null ? principal.getId() : null;
        return analysisService.analyze(request.resumeId(), userId);
    }

    @GetMapping("/{resumeId}/latest")
    @Operation(summary = "Get the most recent analysis result for a resume")
    public AnalysisResponse getLatest(@PathVariable UUID resumeId, @AuthenticationPrincipal UserPrincipal principal) {

        UUID userId = principal != null ? principal.getId() : null;
        return analysisService.getLatest(resumeId, userId);
    }

    @PostMapping("/keyword-gap")
    @Operation(summary = "Analyze keyword/skill gap between a resume and a job description")
    public KeywordGapResult keywordGap(@Valid @RequestBody KeywordGapRequest request, @AuthenticationPrincipal UserPrincipal principal) {

        UUID userId = principal != null ? principal.getId() : null;
        return analysisService.analyzeKeywordGap(
                request.resumeId(), request.jobDescriptionText(), userId);
    }

    @GetMapping("/{analysisId}/report")
    @Operation(summary = "Download a PDF report for an analysis result",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<byte[]> downloadReport(@PathVariable UUID analysisId) throws IOException {

        byte[] pdf = reportService.generateReport(analysisId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"resumeiq-report-" + analysisId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}