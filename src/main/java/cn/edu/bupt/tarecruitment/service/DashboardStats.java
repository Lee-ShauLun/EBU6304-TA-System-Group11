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
