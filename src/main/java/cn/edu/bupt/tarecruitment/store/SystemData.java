package cn.edu.bupt.tarecruitment.store;

import cn.edu.bupt.tarecruitment.model.ApplicantProfile;
import cn.edu.bupt.tarecruitment.model.ApplicationRecord;
import cn.edu.bupt.tarecruitment.model.CvDocument;
import cn.edu.bupt.tarecruitment.model.Position;
import cn.edu.bupt.tarecruitment.model.UserAccount;
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
        if (accounts == null) {
            accounts = new ArrayList<>();
        }
        if (applicants == null) {
            applicants = new ArrayList<>();
        }
        if (cvDocuments == null) {
            cvDocuments = new ArrayList<>();
        }
        if (positions == null) {
            positions = new ArrayList<>();
        }
        if (applications == null) {
            applications = new ArrayList<>();
        }
    }
}
