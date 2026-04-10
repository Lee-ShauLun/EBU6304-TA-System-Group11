package cn.edu.bupt.tarecruitment.service;

public class DashboardStats {

    private final int applicantCount;
    private final int openPositionCount;
    private final int applicationCount;
    private final int selectedCount;

    public DashboardStats(
            int applicantCount, int openPositionCount, int applicationCount, int selectedCount) {
        this.applicantCount = applicantCount;
        this.openPositionCount = openPositionCount;
        this.applicationCount = applicationCount;
        this.selectedCount = selectedCount;
    }
    public boolean hasOpenPositions() {
        return openPositionCount > 0;
    }
    public boolean hasApplications() {
        return applicationCount > 0;
    }
    public double getSelectionRate() {
        if (applicationCount <= 0) {
            return 0.0;
        }
         return (double) selectedCount / applicationCount;
    }
    public boolean isSystemEmpty() {
        return applicantCount == 0 && openPositionCount == 0;
    }
    public int getApplicantCount() {
        return applicantCount;
    }

    public int getOpenPositionCount() {
        return openPositionCount;
    }

    public int getApplicationCount() {
        return applicationCount;
    }

    public int getSelectedCount() {
        return selectedCount;
    }
}
