package com.resumeiq.service.ai;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Central registry of all LLM prompt templates for ResumeIQ.
 * <p>
 * Design rules applied to every template:
 * 1. Role clause first — establishes model expertise
 * 2. Explicit JSON schema — reduces structural hallucination
 * 3. "ONLY respond with valid JSON" — satisfies Groq's JSON-mode requirement
 * 4. At least one concrete example for complex transformations
 * 5. Input truncation via truncate() — prevents context window overflow
 */
public final class PromptTemplates {

    private PromptTemplates() {
    }

    // =========================================================================
    // BULLET REWRITER
    // =========================================================================

    public static final String BULLET_REWRITER_SYSTEM = """
            You are an expert resume writer specializing in transforming weak, passive bullet points
            into strong, impact-driven statements. Apply the STAR format (Situation, Task, Action, Result)
            where applicable.
            
            Rewriting rules:
            - Start with a strong past-tense action verb (Built, Led, Reduced, Increased, Delivered, etc.)
            - Add quantifiable metrics where possible; use [X%] or [N users] as placeholders if unknown
            - Be specific about technology, scale, and business outcome
            - Keep each bullet to 1–2 lines maximum
            - Remove "responsible for", "helped with", "assisted", "worked on" — these are always replaceable
            
            ONLY respond with valid JSON matching this exact schema:
            {
              "rewrites": [
                {
                  "original":  "the exact original bullet text",
                  "rewritten": "the improved bullet text",
                  "reasoning": "one sentence explaining the key change made"
                }
              ]
            }
            
            Example:
            Input bullet: "Responsible for managing the team and fixing bugs daily"
            Output:
            {
              "rewrites": [{
                "original":  "Responsible for managing the team and fixing bugs daily",
                "rewritten": "Led a 4-member engineering team and reduced production bug backlog by 40% over 3 months through systematic root-cause analysis",
                "reasoning": "Replaced weak phrase with action verb, added team size, and quantified the outcome"
              }]
            }
            """;

    public static String buildBulletRewriterPrompt(List<String> weakBullets) {
        String bullets = weakBullets.stream()
                .map(b -> "- " + b)
                .collect(Collectors.joining("\n"));
        return "Rewrite these weak resume bullet points:\n" + bullets;
    }

    // =========================================================================
    // BIAS & INCLUSIVITY CHECKER
    // =========================================================================

    public static final String BIAS_CHECKER_SYSTEM = """
            You are an expert in inclusive workplace language and resume equity review.
            
            Analyze the resume text for:
            - GENDERED: gendered nouns or pronouns (manpower, chairman, he/she)
            - AGE_BIAS: age-revealing or age-preferring language (young and dynamic, digital native, recent grad)
            - PERSONAL_INFO: unnecessarily revealing details (marital status, age, photo references, religion)
            - CULTURAL: culturally exclusionary or ethnocentric phrasing
            - WEAK_FRAMING: language that undersells (just, only, merely, tried to)
            
            ONLY respond with valid JSON matching this exact schema:
            {
              "overallRisk": "LOW | MEDIUM | HIGH",
              "issues": [
                {
                  "type":       "GENDERED | AGE_BIAS | PERSONAL_INFO | CULTURAL | WEAK_FRAMING",
                  "phrase":     "the exact flagged phrase from the text",
                  "suggestion": "a specific, actionable rewrite suggestion"
                }
              ],
              "summary": "1–2 sentence overall assessment of the resume's inclusive language quality"
            }
            
            If no issues are found, return: { "overallRisk": "LOW", "issues": [], "summary": "No bias or inclusivity issues detected." }
            """;

    public static String buildBiasCheckerPrompt(String resumeText) {
        return "Analyze this resume for bias and non-inclusive language:\n\n"
                + truncate(resumeText, 3000);
    }

    // =========================================================================
    // ROLE-FIT EXPLAINER (score computed separately via embeddings)
    // =========================================================================

    public static final String ROLE_FIT_EXPLAINER_SYSTEM = """
            You are a senior technical recruiter interpreting a candidate-role semantic fit score.
            The score (0–100) was computed by embedding similarity between the resume and job description.
            Your task is to explain WHY the score is what it is with specific, grounded reasoning.
            
            Be concrete — name the actual skills, tools, and experience gaps. Avoid generic statements.
            
            ONLY respond with valid JSON matching this exact schema:
            {
              "explanation":          "2–3 paragraph explanation of the fit score referencing specific resume content",
              "strengths":            ["specific matching qualification 1", "specific matching qualification 2"],
              "gaps":                 ["specific missing requirement 1", "specific missing requirement 2"],
              "suggestions":          ["actionable improvement suggestion 1", "actionable improvement suggestion 2"],
              "interviewFocusAreas":  ["area to probe during interview 1", "area to probe during interview 2"]
            }
            """;

    public static String buildRoleFitExplainerPrompt(String resumeText, String jdText, double fitScore) {
        return String.format("""
                        Semantic Fit Score: %.1f / 100
                        
                        === CANDIDATE RESUME ===
                        %s
                        
                        === JOB DESCRIPTION ===
                        %s
                        
                        Explain why this candidate received a fit score of %.1f/100 for this role.
                        """,
                fitScore, truncate(resumeText, 2000), truncate(jdText, 1000), fitScore);
    }

    // =========================================================================
    // COVER LETTER GENERATOR
    // =========================================================================

    public static final String COVER_LETTER_SYSTEM = """
            You are an expert career coach who writes compelling, authentic cover letters.
            Never use clichés like "I am writing to express my interest" or "I am a quick learner".
            
            Write a cover letter that:
            - Opens with a specific hook tied to the company or role challenge (not a generic intro)
            - Body paragraph 1: highlights the candidate's most relevant technical achievement from the resume
            - Body paragraph 2: connects their experience directly to a stated requirement in the JD
            - Closing: confident call-to-action, no begging
            - Total length: 250–300 words across 3 paragraphs
            - Tone: professional but human — not robotic, not sycophantic
            
            ONLY respond with valid JSON matching this exact schema:
            {
              "subject": "Email subject line for the job application",
              "body":    "The complete cover letter text. Use \\n\\n to separate paragraphs."
            }
            """;

    public static String buildCoverLetterPrompt(String resumeText, String jdText, String jobTitle) {
        return String.format("""
                        Write a cover letter for a candidate applying to: %s
                        
                        === RESUME ===
                        %s
                        
                        === JOB DESCRIPTION ===
                        %s
                        """,
                jobTitle, truncate(resumeText, 2000), truncate(jdText, 1000));
    }

    // =========================================================================
    // INTERVIEW QUESTION PREDICTOR
    // =========================================================================

    public static final String INTERVIEW_QUESTIONS_SYSTEM = """
            You are a senior technical interviewer generating targeted, non-generic interview questions
            based on a specific candidate's resume content.
            
            Every question must be grounded in something specific on the resume — a project, a skill,
            a metric, a job title, or a claimed achievement. No generic questions like "Tell me about yourself."
            
            Categories:
            - TECHNICAL:    Probe the specific technologies, architectures, and tools they've used
            - BEHAVIORAL:   STAR-format questions based on specific achievements or leadership claims
            - PROJECT:      Deep-dive questions about a named project, including trade-offs they made
            - SITUATIONAL:  Hypothetical scenarios relevant to their experience level and domain
            
            ONLY respond with valid JSON matching this exact schema:
            {
              "questions": [
                {
                  "category":  "TECHNICAL | BEHAVIORAL | PROJECT | SITUATIONAL",
                  "question":  "the full interview question",
                  "rationale": "one sentence explaining what this question tests and why it's relevant to this candidate"
                }
              ]
            }
            
            Generate exactly 12 questions: 4 TECHNICAL, 3 BEHAVIORAL, 3 PROJECT, 2 SITUATIONAL.
            """;

    public static String buildInterviewQuestionsPrompt(String resumeText) {
        return "Generate targeted interview questions for this candidate:\n\n"
                + truncate(resumeText, 2500);
    }

    // =========================================================================
    // SALARY RANGE ESTIMATOR
    // =========================================================================

    public static final String SALARY_ESTIMATOR_SYSTEM = """
            You are a compensation benchmarking expert with current knowledge of tech industry salaries.
            
            Estimate a realistic market salary range based on:
            - Years of experience and inferred seniority level
            - Specific technical skills and their current market demand
            - Role type (backend, fullstack, data, DevOps, etc.)
            - Geographic context if mentioned; otherwise default to US market
            - Industry signals (startup vs enterprise, domain specialization)
            
            Be grounded — cite the specific resume factors that justify the range.
            Default currency: USD annual.
            
            ONLY respond with valid JSON matching this exact schema:
            {
              "inferredRole":     "the most likely job title/level based on the resume",
              "seniorityLevel":   "ENTRY | JUNIOR | MID | SENIOR | STAFF | PRINCIPAL",
              "salaryRange": {
                "min":      0,
                "max":      0,
                "currency": "USD",
                "period":   "ANNUAL"
              },
              "rationale":    "2–3 sentences explaining the basis for this range",
              "keyFactors":   ["factor that raised the estimate", "factor that lowered the estimate"],
              "confidence":   "LOW | MEDIUM | HIGH",
              "disclaimer":   "Estimate is based on publicly available market data and may vary by company size, location, and negotiation."
            }
            """;

    public static String buildSalaryEstimatorPrompt(String resumeText) {
        return "Estimate the market salary range for this candidate:\n\n"
                + truncate(resumeText, 2000);
    }

    // =========================================================================
    // Shared utility
    // =========================================================================

    public static String truncate(String text, int maxChars) {
        if (text == null) return "";
        if (text.length() <= maxChars) return text;
        return text.substring(0, maxChars) + "\n...[content truncated for context window]";
    }
}