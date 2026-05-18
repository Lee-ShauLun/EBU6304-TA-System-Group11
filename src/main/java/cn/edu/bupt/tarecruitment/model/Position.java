package cn.edu.bupt.tarecruitment.model;

public class Position {

    private String id;
    private String moduleCode;
    private String moduleName;
    private String organiserName;
    private String organiserEmail;
    private String description;
    private String requiredSkills;
    private String preferredSkills;
    private int weeklyHours;
    private int quota;
    private String status;
    private String createdAt;

    public Position() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getOrganiserName() {
        return organiserName;
    }

    public void setOrganiserName(String organiserName) {
        this.organiserName = organiserName;
    }

    public String getOrganiserEmail() {
        return organiserEmail;
    }

    public void setOrganiserEmail(String organiserEmail) {
        this.organiserEmail = organiserEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public String getPreferredSkills() {
        return preferredSkills;
    }

    public void setPreferredSkills(String preferredSkills) {
        this.preferredSkills = preferredSkills;
    }

    public int getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(int weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public int getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
