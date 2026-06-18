package com.resumeiq.service.parsing;

import com.resumeiq.exception.ResumeParsingException;
import com.resumeiq.exception.ScannedPdfException;
import com.resumeiq.exception.UnsupportedFileTypeException;
import com.resumeiq.util.TextCleaningUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeParsingService {

    private final List<TextExtractor> extractors;
    private final TextCleaningUtil textCleaningUtil;

    public String extractAndClean(MultipartFile file) {
        String contentType = resolveContentType(file);

        TextExtractor extractor = extractors.stream()
                .filter(e -> e.supports(contentType))
                .findFirst()
                .orElseThrow(() -> new UnsupportedFileTypeException(contentType));

        try {
            String rawText = extractor.extract(file.getInputStream());
            return textCleaningUtil.clean(rawText);
        } catch (ScannedPdfException e) {
            throw e;
        } catch (IOException e) {
            throw new ResumeParsingException("Failed to read file content", e);
        }
    }

    private String resolveContentType(MultipartFile file) {
        String declared = file.getContentType();
        String filename = file.getOriginalFilename();

        if (declared == null || "application/octet-stream".equals(declared)) {
            if (filename != null) {
                String lower = filename.toLowerCase();
                if (lower.endsWith(".pdf")) return "application/pdf";
                if (lower.endsWith(".docx"))
                    return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
        }
        return declared != null ? declared : "";
    }
}