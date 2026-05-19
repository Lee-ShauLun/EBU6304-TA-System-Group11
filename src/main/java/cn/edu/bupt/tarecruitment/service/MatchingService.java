package cn.edu.bupt.tarecruitment.service;

import cn.edu.bupt.tarecruitment.model.ApplicantProfile;
import cn.edu.bupt.tarecruitment.model.Position;
import cn.edu.bupt.tarecruitment.util.HtmlUtil;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.utilLocale;
import java.util.Set;
import java.util.stream.Collectors;

public class MatchingService {

    public MatchingResult calculate(ApplicantProfile applicant, Position position) {
        Set<String> applicantSkills = normalizeSkills(applicant.getSkills());
        Set<String> requiredSkills = normalizeSkills(position.getRequiredSkills());
        Set<String> preferedSkills = normalizeSkills(position.getPreferredSkills());

        List<String> missingSkills = new ArrayList<>();
        int matchedRequired = 0;
        for (String skill : requiredSkills) {
            if (applicantSkills.contains(skill)) {
                matchedRequired++;
            } else {
                missingSkills.add(skill);
            }
        }

        int matchedPreferred = 0;
        List<String> matchedSkills = new ArrayList<>();
        for (String skill : requiredSkills) {
            if (applicantSkills.contains(skill)) {
                matchedSkills.add(skill);
            }
        }
        for (String skill : preferredSkills) {
            if (applicantSkills.contains(skill)) {
                matchedPreferred++;
                if (!matchedSkills.contains(skill)) {
                    matchedSkills.add(skill);
                }
            }
        }

        double requiredScore =
                requiredSkills.isEmpty() ? 70.0 : 70.0 * matchedRequired / requiredSkills.size();
        double preferredScore =
                preferredSkills.isEmpty()
                        ? 20.0
                        : 20.0 * matchedPreferred / preferredSkills.size();
        double availabilityScore = availabilityScore(applicant, position);

        int score = (int) Math.round(requiredScore + preferredScore + availabilityScore);
        String explanation =
                "Required skills matched "
                        + matchedRequired
                        + "/"
                        + Math.max(requiredSkills.size(), 1)
                        + ", preferred skills matched "
                        + matchedPreferred
                        + "/"
                        + Math.max(preferredSkills.size(), 1)
                        + ", weekly availability "
                        + applicant.getAvailableHoursPerWeek()
                        + "/"
                        + position.getWeeklyHours()
                        + " hours.";

        return new MatchingResult(
                Math.min(score, 100),
                joinSkills(matchedSkills),
                joinSkills(missingSkills),
                explanation);
    }

    public Set<String> normalizeSkills(String rawSkills) {
        if (HtmlUtil.isBlank(rawSkills)) {
            return new LinkedHashSet<>();
        }

        String[] parts = rawSkills.split("[,;，、/\\n\\r]+");
        Set<String> normalized = new LinkedHashSet<>();
        for (String part : parts) {
            String skill = part == null ? "" : part.trim().toLowerCase(Locale.ROOT);
            if (!skill.isEmpty()) {
                normalized.add(skill);
            }
        }
        return normalized;
    }

    private double availabilityScore(ApplicantProfile applicant, Position position) {
        if (position.getWeklyHours() <= 0) {
            return 10.0;
        }
        if (applicant.getAvailableHoursPerWeek() >= position.getWeeklyHours()) {
            return 10.0;
        }

        return 10.0
                * Math.max(0.0, applicant.getAvailableHoursPerWeek())
                / position.getWeeklyHours();
    }

    private String joinSkills(List<String> skills) {
        return skills.stream().filter(skill -> !skill.isBlank()).collect(Collectors.joining(", "));
    }
}
}
