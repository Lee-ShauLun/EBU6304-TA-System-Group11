package cn.edu.bupt.tarecruitment;

import cn.edu.bupt.tarecruitment.model.ApplicantProfile;
import cn.edu.bupt.tarecruitment.model.ApplicationRecord;
import cn.edu.bupt.tarecruitment.model.Position;
import cn.edu.bupt.tarecruitment.model.UserAccount;
import cn.edu.bupt.tarecruitment.service.MatchingResult;
import cn.edu.bupt.tarecruitment.service.RecruitmentService;
import cn.edu.bupt.tarecruitment.service.ValidationException;
import cn.edu.bupt.tarecruitment.service.WorkloadEntry;
import cn.edu.bupt.tarecruitment.store.XmlDataStore;
import cn.edu.bupt.tarecruitment.web.UploadedFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class TestRunner {

    private int passed;
    private int total;

    private TestRunner() {
    }

    public static void main(String[] args) throws Exception {
        TestRunner runner = new TestRunner();
        runner.runAll();
    }

    private void runAll() throws Exception {
        Path tempRoot = Files.createTempDirectory("trs-tests-");
        try {
            RecruitmentService service = createService(tempRoot);

            testRegistrationAndAuthentication(service);
            testValidation(service);
            testMatchingAndApplicationWorkflow(service);
            testWorkloadStatus(service);
            testAdminRole(service);

            System.out.println("All tests passed: " + passed + "/" + total);
        } finally {
            deleteRecursively(tempRoot);
        }
    }

    private RecruitmentService createService(Path tempRoot) throws Exception {
        Path dataFile = tempRoot.resolve("data").resolve("trs-data.xml");
        Path uploadsDirectory = tempRoot.resolve("data").resolve("uploads");
        XmlDataStore dataStore = new XmlDataStore(dataFile, uploadsDirectory);
        dataStore.initializeIfMissing();
        return new RecruitmentService(dataStore, uploadsDirectory);
    }

    private void testRegistrationAndAuthentication(RecruitmentService service) {
        UserAccount account =
                service.registerAccount(
                        RecruitmentService.ROLE_APPLICANT,
                        "test_applicant",
                        "Test Applicant",
                        "password123",
                        "password123");

        assertEquals("APPLICANT", account.getRole(), "register applicant role");
        assertNotNull(
                service.authenticate(
                        RecruitmentService.ROLE_APPLICANT,
                        "test_applicant",
                        "password123"),
                "authenticate registered applicant");
        assertThrows(
                () -> service.registerAccount(
                        RecruitmentService.ROLE_APPLICANT,
                        "test_applicant",
                        "Duplicate Applicant",
                        "password123",
                        "password123"),
                "duplicate username validation");
        assertThrows(
                () -> service.authenticate(
                        RecruitmentService.ROLE_APPLICANT,
                        "test_applicant",
                        "wrong-password"),
                "wrong password validation");
    }

    private void testValidation(RecruitmentService service) {
        UserAccount account =
                service.registerAccount(
                        RecruitmentService.ROLE_APPLICANT,
                        "validation_applicant",
                        "Validation Applicant",
                        "password123",
                        "password123");

        ApplicantProfile invalidEmail = profile("Validation Applicant", "bad-email", 12);
        assertThrows(
                () -> service.saveApplicantProfileForAccount(account.getId(), invalidEmail),
                "invalid email rejected");

        ApplicantProfile tooManyHours = profile("Validation Applicant", "valid@example.com", 41);
        assertThrows(
                () -> service.saveApplicantProfileForAccount(account.getId(), tooManyHours),
                "availability upper bound rejected");

        Position invalidPosition = position("VAL101", "Validation Position", "bad-email", 6, 1);
        assertThrows(() -> service.createPosition(invalidPosition), "invalid organiser email rejected");

        assertThrows(
                () -> service.uploadCvForAccount(
                        account.getId(),
                        new UploadedFile(
                                "cvFile",
                                "resume.exe",
                                "application/octet-stream",
                                "not a cv".getBytes(StandardCharsets.UTF_8))),
                "invalid CV extension rejected");
    }

    private void testMatchingAndApplicationWorkflow(RecruitmentService service) {
        UserAccount account =
                service.registerAccount(
                        RecruitmentService.ROLE_APPLICANT,
                        "workflow_applicant",
                        "Workflow Applicant",
                        "password123",
                        "password123");
        service.saveApplicantProfileForAccount(
                account.getId(),
                profile("Workflow Applicant", "workflow@example.com", 12));

        Position position = service.listOpenPositions("Programming").get(0);
        MatchingResult match = service.previewMatchForAccount(account.getId(), position.getId());
        assertTrue(match.getScore() >= 70, "matching score uses profile skills");
        assertTrue(match.getExplanation().contains("Required skills"), "matching explanation is present");

        assertThrows(
                () -> service.applyForPositionForAccount(account.getId(), position.getId(), "I can help."),
                "application requires CV");

        service.uploadCvForAccount(
                account.getId(),
                new UploadedFile(
                        "cvFile",
                        "resume.txt",
                        "text/plain",
                        "Java, debugging, algorithms, git".getBytes(StandardCharsets.UTF_8)));
        ApplicationRecord application =
                service.applyForPositionForAccount(account.getId(), position.getId(), "I can help.");
        assertEquals("PENDING", application.getStatus(), "application starts pending");
        assertThrows(
                () -> service.applyForPositionForAccount(account.getId(), position.getId(), "Duplicate"),
                "duplicate application rejected");
    }

    private void testWorkloadStatus(RecruitmentService service) {
        UserAccount account =
                service.registerAccount(
                        RecruitmentService.ROLE_APPLICANT,
                        "workload_applicant",
                        "Workload Applicant",
                        "password123",
                        "password123");
        service.saveApplicantProfileForAccount(
                account.getId(),
                profile("Workload Applicant", "workload@example.com", 6));
        service.uploadCvForAccount(
                account.getId(),
                new UploadedFile(
                        "cvFile",
                        "workload.txt",
                        "text/plain",
                        "java, debugging, algorithms".getBytes(StandardCharsets.UTF_8)));

        Position position = service.listOpenPositions("Programming").get(0);
        ApplicationRecord application =
                service.applyForPositionForAccount(account.getId(), position.getId(), "Available for labs.");
        service.selectApplicant(position.getId(), application.getId(), "Strong match.");

        List<WorkloadEntry> report = service.buildWorkloadReport();
        WorkloadEntry entry =
                report.stream()
                        .filter(item -> "Workload Applicant".equals(item.getApplicantName()))
                        .findFirst()
                        .orElseThrow();
        assertEquals("OVERLOADED", entry.getStatus(), "workload overloaded detection");
    }

    private void testAdminRole(RecruitmentService service) {
        UserAccount admin =
                service.registerAccount(
                        RecruitmentService.ROLE_ADMIN,
                        "test_admin",
                        "Test Admin",
                        "password123",
                        "password123");
        assertEquals("ADMIN", admin.getRole(), "admin registration role");
        assertNotNull(
                service.authenticate(
                        RecruitmentService.ROLE_ADMIN,
                        "test_admin",
                        "password123"),
                "admin authentication");
    }

    private ApplicantProfile profile(String fullName, String email, int availableHours) {
        ApplicantProfile profile = new ApplicantProfile();
        profile.setFullName(fullName);
        profile.setEmail(email);
        profile.setPhone("123456789");
        profile.setMajor("Software Engineering");
        profile.setYearOfStudy("Year 3");
        profile.setSkills("java, debugging, algorithms, git, communication");
        profile.setAvailableHoursPerWeek(availableHours);
        return profile;
    }

    private Position position(String moduleCode, String moduleName, String email, int weeklyHours, int quota) {
        Position position = new Position();
        position.setModuleCode(moduleCode);
        position.setModuleName(moduleName);
        position.setOrganiserName("Dr. Validator");
        position.setOrganiserEmail(email);
        position.setDescription("Validation test position.");
        position.setRequiredSkills("java");
        position.setPreferredSkills("communication");
        position.setWeeklyHours(weeklyHours);
        position.setQuota(quota);
        return position;
    }

    private void assertEquals(Object expected, Object actual, String name) {
        total++;
        if (!expected.equals(actual)) {
            throw new AssertionError(name + " expected " + expected + " but was " + actual);
        }
        passed++;
        System.out.println("PASS " + name);
    }

    private void assertTrue(boolean condition, String name) {
        total++;
        if (!condition) {
            throw new AssertionError(name);
        }
        passed++;
        System.out.println("PASS " + name);
    }

    private void assertNotNull(Object value, String name) {
        assertTrue(value != null, name);
    }

    private void assertThrows(Runnable runnable, String name) {
        total++;
        try {
            runnable.run();
        } catch (ValidationException expected) {
            passed++;
            System.out.println("PASS " + name);
            return;
        }
        throw new AssertionError(name + " expected ValidationException");
    }

    private void deleteRecursively(Path root) throws Exception {
        if (root == null || Files.notExists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception ignored) {
                    // Best-effort cleanup for temporary test data.
                }
            });
        }
    }
}
