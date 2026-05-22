package cn.edu.bupt.tarecruitment.service;
import cn.edu.bupt.tarecruitment.model.ApplicantProfile;
import cn.edu.bupt.tarecruitment.model.ApplicationRecord;
import cn.edu.bupt.tarecruitment.model.Position;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkloadService {
    private static final String STATUS_SELECTED = "SELECTED";
    private static final String STATUS_OVERLOADED = "OVERLOADED";
    private static final String STATUS_AT_RISK = "AT_RISK";
    private static final String STATUS_BALANCED = "BALANCED";
    private static final double AT_RISK_THRESHOLD = 0.8d;
    private static final String MODULE_SEPARATOR = " / ";
    public List<WorkloadEntry> buildReport(
            List<ApplicantProfile> applicants,
            List<Position> positions,
            List<ApplicationRecord> applications) {
        List<ApplicantProfile> safeApplicants = applicants == null ? List.of() : applicants;
        List<Position> safePositions = positions == null ? List.of() : positions;
        List<ApplicationRecord> safeApplications = applications == null ? List.of() : applications;
        Map<String, Position> positionIndex = new HashMap<>();
        for (Position position : safePositions) {
            if (position == null || position.getId() == null) {
                continue;
            }
            positionIndex.put(position.getId(), position);
        }
        Map<String, Integer> assignedHours = new HashMap<>();
        Map<String, Integer> selectedCounts = new HashMap<>();
        Map<String, List<String>> moduleLabels = new HashMap<>();
        for (ApplicationRecord application : safeApplications) {
            if (application == null) {
                continue;
            }
            if (!STATUS_SELECTED.equals(application.getStatus())) {
                continue;
            }
            Position position = positionIndex.get(application.getPositionId());
            if (position == null) {
                continue;
            }
            String applicantId = application.getApplicantId();
            if (applicantId == null) {
                continue;
            }
            assignedHours.merge(applicantId, position.getWeeklyHours(), Integer::sum);
            selectedCounts.merge(applicantId, 1, Integer::sum);
            moduleLabels
                    .computeIfAbsent(applicantId, key -> new ArrayList<>())
                    .add(buildModuleLabel(position));
        }
        List<WorkloadEntry> results = new ArrayList<>();
        for (ApplicantProfile applicant : safeApplicants) {
            if (applicant == null) {
                continue;
            }
            String applicantId = applicant.getId();
            int currentHours = assignedHours.getOrDefault(applicantId, 0);
            int maxHours = Math.max(applicant.getAvailableHoursPerWeek(), 0);
            int selectedPositions = selectedCounts.getOrDefault(applicantId, 0);
            String modules =
                    String.join(
                            MODULE_SEPARATOR, moduleLabels.getOrDefault(applicantId, List.of()));
            String status = evaluateStatus(currentHours, maxHours);
            String recommendation = buildRecommendation(status, currentHours, maxHours);
            results.add(
                    new WorkloadEntry(
                            applicantId,
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
            return STATUS_OVERLOADED;
        }
        if (maxHours == 0 && assignedHours > 0) {
            return STATUS_OVERLOADED;
        }
        if (maxHours > 0 && assignedHours >= Math.ceil(maxHours * AT_RISK_THRESHOLD)) {
            return STATUS_AT_RISK;
        }
        return STATUS_BALANCED;
    }
    private String buildRecommendation(String status, int assignedHours, int maxHours) {
        return switch (status) {
            case STATUS_OVERLOADED ->
                    "Assigned hours exceed the recommended limit ("
                            + assignedHours
                            + "/"
                            + maxHours
                            + "). Reassign this workload before confirming more positions.";
            case STATUS_AT_RISK ->
                    "Assigned hours are close to the limit ("
                            + assignedHours
                            + "/"
                            + maxHours
                            + "). Review carefully before making another offer.";
            default -> "Workload is balanced and can support additional recruitment activity.";
        };
    }
    private String buildModuleLabel(Position position) {
        return safe(position.getModuleCode()) + " " + safe(position.getModuleName());
    }
    private String safe(String value) {
        return value == null ? "" : value;
    }
    private int statusRank(String status) {
        return switch (status) {
            case STATUS_OVERLOADED -> 2;
            case STATUS_AT_RISK -> 1;
            default -> 0;
        };
    }
}
