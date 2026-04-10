package cn.edu.bupt.tarecruitment.service;

public class WorkloadEntry {

    private final String applicantId;
    private final String applicantName;
    private final int assignedHours;
    private final int maxHours;
    private final int selectedPositionCount;
    private final String modules;
    private final String status;
    private final String recommendation;

    public WorkloadEntry(
            String applicantId,
            String applicantName,
            int assignedHours,
            int maxHours,
            int selectedPositionCount,
            String modules,
            String status,
            String recommendation) {
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.assignedHours = assignedHours;
        this.maxHours = maxHours;
        this.selectedPositionCount = selectedPositionCount;
        this.modules = modules;
        this.status = status;
        this.recommendation = recommendation;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public int getAssignedHours() {
        return assignedHours;
    }

    public int getMaxHours() {
        return maxHours;
    }

    public int getSelectedPositionCount() {
        return selectedPositionCount;
    }

    public String getModules() {
        return modules;
    }

    public String getStatus() {
        return status;
    }

    public String getRecommendation() {
        return recommendation;
    }
}
