package cn.edu.bupt.tarecruitment.service;

import cn.edu.bupt.tarecruitment.model.ApplicantProfile;
import cn.edu.bupt.tarecruitment.model.ApplicationRecord;
import cn.edu.bupt.tarecruitment.model.Position;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds workload summaries from selected applications and position weekly hours.
 */
public class WorkloadService {

    public List<WorkloadEntry> buildReport(
            List<ApplicantProfile> applicants,
            List<Position> positions,
            List<ApplicationRecord> applications) {

        Map<String, Position> positionIndex = new HashMap<>();
        for (Position position : positions) {
            positionIndex.put(position.getId(), position);
        }

        Map<String, Integer> assignedHours = new HashMap<>();
        Map<String, Integer> selectedCounts = new HashMap<>();
        Map<String, List<String>> moduleLabels = new HashMap<>();

        for (ApplicationRecord application : applications) {
            if (!"SELECTED".equals(application.getStatus())) {
                continue;
            }

            Position position = positionIndex.get(application.getPositionId());
            if (position == null) {
                continue;
            }

            assignedHours.merge(
                    application.getApplicantId(), position.getWeeklyHours(), Integer::sum);
            selectedCounts.merge(application.getApplicantId(), 1, Integer::sum);
            moduleLabels
                    .computeIfAbsent(application.getApplicantId(), key -> new ArrayList<>())
                    .add(position.getModuleCode() + " " + position.getModuleName());
        }

        List<WorkloadEntry> results = new ArrayList<>();
        for (ApplicantProfile applicant : applicants) {
            int currentHours = assignedHours.getOrDefault(applicant.getId(), 0);
            int maxHours = Math.max(applicant.getAvailableHoursPerWeek(), 0);
            int selectedPositions = selectedCounts.getOrDefault(applicant.getId(), 0);
            String modules = String.join(" / ", moduleLabels.getOrDefault(applicant.getId(), List.of()));
            String status = evaluateStatus(currentHours, maxHours);
            String recommendation = buildRecommendation(status, currentHours, maxHours);

            results.add(
                    new WorkloadEntry(
                            applicant.getId(),
                            applicant.getFullName(),
                            currentHours,
                            maxHours,
                            selectedPositions,
                            modules,
                            status,
                            recommendation));
        }

        results.sort((left, right) -> {
            int byStatus = Integer.compare(statusRank(right.getStatus()), statusRank(left.getStatus()));
            if (byStatus != 0) {
                return byStatus;
            }
            int byHours = Integer.compare(right.getAssignedHours(), left.getAssignedHours());
            if (byHours != 0) {
                return byHours;
            }
            return safe(left.getApplicantName()).compareToIgnoreCase(safe(right.getApplicantName()));
        });
        return results;
    }

    public String evaluateStatus(int assignedHours, int maxHours) {
        if (assignedHours > maxHours) {
            return "OVERLOADED";
        }
        if (maxHours == 0 && assignedHours > 0) {
            return "OVERLOADED";
        }
        if (maxHours > 0 && assignedHours >= Math.ceil(maxHours * 0.8)) {
            return "AT_RISK";
        }
        return "BALANCED";
    }

    private String buildRecommendation(String status, int assignedHours, int maxHours) {
        return switch (status) {
            case "OVERLOADED" ->
                    "Assigned hours exceed the recommended limit ("
                            + assignedHours
                            + "/"
                            + maxHours
                            + "). Reassign this workload before confirming more positions.";
            case "AT_RISK" ->
                    "Assigned hours are close to the limit ("
                            + assignedHours
                            + "/"
                            + maxHours
                            + "). Review carefully before making another offer.";
            default -> "Workload is balanced and can support additional recruitment activity.";
        };
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private int statusRank(String status) {
        return switch (status) {
            case "OVERLOADED" -> 2;
            case "AT_RISK" -> 1;
            default -> 0;
        };
    }
}
