package com.resumeiq.dto.response;

public record SectionCompletenessResult(
        boolean contact,
        boolean summary,
        boolean experience,
        boolean education,
        boolean skills,
        boolean projects,
        boolean certifications,
        boolean achievements,
        double completenessScore
) {
}