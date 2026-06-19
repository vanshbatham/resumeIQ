package com.resumeiq.service.analysis;

import com.resumeiq.dto.response.ActionVerbResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ActionVerbAnalyzer {

    private static final Set<String> STRONG_VERBS = Set.of(
            // Achievement
            "achieved", "accomplished", "attained", "completed", "delivered",
            "exceeded", "surpassed", "awarded",
            // Leadership
            "led", "managed", "directed", "oversaw", "supervised", "coordinated",
            "mentored", "coached", "guided", "established", "founded", "spearheaded",
            // Technical
            "built", "developed", "engineered", "architected", "designed", "implemented",
            "deployed", "created", "launched", "shipped", "integrated", "automated",
            "refactored", "migrated", "optimized", "debugged", "resolved",
            // Improvement
            "improved", "enhanced", "upgraded", "streamlined", "accelerated",
            "reduced", "eliminated", "increased", "boosted", "grew", "scaled",
            "expanded", "transformed", "modernized", "revamped",
            // Analysis
            "analyzed", "assessed", "evaluated", "researched", "identified",
            "diagnosed", "investigated", "benchmarked", "audited",
            // Delivery
            "generated", "saved", "cut", "negotiated", "presented",
            "authored", "documented", "published", "trained", "collaborated"
    );

    private static final List<String> WEAK_PHRASES = List.of(
            "responsible for", "assisted with", "helped with", "worked on",
            "was involved in", "participated in", "contributed to", "supported the",
            "provided support", "tasked with", "duties included", "job was to",
            "was responsible", "role involved", "assisted in"
    );

    private static final Pattern BULLET_PATTERN =
            Pattern.compile("^\\s*[•\\-*►▸◦‣⁃]\\s*(.{10,})");

    public ActionVerbResult analyze(String parsedText) {
        List<String> bullets = extractBullets(parsedText);
        List<String> weakBullets = new ArrayList<>();
        int strong = 0;
        int weak = 0;

        for (String bullet : bullets) {
            String lower = bullet.toLowerCase().trim();
            String firstWord = lower.split("\\s+")[0];

            boolean isWeak = WEAK_PHRASES.stream().anyMatch(lower::startsWith);
            boolean isStrong = STRONG_VERBS.contains(firstWord);

            if (isWeak) {
                weak++;
                weakBullets.add(bullet.trim().length() > 120
                        ? bullet.trim().substring(0, 120) + "..." : bullet.trim());
            } else if (isStrong) {
                strong++;
            }
        }

        int total = bullets.size();
        int classified = strong + weak;
        double score = classified == 0 ? 50.0 :
                Math.round(((double) strong / classified) * 100 * 10.0) / 10.0;

        return new ActionVerbResult(total, strong, weak, weakBullets, score);
    }

    private List<String> extractBullets(String text) {
        List<String> result = new ArrayList<>();
        for (String line : text.split("\n")) {
            Matcher m = BULLET_PATTERN.matcher(line);
            if (m.matches()) result.add(m.group(1).trim());
        }
        return result;
    }
}