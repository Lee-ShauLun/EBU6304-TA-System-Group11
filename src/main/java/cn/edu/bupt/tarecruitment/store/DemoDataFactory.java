package cn.edu.bupt.tarecruitment.store;

import cn.edu.bupt.tarecruitment.model.ApplicantProfile;
import cn.edu.bupt.tarecruitment.model.Position;
import cn.edu.bupt.tarecruitment.model.UserAccount;
import cn.edu.bupt.tarecruitment.service.RecruitmentService;
import cn.edu.bupt.tarecruitment.util.PasswordUtil;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class DemoDataFactory {

    private DemoDataFactory() {
    }

    public static SystemData create() {
        SystemData data = new SystemData();
        data.ensureCollections();

        String applicantAccountId = UUID.randomUUID().toString();
        data.getAccounts().add(account(
                applicantAccountId,
                RecruitmentService.ROLE_APPLICANT,
                "demo_applicant",
                "Alice Applicant"));
        data.getAccounts().add(account(
                UUID.randomUUID().toString(),
                RecruitmentService.ROLE_RECRUITER,
                "demo_recruiter",
                "Riley Recruiter"));
        data.getAccounts().add(account(
                UUID.randomUUID().toString(),
                RecruitmentService.ROLE_ADMIN,
                "demo_admin",
                "Avery Admin"));
        data.getApplicants().add(applicant(
                applicantAccountId,
                "Alice Applicant",
                "alice.applicant@example.com",
                "Software Engineering",
                "Year 3",
                "java, debugging, algorithms, git, communication",
                12));

        data.getPositions().add(position(
                "INT101",
                "Academic English Support",
                "Dr. Helen Carter",
                "helen.carter@bupt.edu.cn",
                "Support tutorials, answer student questions, and help prepare weekly worksheets.",
                "english writing, communication, tutorial facilitation",
                "canvas, grading, presentation",
                6,
                2));

        data.getPositions().add(position(
                "CS205",
                "Programming Fundamentals",
                "Prof. Chen Hao",
                "chenhao@bupt.edu.cn",
                "Assist with lab sessions, debug student code, and maintain office hour records.",
                "java, debugging, algorithms",
                "git, unit testing, patience",
                8,
                1));

        data.getPositions().add(position(
                "DS301",
                "Data Science Practice",
                "Dr. Sophia Li",
                "sophia.li@bupt.edu.cn",
                "Help with data-cleaning workshops and guide students through weekly notebooks.",
                "python, statistics, data analysis",
                "pandas, visualization, teamwork",
                7,
                1));

        return data;
    }

    private static UserAccount account(String id, String role, String username, String displayName) {
        UserAccount account = new UserAccount();
        account.setId(id);
        account.setRole(role);
        account.setUsername(username);
        account.setDisplayName(displayName);
        account.setPasswordHash(PasswordUtil.sha256("password123"));
        account.setCreatedAt(OffsetDateTime.now().toString());
        return account;
    }

    private static ApplicantProfile applicant(
            String accountId,
            String fullName,
            String email,
            String major,
            String yearOfStudy,
            String skills,
            int availableHours) {
        ApplicantProfile applicant = new ApplicantProfile();
        applicant.setId(UUID.randomUUID().toString());
        applicant.setAccountId(accountId);
        applicant.setFullName(fullName);
        applicant.setEmail(email);
        applicant.setPhone("");
        applicant.setMajor(major);
        applicant.setYearOfStudy(yearOfStudy);
        applicant.setSkills(skills);
        applicant.setAvailableHoursPerWeek(availableHours);
        applicant.setCreatedAt(OffsetDateTime.now().toString());
        applicant.setUpdatedAt(OffsetDateTime.now().toString());
        return applicant;
    }

    private static Position position(
            String moduleCode,
            String moduleName,
            String organiserName,
            String organiserEmail,
            String description,
            String requiredSkills,
            String preferredSkills,
            int weeklyHours,
            int quota) {

        Position position = new Position();
        position.setId(UUID.randomUUID().toString());
        position.setModuleCode(moduleCode);
        position.setModuleName(moduleName);
        position.setOrganiserName(organiserName);
        position.setOrganiserEmail(organiserEmail);
        position.setDescription(description);
        position.setRequiredSkills(requiredSkills);
        position.setPreferredSkills(preferredSkills);
        position.setWeeklyHours(weeklyHours);
        position.setQuota(quota);
        position.setStatus("OPEN");
        position.setCreatedAt(OffsetDateTime.now().toString());
        return position;
    }
}
