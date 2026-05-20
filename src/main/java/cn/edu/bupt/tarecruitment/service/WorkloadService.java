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
            if (!STATUS_SELECTED.equals(application.getStatus())) {
                continue;
            }

            Position position = positionIndex.get(application.getPositionId());
            if (position == null) {
                continue;
            }

            String applicantId = application.getApplicantId();

            assignedHours.merge(applicantId, position.getWeeklyHours(), Integer::sum);
            selectedCounts.merge(applicantId, 1, Integer::sum);
            moduleLabels
                    .computeIfAbsent(applicantId, key -> new ArrayList<>())
                    .add(position.getModuleCode() + " " + position.getModuleName());
        }

        List<WorkloadEntry> results = new ArrayList<>();
        for (ApplicantProfile applicant : applicants) {
            String applicantId = applicant.getId();
            int currentHours = assignedHours.getOrDefault(applicantId, 0);
            int maxHours = Math.max(applicant.getAvailableHoursPerWeek(), 0);
            int selectedPositions = selectedCounts.getOrDefault(applicantId, 0);
            String modules = String.join(" / ", moduleLabels.getOrDefault(applicantId, List.of()));
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

        results.sort(this::compareWorkloadEntries);
        return results;
    }

    public String evaluateStatus(int assignedHours, int maxHours) {
        if (assignedHours > maxHours) {
            return STATUS_OVERLOADED;
        }
        if (maxHours > 0 && assignedHours >= Math.ceil(maxHours * 0.8)) {
            return STATUS_AT_RISK;
        }
        return STATUS_BALANCED;
    }

    private int compareWorkloadEntries(WorkloadEntry left, WorkloadEntry right) {
        int byStatus = Integer.compare(statusRank(right.getStatus()), statusRank(left.getStatus()));
        if (byStatus != 0) {
            return byStatus;
        }

        int byHours = Integer.compare(right.getAssignedHours(), left.getAssignedHours());
        if (byHours != 0) {
            return byHours;
        }

        return safe(left.getApplicantName()).compareToIgnoreCase(safe(right.getApplicantName()));
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
