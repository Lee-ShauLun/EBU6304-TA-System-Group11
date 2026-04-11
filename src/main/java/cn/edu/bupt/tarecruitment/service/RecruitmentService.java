package cn.edu.bupt.tarecruitment.service;

import cn.edu.bupt.tarecruitment.model.ApplicantProfile;
import cn.edu.bupt.tarecruitment.model.ApplicationRecord;
import cn.edu.bupt.tarecruitment.model.CvDocument;
import cn.edu.bupt.tarecruitment.model.Position;
import cn.edu.bupt.tarecruitment.model.UserAccount;
import cn.edu.bupt.tarecruitment.store.SystemData;
import cn.edu.bupt.tarecruitment.store.XmlDataStore;
import cn.edu.bupt.tarecruitment.util.HtmlUtil;
import cn.edu.bupt.tarecruitment.util.PasswordUtil;
import cn.edu.bupt.tarecruitment.web.UploadedFile;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class RecruitmentService {

    public static final String ROLE_APPLICANT = "APPLICANT";
    public static final String ROLE_RECRUITER = "RECRUITER";

    private final XmlDataStore dataStore;
    private final Path uploadsDirectory;
    private final MatchingService matchingService;
    private final WorkloadService workloadService;

    public RecruitmentService(XmlDataStore dataStore, Path uploadsDirectory) {
        this.dataStore = dataStore;
        this.uploadsDirectory = uploadsDirectory;
        this.matchingService = new MatchingService();
        this.workloadService = new WorkloadService();
    }

    public synchronized DashboardStats getDashboardStats() {
        SystemData data = dataStore.read();
        int openPositions =
                (int) data.getPositions().stream().filter(position -> "OPEN".equals(position.getStatus())).count();
        int selectedCount =
                (int)
                        data.getApplications().stream()
                                .filter(application -> "SELECTED".equals(application.getStatus()))
                                .count();
        return new DashboardStats(
                data.getApplicants().size(), openPositions, data.getApplications().size(), selectedCount);
    }

    
                
//YJY    修改部分I improved the error messages in registration and login to make them clearer, more user-friendly, and consistent in tone.
//I also made them more actionable, so users know how to Fix the issue.

public synchronized UserAccount registerAccount(
        String role, String username, String displayName, String password, String confirmPassword) {

    String normalizedRole = normalizeRole(role);
    String normalizedUsername = normalizeUsername(username);
    String cleanedDisplayName = clean(displayName);

    require(!cleanedDisplayName.isEmpty(), "Display name cannot be empty.");

    require(!normalizedUsername.isEmpty(), "Username cannot be empty.");

    require(
            normalizedUsername.matches("[a-z0-9._-]{4,32}"),
            "Username must be 4–32 characters long and may contain only lowercase letters, numbers, dots (.), underscores (_), or hyphens (-)."
    );

    require(
            password != null && password.length() >= 6,
            "Password must be at least 6 characters long."
    );

    require(
            password.equals(confirmPassword),
            "Passwords do not match. Please re-enter."
    );

    SystemData data = dataStore.read();

    boolean usernameTaken =
            data.getAccounts().stream()
                    .anyMatch(account -> normalizedUsername.equals(normalizeUsername(account.getUsername())));

    require(
            !usernameTaken,
            "This username is already taken. Please choose another one."
    );

    UserAccount account = new UserAccount();
    account.setId(UUID.randomUUID().toString());
    account.setRole(normalizedRole);
    account.setUsername(normalizedUsername);
    account.setDisplayName(cleanedDisplayName);
    account.setPasswordHash(PasswordUtil.sha256(password));
    account.setCreatedAt(now());
    data.getAccounts().add(account);

    if (ROLE_APPLICANT.equals(normalizedRole)) {
        ApplicantProfile profile = new ApplicantProfile();
        profile.setId(UUID.randomUUID().toString());
        profile.setAccountId(account.getId());
        profile.setFullName(cleanedDisplayName);
        profile.setEmail("");
        profile.setPhone("");
        profile.setMajor("");
        profile.setYearOfStudy("");
        profile.setSkills("");
        profile.setAvailableHoursPerWeek(0);
        profile.setCreatedAt(now());
        profile.setUpdatedAt(now());
        data.getApplicants().add(profile);
    }

    persist(data);
    return account;
}


public synchronized UserAccount authenticate(String role, String username, String password) {

    String normalizedRole = normalizeRole(role);
    String normalizedUsername = normalizeUsername(username);

    require(!normalizedUsername.isEmpty(), "Username cannot be empty.");
    require(password != null && !password.isEmpty(), "Password cannot be empty.");

    UserAccount account =
            dataStore.read().getAccounts().stream()
                    .filter(item -> normalizedRole.equals(item.getRole()))
                    .filter(item -> normalizedUsername.equals(normalizeUsername(item.getUsername())))
                    .findFirst()
                    .orElse(null);

    require(
            account != null,
            "No account found with the given username and role."
    );

    require(
            PasswordUtil.sha256(password).equals(account.getPasswordHash()),
            "Incorrect password. Please try again."
    );

    return account;
}

    public synchronized UserAccount findAccountById(String accountId) {
        if (HtmlUtil.isBlank(accountId)) {
            return null;
        }
        return dataStore.read().getAccounts().stream()
                .filter(account -> accountId.equals(account.getId()))
                .findFirst()
                .orElse(null);
    }

    public synchronized ApplicantProfile getApplicantProfileForAccount(String accountId) {
        if (HtmlUtil.isBlank(accountId)) {
            return null;
        }
        return dataStore.read().getApplicants().stream()
                .filter(profile -> accountId.equals(profile.getAccountId()))
                .findFirst()
                .orElse(null);
    }

    public synchronized ApplicantProfile saveApplicantProfileForAccount(
            String accountId, ApplicantProfile draft) {
        require(!HtmlUtil.isBlank(accountId), "You must sign in first.");
        require(!HtmlUtil.isBlank(draft.getFullName()), "Full name is required.");
        require(!HtmlUtil.isBlank(draft.getEmail()), "Email is required.");
        require(draft.getAvailableHoursPerWeek() >= 0, "Weekly availability cannot be negative.");

        SystemData data = dataStore.read();
        ApplicantProfile target = findOrCreateApplicantProfile(data, accountId);
        String currentTime = now();

        target.setAccountId(accountId);
        target.setFullName(clean(draft.getFullName()));
        target.setEmail(clean(draft.getEmail()));
        target.setPhone(clean(draft.getPhone()));
        target.setMajor(clean(draft.getMajor()));
        target.setYearOfStudy(clean(draft.getYearOfStudy()));
        target.setSkills(clean(draft.getSkills()));
        target.setAvailableHoursPerWeek(draft.getAvailableHoursPerWeek());
        if (HtmlUtil.isBlank(target.getCreatedAt())) {
            target.setCreatedAt(currentTime);
        }
        target.setUpdatedAt(currentTime);

        persist(data);
        return target;
    }

    public synchronized CvDocument uploadCvForAccount(String accountId, UploadedFile uploadedFile) {
        ApplicantProfile applicantProfile = getRequiredApplicantProfile(accountId);
        return uploadCv(applicantProfile.getId(), uploadedFile);
    }

    public synchronized CvDocument findLatestCvForAccount(String accountId) {
        ApplicantProfile applicantProfile = getApplicantProfileForAccount(accountId);
        if (applicantProfile == null) {
            return null;
        }
        return findLatestCvByApplicant(applicantProfile.getId());
    }

    public synchronized ApplicationRecord applyForPositionForAccount(
            String accountId, String positionId, String statement) {
        ApplicantProfile applicantProfile = getRequiredApplicantProfile(accountId);
        return applyForPosition(applicantProfile.getId(), positionId, statement);
    }

    public synchronized List<ApplicationRecord> listApplicationsForAccount(String accountId) {
        ApplicantProfile applicantProfile = getApplicantProfileForAccount(accountId);
        if (applicantProfile == null) {
            return List.of();
        }
        return listApplicationsForApplicant(applicantProfile.getId());
    }

    public synchronized MatchingResult previewMatchForAccount(String accountId, String positionId) {
        ApplicantProfile applicantProfile = getApplicantProfileForAccount(accountId);
        Position position = findPosition(positionId);
        if (applicantProfile == null || position == null) {
            return new MatchingResult(0, "", "", "Create your profile first to preview a match.");
        }
        return matchingService.calculate(applicantProfile, position);
    }

    public synchronized List<ApplicantProfile> listApplicants() {
        List<ApplicantProfile> applicants = new ArrayList<>(dataStore.read().getApplicants());
        applicants.sort(Comparator.comparing(applicant -> safe(applicant.getFullName())));
        return applicants;
    }

    public synchronized ApplicantProfile findApplicant(String applicantId) {
        if (HtmlUtil.isBlank(applicantId)) {
            return null;
        }
        return dataStore.read().getApplicants().stream()
                .filter(applicant -> applicantId.equals(applicant.getId()))
                .findFirst()
                .orElse(null);
    }

    public synchronized List<Position> listOpenPositions(String keyword) {
        return listPositions(keyword, false);
    }

    public synchronized List<Position> listAllPositions(String keyword) {
        return listPositions(keyword, true);
    }

    public synchronized Position findPosition(String positionId) {
        if (HtmlUtil.isBlank(positionId)) {
            return null;
        }
        return dataStore.read().getPositions().stream()
                .filter(position -> positionId.equals(position.getId()))
                .findFirst()
                .orElse(null);
    }

    public synchronized Position createPosition(Position draft) {
        require(!HtmlUtil.isBlank(draft.getModuleCode()), "Module code is required.");
        require(!HtmlUtil.isBlank(draft.getModuleName()), "Position title is required.");
        require(!HtmlUtil.isBlank(draft.getOrganiserName()), "Recruiter name is required.");
        require(!HtmlUtil.isBlank(draft.getOrganiserEmail()), "Recruiter email is required.");
        require(draft.getWeeklyHours() > 0, "Weekly hours must be greater than zero.");
        require(draft.getQuota() > 0, "Quota must be greater than zero.");

        Position position = new Position();
        position.setId(UUID.randomUUID().toString());
        position.setModuleCode(clean(draft.getModuleCode()));
        position.setModuleName(clean(draft.getModuleName()));
        position.setOrganiserName(clean(draft.getOrganiserName()));
        position.setOrganiserEmail(clean(draft.getOrganiserEmail()));
        position.setDescription(clean(draft.getDescription()));
        position.setRequiredSkills(clean(draft.getRequiredSkills()));
        position.setPreferredSkills(clean(draft.getPreferredSkills()));
        position.setWeeklyHours(draft.getWeeklyHours());
        position.setQuota(draft.getQuota());
        position.setStatus("OPEN");
        position.setCreatedAt(now());

        SystemData data = dataStore.read();
        data.getPositions().add(position);
        persist(data);
        return position;
    }

    public synchronized CvDocument uploadCv(String applicantId, UploadedFile uploadedFile) {
        require(!HtmlUtil.isBlank(applicantId), "Select an applicant profile first.");
        require(uploadedFile != null, "Please upload a CV file.");
        require(!HtmlUtil.isBlank(uploadedFile.getOriginalFileName()), "The CV file name is missing.");
        require(uploadedFile.getContent().length > 0, "The uploaded file is empty.");

        ApplicantProfile applicant = findApplicant(applicantId);
        require(applicant != null, "The applicant profile was not found.");

        String safeOriginalFileName = sanitizeFileName(uploadedFile.getOriginalFileName());
        String extension = extractExtension(safeOriginalFileName);
        String storedFileName = applicantId + "-" + System.currentTimeMillis() + extension;
        Path targetFile = uploadsDirectory.resolve(storedFileName).normalize();
        require(targetFile.startsWith(uploadsDirectory.normalize()), "Invalid file path.");

        try {
            Files.createDirectories(uploadsDirectory);
            Path tempFile = uploadsDirectory.resolve(storedFileName + ".tmp");
            Files.write(tempFile, uploadedFile.getContent());
            try {
                Files.move(
                        tempFile,
                        targetFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save the uploaded CV.", e);
        }

        CvDocument cvDocument = new CvDocument();
        cvDocument.setId(UUID.randomUUID().toString());
        cvDocument.setApplicantId(applicantId);
        cvDocument.setOriginalFileName(safeOriginalFileName);
        cvDocument.setStoredFileName(storedFileName);
        cvDocument.setContentType(uploadedFile.getContentType());
        cvDocument.setSize(uploadedFile.getContent().length);
        cvDocument.setUploadedAt(now());

        SystemData data = dataStore.read();
        data.getCvDocuments().add(cvDocument);
        persist(data);
        return cvDocument;
    }

    public synchronized CvDocument findLatestCvByApplicant(String applicantId) {
        if (HtmlUtil.isBlank(applicantId)) {
            return null;
        }
        return dataStore.read().getCvDocuments().stream()
                .filter(document -> applicantId.equals(document.getApplicantId()))
                .max(Comparator.comparing(document -> safe(document.getUploadedAt())))
                .orElse(null);
    }

    public synchronized List<CvDocument> listCvDocuments() {
        List<CvDocument> documents = new ArrayList<>(dataStore.read().getCvDocuments());
        documents.sort(
                Comparator.comparing(
                                CvDocument::getUploadedAt,
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                        .reversed());
        return documents;
    }

    public synchronized ApplicationRecord applyForPosition(
            String applicantId, String positionId, String statement) {
        require(!HtmlUtil.isBlank(applicantId), "Select your applicant profile before applying.");
        require(!HtmlUtil.isBlank(positionId), "Select a position first.");

        SystemData data = dataStore.read();

        ApplicantProfile applicant =
                data.getApplicants().stream()
                        .filter(item -> applicantId.equals(item.getId()))
                        .findFirst()
                        .orElse(null);
        require(applicant != null, "The applicant profile was not found.");

        Position position =
                data.getPositions().stream()
                        .filter(item -> positionId.equals(item.getId()))
                        .findFirst()
                        .orElse(null);
        require(position != null, "The selected position was not found.");
        require("OPEN".equals(position.getStatus()), "This position is no longer open.");
        require(findLatestCvByApplicant(applicantId) != null, "Upload a CV before submitting an application.");

        boolean duplicateApplication =
                data.getApplications().stream()
                        .anyMatch(
                                application ->
                                        applicantId.equals(application.getApplicantId())
                                                && positionId.equals(application.getPositionId()));
        require(!duplicateApplication, "You have already applied for this position.");

        MatchingResult matchingResult = matchingService.calculate(applicant, position);
        String currentTime = now();

        ApplicationRecord application = new ApplicationRecord();
        application.setId(UUID.randomUUID().toString());
        application.setApplicantId(applicantId);
        application.setPositionId(positionId);
        application.setStatement(clean(statement));
        application.setMatchScore(matchingResult.getScore());
        application.setMissingSkills(matchingResult.getMissingSkills());
        application.setStatus("PENDING");
        application.setAppliedAt(currentTime);
        application.setUpdatedAt(currentTime);

        data.getApplications().add(application);
        persist(data);
        return application;
    }

    public synchronized List<ApplicationRecord> listApplicationsForApplicant(String applicantId) {
        List<ApplicationRecord> applications = new ArrayList<>();
        for (ApplicationRecord application : dataStore.read().getApplications()) {
            if (applicantId != null && applicantId.equals(application.getApplicantId())) {
                applications.add(application);
            }
        }
        applications.sort(
                Comparator.comparing(
                                ApplicationRecord::getAppliedAt,
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                        .reversed());
        return applications;
    }

    public synchronized List<ApplicationRecord> listApplicationsForPosition(String positionId) {
        List<ApplicationRecord> applications = new ArrayList<>();
        for (ApplicationRecord application : dataStore.read().getApplications()) {
            if (positionId != null && positionId.equals(application.getPositionId())) {
                applications.add(application);
            }
        }
        applications.sort((left, right) -> {
            int byStatus = statusRank(left.getStatus()) - statusRank(right.getStatus());
            if (byStatus != 0) {
                return byStatus;
            }
            return Integer.compare(right.getMatchScore(), left.getMatchScore());
        });
        return applications;
    }

    public synchronized ApplicationRecord selectApplicant(
            String positionId, String applicationId, String note) {
        SystemData data = dataStore.read();

        Position position = findRequiredPosition(data, positionId);
        ApplicationRecord application = findRequiredApplication(data, applicationId);
        require(positionId.equals(application.getPositionId()), "The application does not belong to this position.");
        require("OPEN".equals(position.getStatus()), "This position cannot accept more selections.");
        require(!"SELECTED".equals(application.getStatus()), "This applicant has already been selected.");
        require(
                countSelectedApplications(data.getApplications(), positionId) < position.getQuota(),
                "This position has already reached its quota.");

        application.setStatus("SELECTED");
        application.setUpdatedAt(now());
        application.setDecisionNote(buildDecisionNote(data, application.getApplicantId(), clean(note)));

        if (countSelectedApplications(data.getApplications(), positionId) >= position.getQuota()) {
            position.setStatus("FILLED");
        }

        persist(data);
        return application;
    }

    public synchronized ApplicationRecord rejectApplicant(
            String positionId, String applicationId, String note) {
        SystemData data = dataStore.read();

        findRequiredPosition(data, positionId);
        ApplicationRecord application = findRequiredApplication(data, applicationId);
        require(positionId.equals(application.getPositionId()), "The application does not belong to this position.");
        require(!"SELECTED".equals(application.getStatus()), "A selected application cannot be rejected directly.");

        application.setStatus("REJECTED");
        application.setUpdatedAt(now());
        application.setDecisionNote(clean(note));

        persist(data);
        return application;
    }

    public synchronized int countSelectedForPosition(String positionId) {
        return countSelectedApplications(dataStore.read().getApplications(), positionId);
    }

    public synchronized List<WorkloadEntry> buildWorkloadReport() {
        SystemData data = dataStore.read();
        return workloadService.buildReport(
                data.getApplicants(), data.getPositions(), data.getApplications());
    }

    public synchronized Path resolveUpload(String storedFileName) {
        require(!HtmlUtil.isBlank(storedFileName), "File name is required.");
        Path file = uploadsDirectory.resolve(storedFileName).normalize();
        require(file.startsWith(uploadsDirectory.normalize()), "Invalid file path.");
        require(Files.exists(file), "The requested file does not exist.");
        return file;
    }

    public synchronized ApplicationRecord findApplication(String applicationId) {
        if (HtmlUtil.isBlank(applicationId)) {
            return null;
        }
        return dataStore.read().getApplications().stream()
                .filter(application -> applicationId.equals(application.getId()))
                .findFirst()
                .orElse(null);
    }

    private List<Position> listPositions(String keyword, boolean includeClosed) {
        String normalizedKeyword = safe(keyword).trim().toLowerCase(Locale.ROOT);
        List<Position> positions = new ArrayList<>();
        for (Position position : dataStore.read().getPositions()) {
            if (!includeClosed && !"OPEN".equals(position.getStatus())) {
                continue;
            }
            if (normalizedKeyword.isEmpty() || matches(position, normalizedKeyword)) {
                positions.add(position);
            }
        }
        positions.sort((left, right) -> safe(right.getCreatedAt()).compareTo(safe(left.getCreatedAt())));
        return positions;
    }

    private boolean matches(Position position, String keyword) {
        return contains(position.getModuleCode(), keyword)
                || contains(position.getModuleName(), keyword)
                || contains(position.getDescription(), keyword)
                || contains(position.getRequiredSkills(), keyword)
                || contains(position.getPreferredSkills(), keyword)
                || contains(position.getOrganiserName(), keyword);
    }

    private boolean contains(String source, String keyword) {
        return safe(source).toLowerCase(Locale.ROOT).contains(keyword);
    }

    private ApplicantProfile getRequiredApplicantProfile(String accountId) {
        ApplicantProfile applicantProfile = getApplicantProfileForAccount(accountId);
        require(applicantProfile != null, "Create your applicant profile first.");
        return applicantProfile;
    }

    private ApplicantProfile findOrCreateApplicantProfile(SystemData data, String accountId) {
        ApplicantProfile target =
                data.getApplicants().stream()
                        .filter(profile -> accountId.equals(profile.getAccountId()))
                        .findFirst()
                        .orElse(null);
        if (target != null) {
            return target;
        }

        target = new ApplicantProfile();
        target.setId(UUID.randomUUID().toString());
        target.setAccountId(accountId);
        target.setCreatedAt(now());
        data.getApplicants().add(target);
        return target;
    }

    private Position findRequiredPosition(SystemData data, String positionId) {
        Optional<Position> position =
                data.getPositions().stream()
                        .filter(item -> positionId.equals(item.getId()))
                        .findFirst();
        require(position.isPresent(), "The position was not found.");
        return position.orElseThrow();
    }

    private ApplicationRecord findRequiredApplication(SystemData data, String applicationId) {
        Optional<ApplicationRecord> application =
                data.getApplications().stream()
                        .filter(item -> applicationId.equals(item.getId()))
                        .findFirst();
        require(application.isPresent(), "The application record was not found.");
        return application.orElseThrow();
    }

    private int countSelectedApplications(List<ApplicationRecord> applications, String positionId) {
        int selectedCount = 0;
        for (ApplicationRecord application : applications) {
            if (positionId.equals(application.getPositionId()) && "SELECTED".equals(application.getStatus())) {
                selectedCount++;
            }
        }
        return selectedCount;
    }

    private int statusRank(String status) {
        return switch (safe(status)) {
            case "SELECTED" -> 0;
            case "PENDING" -> 1;
            case "REJECTED" -> 2;
            default -> 3;
        };
    }

    private String buildDecisionNote(SystemData data, String applicantId, String userNote) {
        List<ApplicationRecord> currentApplications = new ArrayList<>(data.getApplications());
        List<WorkloadEntry> report =
                workloadService.buildReport(data.getApplicants(), data.getPositions(), currentApplications);

        WorkloadEntry matchedEntry = null;
        for (WorkloadEntry entry : report) {
            if (applicantId.equals(entry.getApplicantId())) {
                matchedEntry = entry;
                break;
            }
        }

        String workloadNote =
                matchedEntry == null
                        ? ""
                        : "System note: " + matchedEntry.getRecommendation();

        if (HtmlUtil.isBlank(userNote)) {
            return workloadNote;
        }
        if (HtmlUtil.isBlank(workloadNote)) {
            return userNote;
        }
        return userNote + " | " + workloadNote;
    }

    private void persist(SystemData data) {
        try {
            dataStore.write(data);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write system data.", e);
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }

    private String normalizeRole(String role) {
        String normalized = safe(role).trim().toUpperCase(Locale.ROOT);
        require(
                ROLE_APPLICANT.equals(normalized) || ROLE_RECRUITER.equals(normalized),
                "Unsupported account role.");
        return normalized;
    }

    private String normalizeUsername(String username) {
        return safe(username).trim().toLowerCase(Locale.ROOT);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String now() {
        return OffsetDateTime.now().toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return fileName.substring(dotIndex);
    }

    private String sanitizeFileName(String fileName) {
        String normalized = fileName == null ? "" : fileName.replace("\\", "/");
        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex >= 0) {
            normalized = normalized.substring(slashIndex + 1);
        }
        return normalized.isBlank() ? "resume" : normalized;
    }
}
