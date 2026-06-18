package com.resumeiq.service;

import com.resumeiq.dto.response.ResumeResponse;
import com.resumeiq.dto.response.ResumeUploadResponse;
import com.resumeiq.entity.Resume;
import com.resumeiq.entity.User;
import com.resumeiq.exception.ResourceNotFoundException;
import com.resumeiq.repository.ResumeRepository;
import com.resumeiq.repository.UserRepository;
import com.resumeiq.service.parsing.FileStorageService;
import com.resumeiq.service.parsing.ResumeParsingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ResumeParsingService resumeParsingService;

    @Transactional
    public ResumeUploadResponse upload(MultipartFile file, UUID userId) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload.");
        }

        String parsedText = resumeParsingService.extractAndClean(file);
        String subdirectory = userId != null ? userId.toString() : "guest";
        String storagePath = fileStorageService.store(file, subdirectory);

        Resume resume = new Resume();
        resume.setFileName(file.getOriginalFilename());
        resume.setStoragePath(storagePath);
        resume.setParsedText(parsedText);

        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
            resume.setUser(user);
        }

        resumeRepository.save(resume);
        return toUploadResponse(resume);
    }

    @Transactional(readOnly = true)
    public ResumeUploadResponse getById(UUID resumeId, UUID requestingUserId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", resumeId.toString()));

        enforceOwnership(resume, requestingUserId);
        return toUploadResponse(resume);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getMyResumes(UUID userId) {
        return resumeRepository.findByUserIdOrderByUploadedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void enforceOwnership(Resume resume, UUID requestingUserId) {
        if (resume.getUser() == null) return;
        if (requestingUserId == null ||
                !resume.getUser().getId().equals(requestingUserId)) {
            throw new ResourceNotFoundException("Resume", resume.getId().toString());
        }
    }

    private ResumeUploadResponse toUploadResponse(Resume resume) {
        String text = resume.getParsedText() != null ? resume.getParsedText() : "";
        String preview = text.length() > 300 ? text.substring(0, 300) + "..." : text;
        return new ResumeUploadResponse(
                resume.getId(),
                resume.getFileName(),
                resume.getStoragePath(),
                text.length(),
                preview,
                resume.getUploadedAt()
        );
    }

    private ResumeResponse toResponse(Resume resume) {
        String text = resume.getParsedText() != null ? resume.getParsedText() : "";
        return new ResumeResponse(
                resume.getId(),
                resume.getFileName(),
                text.length(),
                resume.getUploadedAt()
        );
    }
}