package cn.edu.bupt.tarecruitment.store;

import java.util.ArrayList;
import java.util.List;

public class SystemData {

    private List<UserAccount> accounts = new ArrayList<>();
    private List<ApplicantProfile> applicants = new ArrayList<>();
    private List<CvDocument> cvDocuments = new ArrayList<>();
    private List<Position> positions = new ArrayList<>();
    private List<ApplicationRecord> applications = new ArrayList<>();

    public SystemData() {
    }

    public List<UserAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<UserAccount> accounts) {
        this.accounts = accounts;
    }

    public List<ApplicantProfile> getApplicants() {
        return applicants;
    }

    public void setApplicants(List<ApplicantProfile> applicants) {
        this.applicants = applicants;
    }

    public List<CvDocument> getCvDocuments() {
        return cvDocuments;
    }

    public void setCvDocuments(List<CvDocument> cvDocuments) {
        this.cvDocuments = cvDocuments;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    public List<ApplicationRecord> getApplications() {
        return applications;
    }

    public void setApplications(List<ApplicationRecord> applications) {
        this.applications = applications;
    }


    public void ensureCollections() {
        accounts = (accounts == null) ? new ArrayList<>() : accounts;
        applicants = (applicants == null) ? new ArrayList<>() : applicants;
        cvDocuments = (cvDocuments == null) ? new ArrayList<>() : cvDocuments;
        positions = (positions == null) ? new ArrayList<>() : positions;
        applications = (applications == null) ? new ArrayList<>() : applications;
    }

}
