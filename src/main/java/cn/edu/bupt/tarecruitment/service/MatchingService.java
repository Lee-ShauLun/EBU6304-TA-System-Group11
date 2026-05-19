package cn.edu.bupt.tarecruitment.service;

import cn.edu.bupt.tarecruitment.model.ApplicantProfile;
import cn.edu.bupt.tarecruitment.model.Position;
import cn.edu.bupt.tarecruitment.util.HtmlUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class MatchingService {

    // 分数权重（保持原有逻辑不动）
    private static final double REQUIRED_WEIGHT = 70.0;
    private static final double PREFERRED_WEIGHT = 20.0;
    private static final double AVAILABILITY_WEIGHT = 10.0;

    public MatchingResult calculate(ApplicantProfile applicant, Position position) {
        Set<String> applicantSkills = normalizeSkills(applicant.getSkills());
        Set<String> requiredSkills = normalizeSkills(position.getRequiredSkills());
        Set<String> preferredSkills = normalizeSkills(position.getPreferredSkills());

        List<String> missingSkills = new ArrayList<>();
        int matchedRequired = 0;

        // 计算必选技能匹配与缺失
        for (String skill : requiredSkills) {
            if (applicantSkills.contains(skill)) {
                matchedRequired++;
            } else {
                missingSkills.add(skill);
            }
        }

        // 构建已匹配的技能列表
        List<String> matchedSkills = new ArrayList<>();
        for (String skill : requiredSkills) {
            if (applicantSkills.contains(skill)) {
                matchedSkills.add(skill);
            }
        }

        // 计算优选技能匹配
        int matchedPreferred = 0;
        for (String skill : preferredSkills) {
            if (applicantSkills.contains(skill)) {
                matchedPreferred++;
                if (!matchedSkills.contains(skill)) {
                    matchedSkills.add(skill);
                }
            }
        }

        // 计算三项分数
        double requiredScore = requiredSkills.isEmpty()
                ? REQUIRED_WEIGHT
                : REQUIRED_WEIGHT * matchedRequired / requiredSkills.size();

        double preferredScore = preferredSkills.isEmpty()
                ? PREFERRED_WEIGHT
                : PREFERRED_WEIGHT * matchedPreferred / preferredSkills.size();

        double availabilityScore = availabilityScore(applicant, position);

        // 总分四舍五入并封顶100
        int totalScore = (int) Math.round(requiredScore + preferredScore + availabilityScore);
        int finalScore = Math.min(totalScore, 100);

        // 构建说明文本
        String explanation = String.format(
                "Required skills matched %d/%d, preferred skills matched %d/%d, weekly availability %.0f/%d hours.",
                matchedRequired,
                Math.max(requiredSkills.size(), 1),
                matchedPreferred,
                Math.max(preferredSkills.size(), 1),
                applicant.getAvailableHoursPerWeek(),
                position.getWeeklyHours()
        );

        return new MatchingResult(
                finalScore,
                joinSkills(matchedSkills),
                joinSkills(missingSkills),
                explanation
        );
    }

    public Set<String> normalizeSkills(String rawSkills) {
        if (HtmlUtil.isBlank(rawSkills)) {
            return new LinkedHashSet<>();
        }

        String[] parts = rawSkills.split("[,;，、/\\n\\r]+");
        Set<String> normalized = new LinkedHashSet<>();

        for (String part : parts) {
            if (part == null) continue;

            String skill = part.trim().toLowerCase(Locale.ROOT);
            if (!skill.isEmpty()) {
                normalized.add(skill);
            }
        }

        return normalized;
    }

    private double availabilityScore(ApplicantProfile applicant, Position position) {
        int requiredHours = position.getWeeklyHours();
        double availableHours = applicant.getAvailableHoursPerWeek();

        if (requiredHours <= 0) {
            return AVAILABILITY_WEIGHT;
        }

        if (availableHours >= requiredHours) {
            return AVAILABILITY_WEIGHT;
        }

        return AVAILABILITY_WEIGHT * Math.max(0.0, availableHours) / requiredHours;
    }

    private String joinSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return "";
        }
        return skills.stream()
                .filter(skill -> !HtmlUtil.isBlank(skill))
                .collect(Collectors.joining(", "));
    }
}
