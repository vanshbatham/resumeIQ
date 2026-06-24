package com.resumeiq.controller;

import com.resumeiq.service.ai.AiClient;
import com.resumeiq.service.ai.PromptTemplates;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Development-only controller for validating AI provider connectivity.
 * Remove this controller before production deployment.
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI (Dev)", description = "Development-only endpoints to validate AI integration")
public class AiTestController {

    private final AiClient aiClient;

    @GetMapping("/health")
    @Operation(summary = "Verify Groq API connectivity with a minimal call")
    public Map<String, String> healthCheck() {
        String response = aiClient.generateText(
                "You are a concise assistant.",
                "Respond with exactly this text and nothing else: 'ResumeIQ AI integration operational.'"
        );
        return Map.of(
                "status", "OK",
                "provider", "groq",
                "response", response.trim()
        );
    }

    @PostMapping("/test-structured")
    @Operation(summary = "Test structured JSON output from Groq")
    public Object testStructured(@RequestBody String bulletText) {
        record RewriteItem(String original, String rewritten, String reasoning) {
        }
        record BulletRewriteResponse(List<RewriteItem> rewrites) {
        }

        return aiClient.generateStructured(
                PromptTemplates.BULLET_REWRITER_SYSTEM,
                PromptTemplates.buildBulletRewriterPrompt(List.of(bulletText)),
                BulletRewriteResponse.class
        );
    }
}