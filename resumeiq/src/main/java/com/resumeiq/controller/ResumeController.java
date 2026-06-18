package com.resumeiq.controller;

import com.resumeiq.dto.response.ResumeResponse;
import com.resumeiq.dto.response.ResumeUploadResponse;
import com.resumeiq.security.UserPrincipal;
import com.resumeiq.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
@Tag(name = "Resume", description = "Resume upload and retrieval")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload a PDF or DOCX resume. Works for both guests and authenticated users.")
    public ResumeUploadResponse upload(@RequestPart("file") MultipartFile file,
                                       @AuthenticationPrincipal UserPrincipal principal) {

        UUID userId = principal != null ? principal.getId() : null;
        return resumeService.upload(file, userId);
    }

    @GetMapping("/{resumeId}")
    @Operation(summary = "Get a resume by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResumeUploadResponse getById(@PathVariable UUID resumeId, @AuthenticationPrincipal UserPrincipal principal) {
        UUID userId = principal != null ? principal.getId() : null;
        return resumeService.getById(resumeId, userId);
    }

    @GetMapping("/my")
    @Operation(summary = "Get all resumes for the authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    public List<ResumeResponse> getMyResumes(@AuthenticationPrincipal UserPrincipal principal) {
        return resumeService.getMyResumes(principal.getId());
    }
}