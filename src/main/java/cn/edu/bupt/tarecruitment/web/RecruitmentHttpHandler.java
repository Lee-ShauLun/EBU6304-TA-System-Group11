package cn.edu.bupt.tarecruitment.web;

import cn.edu.bupt.tarecruitment.model.ApplicantProfile;
import cn.edu.bupt.tarecruitment.model.ApplicationRecord;
import cn.edu.bupt.tarecruitment.model.CvDocument;
import cn.edu.bupt.tarecruitment.model.Position;
import cn.edu.bupt.tarecruitment.model.UserAccount;
import cn.edu.bupt.tarecruitment.service.DashboardStats;
import cn.edu.bupt.tarecruitment.service.MatchingResult;
import cn.edu.bupt.tarecruitment.service.RecruitmentService;
import cn.edu.bupt.tarecruitment.service.ValidationException;
import cn.edu.bupt.tarecruitment.service.WorkloadEntry;
import cn.edu.bupt.tarecruitment.util.HtmlUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Routes HTTP requests to role-specific recruitment workflows.
 */
public class RecruitmentHttpHandler implements HttpHandler {

    private static final String SESSION_COOKIE = "trs_session";

    private final RecruitmentService recruitmentService;
    private final HtmlRenderer renderer;
    private final Map<String, UserSession> sessions;

    public RecruitmentHttpHandler(RecruitmentService recruitmentService) {
        this.recruitmentService = recruitmentService;
        this.renderer = new HtmlRenderer();
        this.sessions = new ConcurrentHashMap<>();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = normalizePath(exchange.getRequestURI().getPath());
        UserSession session = currentSession(exchange);
        UserAccount account = session == null ? null : recruitmentService.findAccountById(session.accountId());

        if (session != null && account == null) {
            sessions.remove(session.sessionId());
            session = null;
        }

        try {
            if (legacyPath(path)) {
                if (session == null) {
                    WebUtil.redirect(exchange, "/");
                } else {
                    WebUtil.redirect(exchange, defaultPortalPath(session.role()));
                }
                return;
            }

            if ("GET".equals(method) && "/".equals(path)) {
                handleLandingPage(exchange, session);
                return;
            }
            if ("GET".equals(method) && "/login".equals(path)) {
                handleLoginPage(exchange, session);
                return;
            }
            if ("POST".equals(method) && "/login".equals(path)) {
                handleLoginSubmit(exchange);
                return;
            }
            if ("GET".equals(method) && "/register".equals(path)) {
                handleRegisterPage(exchange, session);
                return;
            }
            if ("POST".equals(method) && "/register".equals(path)) {
                handleRegisterSubmit(exchange);
                return;
            }
            if ("POST".equals(method) && "/logout".equals(path)) {
                handleLogout(exchange, session);
                return;
            }
            if ("GET".equals(method) && "/uploads".equals(path)) {
                requireSignedIn(session);
                handleDownload(exchange);
                return;
            }

            if (path.startsWith("/applicant")) {
                handleApplicantRoutes(exchange, method, path, session, account);
                return;
            }
            if (path.startsWith("/recruiter")) {
                handleRecruiterRoutes(exchange, method, path, session, account);
                return;
            }
            if (path.startsWith("/admin")) {
                handleAdminRoutes(exchange, method, path, session, account);
                return;
            }

            WebUtil.sendHtml(exchange, 404, renderer.renderErrorPage("Page not found", "The requested page does not exist."));
        } catch (ValidationException e) {
            WebUtil.sendHtml(exchange, 400, renderer.renderErrorPage("Invalid request", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            WebUtil.sendHtml(
                    exchange,
                    500,
                    renderer.renderErrorPage(
                            "Server error",
                            "Something went wrong while processing the request. Please try again."));
        }
    }

    private void handleLandingPage(HttpExchange exchange, UserSession session) throws IOException {
    if (session != null) {
        WebUtil.redirect(exchange, defaultPortalPath(session.role()));
        return;
    }

    String rawQuery = exchange.getRequestURI().getRawQuery();
    Map<String, String> query = (rawQuery != null && !rawQuery.isEmpty()) 
            ? WebUtil.parseQuery(rawQuery) 
            : Collections.emptyMap();

    String notice = query.get("notice");
    String error = query.get("error");

    WebUtil.sendHtml(
            exchange,
            HttpURLConnection.HTTP_OK, 
            renderer.renderLandingPage(notice, error)
    );
}

    private void handleLoginPage(HttpExchange exchange, UserSession session) throws IOException {
        if (session != null) {
            WebUtil.redirect(exchange, defaultPortalPath(session.role()));
            return;
        }

        Map<String, String> query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        String role = normalizedRole(query.getOrDefault("role", RecruitmentService.ROLE_APPLICANT));
        WebUtil.sendHtml(
                exchange,
                200,
                renderer.renderLoginPage(role, query.get("notice"), query.get("error")));
    }

    private void handleLoginSubmit(HttpExchange exchange) throws IOException {
        Map<String, String> form = WebUtil.parseFormBody(exchange);
        String role = normalizedRole(form.get("role"));

        try {
            UserAccount account =
                    recruitmentService.authenticate(role, form.get("username"), form.get("password"));
            UserSession session = createSession(account);
            exchange.getResponseHeaders().add("Set-Cookie", sessionCookie(session.sessionId()));
            WebUtil.redirect(exchange, buildUrl(defaultPortalPath(account.getRole()), mapOf("notice", "Signed in successfully.")));
        } catch (ValidationException e) {
            WebUtil.redirect(
                    exchange,
                    buildUrl(
                            "/login",
                            mapOf("role", role, "error", e.getMessage())));
        }
    }

    private void handleRegisterPage(HttpExchange exchange, UserSession session) throws IOException {
    if (session != null) {
        WebUtil.redirect(exchange, defaultPortalPath(session.role()));
        return;
    }

    var query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
    
    // 提前准备好渲染所需的各个参数
    var role = normalizedRole(query.getOrDefault("role", RecruitmentService.ROLE_APPLICANT));
    var noticeMsg = query.get("notice");
    var errorMsg = query.get("error");
    
    // 渲染并发送响应
    var htmlContent = renderer.renderRegisterPage(role, noticeMsg, errorMsg);
    WebUtil.sendHtml(exchange, 200, htmlContent);
}

    private void handleRegisterSubmit(HttpExchange exchange) throws IOException {
        Map<String, String> form = WebUtil.parseFormBody(exchange);
        String role = normalizedRole(form.get("role"));

        try {
            UserAccount account =
                    recruitmentService.registerAccount(
                            role,
                            form.get("username"),
                            form.get("displayName"),
                            form.get("password"),
                            form.get("confirmPassword"));
            UserSession session = createSession(account);
            exchange.getResponseHeaders().add("Set-Cookie", sessionCookie(session.sessionId()));
            String redirectPath =
                    switch (role) {
                        case RecruitmentService.ROLE_APPLICANT -> "/applicant/profile";
                        case RecruitmentService.ROLE_ADMIN -> "/admin";
                        default -> "/recruiter";
                    };
            WebUtil.redirect(
                    exchange,
                    buildUrl(redirectPath, mapOf("notice", "Account created successfully.")));
        } catch (ValidationException e) {
            WebUtil.redirect(
                    exchange,
                    buildUrl(
                            "/register",
                            mapOf("role", role, "error", e.getMessage())));
        }
    }

    private void handleLogout(HttpExchange exchange, UserSession session) throws IOException {
        if (session != null) {
            sessions.remove(session.sessionId());
        }
        exchange.getResponseHeaders().add("Set-Cookie", clearSessionCookie());
        WebUtil.redirect(exchange, buildUrl("/", mapOf("notice", "You have signed out.")));
    }

    private void handleApplicantRoutes(
            HttpExchange exchange,
            String method,
            String path,
            UserSession session,
            UserAccount account)
            throws IOException {
        UserAccount applicantAccount = requireRole(exchange, session, account, RecruitmentService.ROLE_APPLICANT);
        if (applicantAccount == null) {
            return;
        }

        if ("GET".equals(method) && "/applicant".equals(path)) {
            handleApplicantDashboard(exchange, applicantAccount);
            return;
        }
        if ("GET".equals(method) && "/applicant/profile".equals(path)) {
            handleApplicantProfilePage(exchange, applicantAccount);
            return;
        }
        if ("POST".equals(method) && "/applicant/profile".equals(path)) {
            handleApplicantProfileSave(exchange, applicantAccount);
            return;
        }
        if ("GET".equals(method) && "/applicant/cv".equals(path)) {
            handleApplicantCvPage(exchange, applicantAccount);
            return;
        }
        if ("POST".equals(method) && "/applicant/cv".equals(path)) {
            handleApplicantCvUpload(exchange, applicantAccount);
            return;
        }
        if ("GET".equals(method) && "/applicant/positions".equals(path)) {
            handleApplicantPositionsPage(exchange, applicantAccount);
            return;
        }
        if ("POST".equals(method) && "/applicant/applications".equals(path)) {
            handleApplicantApplicationSubmit(exchange, applicantAccount);
            return;
        }
        if ("GET".equals(method) && "/applicant/applications".equals(path)) {
            handleApplicantApplicationsPage(exchange, applicantAccount);
            return;
        }

        WebUtil.sendHtml(exchange, 404, renderer.renderErrorPage("Page not found", "The applicant page does not exist."));
    }

    private void handleRecruiterRoutes(
            HttpExchange exchange,
            String method,
            String path,
            UserSession session,
            UserAccount account)
            throws IOException {
        UserAccount recruiterAccount = requireRole(exchange, session, account, RecruitmentService.ROLE_RECRUITER);
        if (recruiterAccount == null) {
            return;
        }

        if ("GET".equals(method) && "/recruiter".equals(path)) {
            handleRecruiterDashboard(exchange, recruiterAccount);
            return;
        }
        if ("GET".equals(method) && "/recruiter/positions".equals(path)) {
            handleRecruiterPositionsPage(exchange, recruiterAccount);
            return;
        }
        if ("POST".equals(method) && "/recruiter/positions".equals(path)) {
            handleRecruiterPositionCreate(exchange);
            return;
        }
        if ("GET".equals(method) && "/recruiter/workload".equals(path)) {
            handleRecruiterWorkloadPage(exchange, recruiterAccount);
            return;
        }

        String[] segments = pathSegments(path);
        if ("GET".equals(method)
                && segments.length == 3
                && "recruiter".equals(segments[0])
                && "positions".equals(segments[1])) {
            handleRecruiterPositionDetailPage(exchange, recruiterAccount, segments[2]);
            return;
        }
        if ("POST".equals(method)
                && segments.length == 4
                && "recruiter".equals(segments[0])
                && "positions".equals(segments[1])
                && "select".equals(segments[3])) {
            handleRecruiterSelection(exchange, segments[2]);
            return;
        }
        if ("POST".equals(method)
                && segments.length == 4
                && "recruiter".equals(segments[0])
                && "positions".equals(segments[1])
                && "reject".equals(segments[3])) {
            handleRecruiterRejection(exchange, segments[2]);
            return;
        }

        WebUtil.sendHtml(exchange, 404, renderer.renderErrorPage("Page not found", "The recruiter page does not exist."));
    }

    private void handleAdminRoutes(
            HttpExchange exchange,
            String method,
            String path,
            UserSession session,
            UserAccount account)
            throws IOException {
        UserAccount adminAccount = requireRole(exchange, session, account, RecruitmentService.ROLE_ADMIN);
        if (adminAccount == null) {
            return;
        }

        if ("GET".equals(method) && "/admin".equals(path)) {
            handleAdminDashboard(exchange, adminAccount);
            return;
        }
        if ("GET".equals(method) && "/admin/workload".equals(path)) {
            handleAdminWorkloadPage(exchange, adminAccount);
            return;
        }
        if ("GET".equals(method) && "/admin/applications".equals(path)) {
            handleAdminApplicationsPage(exchange, adminAccount);
            return;
        }

        WebUtil.sendHtml(exchange, 404, renderer.renderErrorPage("Page not found", "The admin page does not exist."));
    }

    private void handleApplicantDashboard(HttpExchange exchange, UserAccount account) throws IOException {
        Map<String, String> query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        ApplicantProfile profile = recruitmentService.getApplicantProfileForAccount(account.getId());
        CvDocument latestCv = recruitmentService.findLatestCvForAccount(account.getId());
        List<ApplicationRecord> applications = recruitmentService.listApplicationsForAccount(account.getId());
        int selectedCount =
                (int) applications.stream().filter(application -> "SELECTED".equals(application.getStatus())).count();

        WebUtil.sendHtml(
                exchange,
                200,
                renderer.renderApplicantDashboard(
                        account,
                        profile,
                        latestCv,
                        recruitmentService.listOpenPositions("").size(),
                        applications.size(),
                        selectedCount,
                        query.get("notice"),
                        query.get("error")));
    }

    private void handleApplicantProfilePage(HttpExchange exchange, UserAccount account) throws IOException {
        Map<String, String> query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        ApplicantProfile profile = recruitmentService.getApplicantProfileForAccount(account.getId());
        CvDocument latestCv = recruitmentService.findLatestCvForAccount(account.getId());
        WebUtil.sendHtml(
                exchange,
                200,
                renderer.renderApplicantProfile(
                        account, profile, latestCv, query.get("notice"), query.get("error")));
    }

    private void handleApplicantProfileSave(HttpExchange exchange, UserAccount account) throws IOException {
        Map<String, String> form = WebUtil.parseFormBody(exchange);
        ApplicantProfile profile = new ApplicantProfile();
        profile.setFullName(form.get("fullName"));
        profile.setEmail(form.get("email"));
        profile.setPhone(form.get("phone"));
        profile.setMajor(form.get("major"));
        profile.setYearOfStudy(form.get("yearOfStudy"));
        profile.setSkills(form.get("skills"));
        profile.setAvailableHoursPerWeek(parseInt(form.get("availableHoursPerWeek"), 0));

        try {
            recruitmentService.saveApplicantProfileForAccount(account.getId(), profile);
            WebUtil.redirect(
                    exchange,
                    buildUrl("/applicant/profile", mapOf("notice", "Profile saved successfully.")));
        } catch (ValidationException e) {
            WebUtil.redirect(
                    exchange,
                    buildUrl("/applicant/profile", mapOf("error", e.getMessage())));
        }
    }

    private void handleApplicantCvPage(HttpExchange exchange, UserAccount account) throws IOException {
        Map<String, String> query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        ApplicantProfile profile = recruitmentService.getApplicantProfileForAccount(account.getId());
        CvDocument latestCv = recruitmentService.findLatestCvForAccount(account.getId());
        WebUtil.sendHtml(
                exchange,
                200,
                renderer.renderApplicantCv(
                        account, profile, latestCv, query.get("notice"), query.get("error")));
    }

    private void handleApplicantCvUpload(HttpExchange exchange, UserAccount account) throws IOException {
        MultipartFormData multipart = WebUtil.parseMultipart(exchange);
        UploadedFile uploadedFile = multipart.getFiles().get("cvFile");

        try {
            recruitmentService.uploadCvForAccount(account.getId(), uploadedFile);
            WebUtil.redirect(
                    exchange,
                    buildUrl("/applicant/cv", mapOf("notice", "CV uploaded successfully.")));
        } catch (ValidationException e) {
            WebUtil.redirect(
                    exchange,
                    buildUrl("/applicant/cv", mapOf("error", e.getMessage())));
        }
    }

    private void handleApplicantPositionsPage(HttpExchange exchange, UserAccount account) throws IOException {
        Map<String, String> query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        ApplicantProfile profile = recruitmentService.getApplicantProfileForAccount(account.getId());
        String keyword = query.getOrDefault("keyword", "");
        List<Position> positions = recruitmentService.listOpenPositions(keyword);
        List<ApplicationRecord> applications = recruitmentService.listApplicationsForAccount(account.getId());
        Map<String, MatchingResult> matchingResults = new HashMap<>();
        for (Position position : positions) {
            matchingResults.put(
                    position.getId(),
                    recruitmentService.previewMatchForAccount(account.getId(), position.getId()));
        }

        WebUtil.sendHtml(
                exchange,
                200,
                renderer.renderApplicantPositions(
                        account,
                        profile,
                        positions,
                        applications,
                        matchingResults,
                        keyword,
                        query.get("notice"),
                        query.get("error")));
    }

    private void handleApplicantApplicationSubmit(HttpExchange exchange, UserAccount account) throws IOException {
        Map<String, String> form = WebUtil.parseFormBody(exchange);
        try {
            recruitmentService.applyForPositionForAccount(
                    account.getId(), form.get("positionId"), form.get("statement"));
            WebUtil.redirect(
                    exchange,
                    buildUrl("/applicant/positions", mapOf("notice", "Application submitted successfully.")));
        } catch (ValidationException e) {
            WebUtil.redirect(
                    exchange,
                    buildUrl("/applicant/positions", mapOf("error", e.getMessage())));
        }
    }

    private void handleApplicantApplicationsPage(HttpExchange exchange, UserAccount account) throws IOException {
        Map<String, String> query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        List<ApplicationRecord> applications = recruitmentService.listApplicationsForAccount(account.getId());
        Map<String, Position> positionIndex = new HashMap<>();
        for (Position position : recruitmentService.listAllPositions("")) {
            positionIndex.put(position.getId(), position);
        }
        WebUtil.sendHtml(
                exchange,
                200,
                renderer.renderApplicantApplications(
                        account, applications, positionIndex, query.get("notice"), query.get("error")));
    }

    private void handleRecruiterDashboard(HttpExchange exchange, UserAccount account) throws IOException {
        Map<String, String> query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        DashboardStats stats = recruitmentService.getDashboardStats();
        List<Position> positions = recruitmentService.listAllPositions("");
        WebUtil.sendHtml(
                exchange,
                200,
                renderer.renderRecruiterDashboard(
                        account, stats, positions, query.get("notice"), query.get("error")));
    }

    private void handleRecruiterPositionsPage(HttpExchange exchange, UserAccount account) throws IOException {
        Map<String, String> query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        List<Position> positions = recruitmentService.listAllPositions("");
        Map<String, Integer> selectedCounts = new HashMap<>();
        for (Position position : positions) {
            selectedCounts.put(position.getId(), recruitmentService.countSelectedForPosition(position.getId()));
        }
        WebUtil.sendHtml(
                exchange,
                200,
                renderer.renderRecruiterPositions(
                        account, positions, selectedCounts, query.get("notice"), query.get("error")));
    }

    private void handleRecruiterPositionCreate(HttpExchange exchange) throws IOException {
        Map<String, String> form = WebUtil.parseFormBody(exchange);
        Position position = new Position();
        position.setModuleCode(form.get("moduleCode"));
        position.setModuleName(form.get("moduleName"));
        position.setOrganiserName(form.get("organiserName"));
        position.setOrganiserEmail(form.get("organiserEmail"));
        position.setDescription(form.get("description"));
        position.setRequiredSkills(form.get("requiredSkills"));
        position.setPreferredSkills(form.get("preferredSkills"));
        position.setWeeklyHours(parseInt(form.get("weeklyHours"), 0));
        position.setQuota(parseInt(form.get("quota"), 1));

        try {
            recruitmentService.createPosition(position);
            WebUtil.redirect(
                    exchange,
                    buildUrl("/recruiter/positions", mapOf("notice", "Position published successfully.")));
        } catch (ValidationException e) {
            WebUtil.redirect(
                    exchange,
                    buildUrl("/recruiter/positions", mapOf("error", e.getMessage())));
        }
    }

    private void handleRecruiterPositionDetailPage(
            HttpExchange exchange, UserAccount account, String positionId) throws IOException {
        Map<String, String> query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        Position position = recruitmentService.findPosition(positionId);
        if (position == null) {
            WebUtil.sendHtml(exchange, 404, renderer.renderErrorPage("Position not found", "The requested position does not exist."));
            return;
        }

        List<ApplicationRecord> applications = recruitmentService.listApplicationsForPosition(positionId);
        Map<String, ApplicantProfile> applicantIndex = new HashMap<>();
        for (ApplicantProfile applicant : recruitmentService.listApplicants()) {
            applicantIndex.put(applicant.getId(), applicant);
        }
        Map<String, CvDocument> latestCvIndex = latestCvIndex();
        WebUtil.sendHtml(
                exchange,
                200,
                renderer.renderRecruiterPositionDetail(
                        account,
                        position,
                        applications,
                        applicantIndex,
                        latestCvIndex,
                        recruitmentService.countSelectedForPosition(positionId),
                        query.get("notice"),
                        query.get("error")));
    }

    private void handleRecruiterSelection(HttpExchange exchange, String positionId) throws IOException {
        Map<String, String> form = WebUtil.parseFormBody(exchange);
        try {
            recruitmentService.selectApplicant(positionId, form.get("applicationId"), form.get("note"));
            WebUtil.redirect(
                    exchange,
                    buildUrl(
                            "/recruiter/positions/" + positionId,
                            mapOf("notice", "Applicant selected successfully.")));
        } catch (ValidationException e) {
            WebUtil.redirect(
                    exchange,
                    buildUrl(
                            "/recruiter/positions/" + positionId,
                            mapOf("error", e.getMessage())));
        }
    }

    private void handleRecruiterRejection(HttpExchange exchange, String positionId) throws IOException {
        Map<String, String> form = WebUtil.parseFormBody(exchange);
        try {
            recruitmentService.rejectApplicant(positionId, form.get("applicationId"), form.get("note"));
            WebUtil.redirect(
                    exchange,
                    buildUrl(
                            "/recruiter/positions/" + positionId,
                            mapOf("notice", "Application rejected successfully.")));
        } catch (ValidationException e) {
            WebUtil.redirect(
                    exchange,
                    buildUrl(
                            "/recruiter/positions/" + positionId,
                            mapOf("error", e.getMessage())));
        }
    }

    private void handleRecruiterWorkloadPage(HttpExchange exchange, UserAccount account) throws IOException {
        Map<String, String> query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        List<WorkloadEntry> workloadEntries = recruitmentService.buildWorkloadReport();
        WebUtil.sendHtml(
                exchange,
                200,
                renderer.renderRecruiterWorkload(
                        account, workloadEntries, query.get("notice"), query.get("error")));
    }

    private void handleAdminDashboard(HttpExchange exchange, UserAccount account) throws IOException {
        Map<String, String> query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        WebUtil.sendHtml(
                exchange,
                200,
                renderer.renderAdminDashboard(
                        account,
                        recruitmentService.getDashboardStats(),
                        recruitmentService.buildWorkloadReport(),
                        allApplications(),
                        query.get("notice"),
                        query.get("error")));
    }

    private void handleAdminWorkloadPage(HttpExchange exchange, UserAccount account) throws IOException {
        Map<String, String> query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        WebUtil.sendHtml(
                exchange,
                200,
                renderer.renderAdminWorkload(
                        account,
                        recruitmentService.buildWorkloadReport(),
                        query.get("notice"),
                        query.get("error")));
    }

    private void handleAdminApplicationsPage(HttpExchange exchange, UserAccount account) throws IOException {
        Map<String, String> query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        Map<String, ApplicantProfile> applicantIndex = new HashMap<>();
        for (ApplicantProfile applicant : recruitmentService.listApplicants()) {
            applicantIndex.put(applicant.getId(), applicant);
        }
        Map<String, Position> positionIndex = new HashMap<>();
        for (Position position : recruitmentService.listAllPositions("")) {
            positionIndex.put(position.getId(), position);
        }
        WebUtil.sendHtml(
                exchange,
                200,
                renderer.renderAdminApplications(
                        account,
                        allApplications(),
                        applicantIndex,
                        positionIndex,
                        query.get("notice"),
                        query.get("error")));
    }

    private void handleDownload(HttpExchange exchange) throws IOException {
        Map<String, String> query = WebUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        String storedFileName = query.get("file");
        String requestedName = query.get("name");

        CvDocument document = null;
        for (CvDocument item : recruitmentService.listCvDocuments()) {
            if (storedFileName != null && storedFileName.equals(item.getStoredFileName())) {
                document = item;
                break;
            }
        }
        if (document == null) {
            WebUtil.sendHtml(exchange, 404, renderer.renderErrorPage("File not found", "The requested CV file does not exist."));
            return;
        }

        Path file = recruitmentService.resolveUpload(storedFileName);
        WebUtil.sendFile(
                exchange,
                file,
                document.getContentType(),
                HtmlUtil.isBlank(requestedName) ? document.getOriginalFileName() : requestedName);
    }

    private UserAccount requireRole(
            HttpExchange exchange,
            UserSession session,
            UserAccount account,
            String requiredRole)
            throws IOException {
        if (session == null || account == null) {
            WebUtil.redirect(
                    exchange,
                    buildUrl("/login", mapOf("role", requiredRole, "error", "Please sign in first.")));
            return null;
        }
        if (!requiredRole.equals(account.getRole())) {
            WebUtil.redirect(
                    exchange,
                    buildUrl(
                            defaultPortalPath(account.getRole()),
                            mapOf("error", "You do not have access to that portal.")));
            return null;
        }
        return account;
    }

    private void requireSignedIn(UserSession session) {
        if (session == null) {
            throw new ValidationException("Please sign in first.");
        }
    }

    private Map<String, CvDocument> latestCvIndex() {
        Map<String, CvDocument> index = new HashMap<>();
        for (CvDocument document : recruitmentService.listCvDocuments()) {
            index.putIfAbsent(document.getApplicantId(), document);
        }
        return index;
    }

    private List<ApplicationRecord> allApplications() {
        return recruitmentService.listAllPositions("").stream()
                .flatMap(position -> recruitmentService.listApplicationsForPosition(position.getId()).stream())
                .toList();
    }

    private UserSession createSession(UserAccount account) {
        String sessionId = UUID.randomUUID().toString();
        UserSession session = new UserSession(sessionId, account.getId(), account.getRole());
        sessions.put(sessionId, session);
        return session;
    }

    private UserSession currentSession(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null || cookieHeader.isBlank()) {
            return null;
        }

        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String trimmed = cookie.trim();
            if (trimmed.startsWith(SESSION_COOKIE + "=")) {
                String sessionId = trimmed.substring((SESSION_COOKIE + "=").length());
                return sessions.get(sessionId);
            }
        }
        return null;
    }

    private boolean legacyPath(String path) {
        return path.startsWith("/ta") || path.startsWith("/mo");
    }

    private String sessionCookie(String sessionId) {
        return SESSION_COOKIE + "=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax";
    }

    private String clearSessionCookie() {
        return SESSION_COOKIE + "=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0";
    }

    private String defaultPortalPath(String role) {
        if (RecruitmentService.ROLE_RECRUITER.equals(role)) {
            return "/recruiter";
        }
        if (RecruitmentService.ROLE_ADMIN.equals(role)) {
            return "/admin";
        }
        return "/applicant";
    }

    private String normalizedRole(String rawRole) {
        String role = HtmlUtil.nonNull(rawRole).trim().toUpperCase();
        if (RecruitmentService.ROLE_RECRUITER.equals(role)) {
            return RecruitmentService.ROLE_RECRUITER;
        }
        if (RecruitmentService.ROLE_ADMIN.equals(role)) {
            return RecruitmentService.ROLE_ADMIN;
        }
        return RecruitmentService.ROLE_APPLICANT;
    }

    private int parseInt(String rawValue, int defaultValue) {
        if (HtmlUtil.isBlank(rawValue)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(rawValue.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        String normalized = path.endsWith("/") && path.length() > 1 ? path.substring(0, path.length() - 1) : path;
        return normalized.isEmpty() ? "/" : normalized;
    }

    private String[] pathSegments(String path) {
        return Arrays.stream(path.split("/")).filter(segment -> !segment.isBlank()).toArray(String[]::new);
    }

    private Map<String, String> mapOf(String... values) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int index = 0; index + 1 < values.length; index += 2) {
            if (!HtmlUtil.isBlank(values[index + 1])) {
                result.put(values[index], values[index + 1]);
            }
        }
        return result;
    }

    private String buildUrl(String path, Map<String, String> queryParameters) {
        if (queryParameters.isEmpty()) {
            return path;
        }

        StringBuilder builder = new StringBuilder(path).append("?");
        boolean first = true;
        for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
            if (!first) {
                builder.append("&");
            }
            builder.append(HtmlUtil.urlEncode(entry.getKey()))
                    .append("=")
                    .append(HtmlUtil.urlEncode(entry.getValue()));
            first = false;
        }
        return builder.toString();
    }

    private record UserSession(String sessionId, String accountId, String role) {
    }
}
