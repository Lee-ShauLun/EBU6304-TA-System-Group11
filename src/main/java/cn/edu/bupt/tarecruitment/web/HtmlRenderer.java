package cn.edu.bupt.tarecruitment.web;

import cn.edu.bupt.tarecruitment.model.ApplicantProfile;
import cn.edu.bupt.tarecruitment.model.ApplicationRecord;
import cn.edu.bupt.tarecruitment.model.CvDocument;
import cn.edu.bupt.tarecruitment.model.Position;
import cn.edu.bupt.tarecruitment.model.UserAccount;
import cn.edu.bupt.tarecruitment.service.DashboardStats;
import cn.edu.bupt.tarecruitment.service.MatchingResult;
import cn.edu.bupt.tarecruitment.service.RecruitmentService;
import cn.edu.bupt.tarecruitment.service.WorkloadEntry;
import cn.edu.bupt.tarecruitment.util.HtmlUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlRenderer {

    public String renderLandingPage(String notice, String error) {
        String body =
                """
                <section class='hero'>
                    <div>
                        <span class='eyebrow'>BUPT International School</span>
                        <h1>TA Recruitment System</h1>
                        <p>
                            A streamlined platform for Teaching Assistant management. 
                            Please select the portal that corresponds to your official role to begin.
                        </p>
                    </div>
                    <div class='hero-card'>
                        <h2>System Overview</h2>
                        <ul class='bullet-list'>
                            <li><strong>Applicant:</strong> For students seeking TA opportunities.</li>
                            <li><strong>Recruiter:</strong> For module leaders and admin staff managing hiring.</li>
                            <li><strong>Unified Access:</strong> Secure, role-based login for all users.</li>
                            <li><strong>Coursework Ready:</strong> Built for BUPT/QMUL academic evaluation.</li>
                        </ul>
                    </div>
                </section>
                <section class='portal-grid'>
                    <article class='portal-card applicant-theme'>
                        <div class='eyebrow'>For Students</div>
                        <h2>Applicant Portal</h2>
                        <p>Discover TA vacancies, submit your CV, and track the real-time status of your module applications.</p>
                        <div class='portal-actions'>
                            <a class='button-link secondary-link' href='/login?role=APPLICANT'>Sign in as Applicant</a>
                            <a class='text-link' href='/register?role=APPLICANT'>Create an applicant account</a>
                        </div>
                    </article>
                    <article class='portal-card recruiter-theme'>
                        <div class='eyebrow'>For Faculty & Staff</div>
                        <h2>Recruiter Portal</h2>
                        <p>Post new TA positions, review candidate skills/match scores, and manage workload distribution.</p>
                        <div class='portal-actions'>
                            <a class='button-link' href='/login?role=RECRUITER'>Sign in as Recruiter</a>
                            <a class='text-link' href='/register?role=RECRUITER'>Create a recruiter account</a>
                        </div>
                    </article>
                </section>
                """;
        return publicLayout("TA Recruitment System", body, notice, error);
    }

    public String renderLoginPage(String role, String notice, String error) {
        String title = portalTitle(role) + " Sign In";
        String body =
                """
                <section class='auth-shell'>
                    <article class='auth-card'>
                        <span class='eyebrow'>%s</span>
                        <h1>%s</h1>
                        <p>Use your account credentials to enter the correct portal.</p>
                        <form method='post' action='/login' class='stack-form'>
                            <input type='hidden' name='role' value='%s'>
                            <label>Username<input name='username' required autocomplete='username'></label>
                            <label>Password<input name='password' type='password' required autocomplete='current-password'></label>
                            <button type='submit'>Sign in</button>
                        </form>
                        <p class='hint'>
                            Need an account?
                            <a class='text-link' href='/register?role=%s'>Register for %s</a>
                        </p>
                        <p class='hint'><a class='text-link' href='/'>Back to portal selection</a></p>
                    </article>
                </section>
                """
                        .formatted(
                                HtmlUtil.escape(portalTitle(role)),
                                HtmlUtil.escape(title),
                                HtmlUtil.escape(role),
                                HtmlUtil.escape(role),
                                HtmlUtil.escape(portalTitle(role)));
        return publicLayout(title, body, notice, error);
    }

    public String renderRegisterPage(String role, String notice, String error) {
        String title = portalTitle(role) + " Registration";
        String body =
                """
                <section class='auth-shell'>
                    <article class='auth-card'>
                        <span class='eyebrow'>%s</span>
                        <h1>%s</h1>
                        <p>Create an account with a username and password. The portal role is fixed during registration.</p>
                        <form method='post' action='/register' class='stack-form'>
                            <input type='hidden' name='role' value='%s'>
                            <label>Display name<input name='displayName' required autocomplete='name'></label>
                            <label>Username<input name='username' required autocomplete='username'></label>
                            <label>Password<input name='password' type='password' required autocomplete='new-password'></label>
                            <label>Confirm password<input name='confirmPassword' type='password' required autocomplete='new-password'></label>
                            <button type='submit'>Create account</button>
                        </form>
                        <p class='hint'>
                            Already registered?
                            <a class='text-link' href='/login?role=%s'>Sign in to %s</a>
                        </p>
                        <p class='hint'><a class='text-link' href='/'>Back to portal selection</a></p>
                    </article>
                </section>
                """
                        .formatted(
                                HtmlUtil.escape(portalTitle(role)),
                                HtmlUtil.escape(title),
                                HtmlUtil.escape(role),
                                HtmlUtil.escape(role),
                                HtmlUtil.escape(portalTitle(role)));
        return publicLayout(title, body, notice, error);
    }

    public String renderApplicantDashboard(
            UserAccount account,
            ApplicantProfile profile,
            CvDocument latestCv,
            int openPositions,
            int applicationCount,
            int selectedCount,
            String notice,
            String error) {
        String profileStatus =
                profile == null || HtmlUtil.isBlank(profile.getEmail()) ? "Needs completion" : "Ready";
        String cvStatus = latestCv == null ? "Not uploaded" : "Uploaded";

        String body =
                metrics(
                                metricCard("Profile", profileStatus, "Complete your profile before applying."),
                                metricCard("CV", cvStatus, "Upload the latest version of your resume."),
                                metricCard("Open positions", String.valueOf(openPositions), "Browse available TA positions."),
                                metricCard("Applications", String.valueOf(applicationCount), "Track your pending and final results."),
                                metricCard("Selected", String.valueOf(selectedCount), "Applications that ended with an offer."))
                        + """
                        <section class='panel-grid'>
                            %s
                            %s
                        </section>
                        """
                                .formatted(
                                        panel(
                                                "Next steps",
                                                """
                                                <ul class='bullet-list'>
                                                    <li>Update your profile with contact details, skills, and weekly availability.</li>
                                                    <li>Upload your latest CV before submitting applications.</li>
                                                    <li>Browse positions and review match explanations before applying.</li>
                                                    <li>Monitor status updates in the applications page.</li>
                                                </ul>
                                                """),
                                        panel(
                                                "Current account",
                                                """
                                                <div class='info-list'>
                                                    <div><strong>Name:</strong> %s</div>
                                                    <div><strong>Username:</strong> %s</div>
                                                    <div><strong>Portal:</strong> Applicant Portal</div>
                                                    <div><strong>Latest CV:</strong> %s</div>
                                                </div>
                                                """
                                                        .formatted(
                                                                HtmlUtil.escape(account.getDisplayName()),
                                                                HtmlUtil.escape(account.getUsername()),
                                                                HtmlUtil.escape(
                                                                        latestCv == null
                                                                                ? "No file uploaded"
                                                                                : latestCv.getOriginalFileName()))));
        return portalLayout(
                "Applicant Dashboard", "applicant", "dashboard", account, body, notice, error);
    }

    public String renderApplicantProfile(
            UserAccount account,
            ApplicantProfile profile,
            CvDocument latestCv,
            String notice,
            String error) {
        ApplicantProfile current = profile == null ? new ApplicantProfile() : profile;
        String cvBlock =
                latestCv == null
                        ? "<p class='empty'>No CV has been uploaded yet.</p>"
                        : """
                          <div class='info-list'>
                              <div><strong>Latest file:</strong> %s</div>
                              <div><strong>Uploaded at:</strong> %s</div>
                              <div><strong>Size:</strong> %d bytes</div>
                              <div><a class='text-link' href='/uploads?file=%s&name=%s'>Download latest CV</a></div>
                          </div>
                          """
                                .formatted(
                                        HtmlUtil.escape(latestCv.getOriginalFileName()),
                                        HtmlUtil.escape(latestCv.getUploadedAt()),
                                        latestCv.getSize(),
                                        HtmlUtil.urlEncode(latestCv.getStoredFileName()),
                                        HtmlUtil.urlEncode(latestCv.getOriginalFileName()));

        String body =
                """
                <section class='panel-grid'>
                    %s
                    %s
                </section>
                """
                        .formatted(
                                panel(
                                        "Applicant profile",
                                        """
                                        <form method='post' action='/applicant/profile' class='form-grid'>
                                            <label>Full name<input name='fullName' required value='%s'></label>
                                            <label>Email<input name='email' type='email' required value='%s'></label>
                                            <label>Phone<input name='phone' value='%s'></label>
                                            <label>Major<input name='major' value='%s'></label>
                                            <label>Year of study<input name='yearOfStudy' value='%s'></label>
                                            <label>Weekly availability (hours)<input name='availableHoursPerWeek' type='number' min='0' value='%s'></label>
                                            <label class='full'>Skills<textarea name='skills' rows='5' placeholder='For example: Java, tutoring, data analysis'>%s</textarea></label>
                                            <div class='full actions'><button type='submit'>Save profile</button></div>
                                        </form>
                                        """
                                                .formatted(
                                                        HtmlUtil.escape(HtmlUtil.nonNull(current.getFullName())),
                                                        HtmlUtil.escape(HtmlUtil.nonNull(current.getEmail())),
                                                        HtmlUtil.escape(HtmlUtil.nonNull(current.getPhone())),
                                                        HtmlUtil.escape(HtmlUtil.nonNull(current.getMajor())),
                                                        HtmlUtil.escape(HtmlUtil.nonNull(current.getYearOfStudy())),
                                                        String.valueOf(Math.max(current.getAvailableHoursPerWeek(), 0)),
                                                        HtmlUtil.escape(HtmlUtil.nonNull(current.getSkills())))),
                                panel("Latest CV", cvBlock));
        return portalLayout("My Profile", "applicant", "profile", account, body, notice, error);
    }

    public String renderApplicantCv(
            UserAccount account,
            ApplicantProfile profile,
            CvDocument latestCv,
            String notice,
            String error) {
        String profileHint =
                profile == null || HtmlUtil.isBlank(profile.getEmail())
                        ? "<p class='hint'>Complete your profile before you submit applications.</p>"
                        : "<p class='hint'>Your profile is ready. Upload the newest CV whenever you need to replace it.</p>";

        String latestCvBlock =
                latestCv == null
                        ? "<p class='empty'>No CV uploaded yet.</p>"
                        : """
                          <div class='info-list'>
                              <div><strong>File:</strong> %s</div>
                              <div><strong>Uploaded at:</strong> %s</div>
                              <div><a class='text-link' href='/uploads?file=%s&name=%s'>Download CV</a></div>
                          </div>
                          """
                                .formatted(
                                        HtmlUtil.escape(latestCv.getOriginalFileName()),
                                        HtmlUtil.escape(latestCv.getUploadedAt()),
                                        HtmlUtil.urlEncode(latestCv.getStoredFileName()),
                                        HtmlUtil.urlEncode(latestCv.getOriginalFileName()));

        String body =
                """
                <section class='panel-grid'>
                    %s
                    %s
                </section>
                """
                        .formatted(
                                panel(
                                        "Upload CV",
                                        """
                                        %s
                                        <form method='post' action='/applicant/cv' enctype='multipart/form-data' class='stack-form'>
                                            <label>Select file<input type='file' name='cvFile' required></label>
                                            <button type='submit'>Upload CV</button>
                                        </form>
                                        """
                                                .formatted(profileHint)),
                                panel("Current file", latestCvBlock));
        return portalLayout("Upload CV", "applicant", "cv", account, body, notice, error);
    }

    public String renderApplicantPositions(
            UserAccount account,
            ApplicantProfile profile,
            List<Position> positions,
            List<ApplicationRecord> existingApplications,
            Map<String, MatchingResult> matchingResults,
            String keyword,
            String notice,
            String error) {
        Map<String, ApplicationRecord> applicationIndex = new HashMap<>();
        for (ApplicationRecord application : existingApplications) {
            applicationIndex.put(application.getPositionId(), application);
        }

        StringBuilder body = new StringBuilder();
        body.append("<section class='panel-grid'>");
        body.append(
                panel(
                        "Search positions",
                        """
                        <form method='get' action='/applicant/positions' class='form-grid'>
                            <label class='full'>Keyword<input name='keyword' value='%s' placeholder='Module, skill, recruiter'></label>
                            <div class='full actions'><button type='submit'>Search</button></div>
                        </form>
                        <p class='hint'>The match score combines required skills, preferred skills, and weekly availability.</p>
                        """
                                .formatted(HtmlUtil.escape(HtmlUtil.nonNull(keyword)))));
        body.append(
                panel(
                        "Application rules",
                        """
                        <ul class='bullet-list'>
                            <li>You must complete your profile and upload a CV before applying.</li>
                            <li>Each account can submit only one application per position.</li>
                            <li>Match explanations help you understand missing skills before you apply.</li>
                        </ul>
                        """));
        body.append("</section>");

        if (positions.isEmpty()) {
            body.append("<section class='single-panel'>");
            body.append(panel("Open positions", "<p class='empty'>No open positions matched your search.</p>"));
            body.append("</section>");
        } else {
            body.append("<section class='position-list'>");
            for (Position position : positions) {
                body.append(
                        renderPositionCard(
                                position,
                                profile,
                                applicationIndex.get(position.getId()),
                                matchingResults.get(position.getId())));
            }
            body.append("</section>");
        }

        return portalLayout(
                "Browse Positions", "applicant", "positions", account, body.toString(), notice, error);
    }

    public String renderApplicantApplications(
            UserAccount account,
            List<ApplicationRecord> applications,
            Map<String, Position> positionIndex,
            String notice,
            String error) {
        String body;
        if (applications.isEmpty()) {
            body =
                    "<section class='single-panel'>"
                            + panel("My applications", "<p class='empty'>You have not submitted any applications yet.</p>")
                            + "</section>";
        } else {
            StringBuilder table = new StringBuilder();
            table.append("<table><thead><tr>");
            table.append("<th>Position</th><th>Status</th><th>Match score</th><th>Missing skills</th><th>Applied at</th><th>Decision note</th>");
            table.append("</tr></thead><tbody>");
            for (ApplicationRecord application : applications) {
                Position position = positionIndex.get(application.getPositionId());
                String positionLabel =
                        position == null
                                ? "Archived position"
                                : HtmlUtil.escape(position.getModuleCode() + " | " + position.getModuleName());
                table.append("<tr>");
                table.append("<td>").append(positionLabel).append("</td>");
                table.append("<td>").append(statusBadge(application.getStatus())).append("</td>");
                table.append("<td>").append(application.getMatchScore()).append("</td>");
                table.append("<td>")
                        .append(
                                HtmlUtil.isBlank(application.getMissingSkills())
                                        ? "None"
                                        : HtmlUtil.escape(application.getMissingSkills()))
                        .append("</td>");
                table.append("<td>").append(HtmlUtil.escape(application.getAppliedAt())).append("</td>");
                table.append("<td>")
                        .append(
                                HtmlUtil.isBlank(application.getDecisionNote())
                                        ? "-"
                                        : HtmlUtil.escape(application.getDecisionNote()))
                        .append("</td>");
                table.append("</tr>");
            }
            table.append("</tbody></table>");
            body = "<section class='single-panel'>" + panel("My applications", table.toString()) + "</section>";
        }

        return portalLayout(
                "My Applications", "applicant", "applications", account, body, notice, error);
    }

    public String renderRecruiterDashboard(
            UserAccount account,
            DashboardStats stats,
            List<Position> recentPositions,
            String notice,
            String error) {
        StringBuilder positionsList = new StringBuilder();
        if (recentPositions.isEmpty()) {
            positionsList.append("<p class='empty'>No positions have been posted yet.</p>");
        } else {
            positionsList.append("<div class='mini-list'>");
            for (int index = 0; index < Math.min(recentPositions.size(), 5); index++) {
                Position position = recentPositions.get(index);
                positionsList.append("<a class='mini-link' href='/recruiter/positions/")
                        .append(HtmlUtil.escape(position.getId()))
                        .append("'>")
                        .append(HtmlUtil.escape(position.getModuleCode()))
                        .append(" | ")
                        .append(HtmlUtil.escape(position.getModuleName()))
                        .append("</a>");
            }
            positionsList.append("</div>");
        }

        String body =
                metrics(
                                metricCard("Applicants", String.valueOf(stats.getApplicantCount()), "Profiles currently stored in the system."),
                                metricCard("Open positions", String.valueOf(stats.getOpenPositionCount()), "Recruitment opportunities still accepting applications."),
                                metricCard("Applications", String.valueOf(stats.getApplicationCount()), "All submitted applications across positions."),
                                metricCard("Selected", String.valueOf(stats.getSelectedCount()), "Applications already converted into offers."))
                        + """
                        <section class='panel-grid'>
                            %s
                            %s
                        </section>
                        """
                                .formatted(
                                        panel(
                                                "Recruiter workspace",
                                                """
                                                <ul class='bullet-list'>
                                                    <li>Post a position and define required skills, preferred skills, quota, and weekly workload.</li>
                                                    <li>Open each position to review applicants, CVs, match scores, and decision notes.</li>
                                                    <li>Use the workload board before making final offers.</li>
                                                </ul>
                                                """),
                                        panel("Recent positions", positionsList.toString()));

        return portalLayout(
                "Recruiter Dashboard", "recruiter", "dashboard", account, body, notice, error);
    }

    public String renderRecruiterPositions(
            UserAccount account,
            List<Position> positions,
            Map<String, Integer> selectedCounts,
            String notice,
            String error) {
        StringBuilder table = new StringBuilder();
        table.append("<table><thead><tr>");
        table.append("<th>Module</th><th>Recruiter</th><th>Hours</th><th>Quota</th><th>Status</th><th>Action</th>");
        table.append("</tr></thead><tbody>");
        for (Position position : positions) {
            int selectedCount = selectedCounts.getOrDefault(position.getId(), 0);
            table.append("<tr>");
            table.append("<td>")
                    .append(HtmlUtil.escape(position.getModuleCode() + " | " + position.getModuleName()))
                    .append("</td>");
            table.append("<td>").append(HtmlUtil.escape(position.getOrganiserName())).append("</td>");
            table.append("<td>").append(position.getWeeklyHours()).append(" h/week</td>");
            table.append("<td>").append(selectedCount).append("/").append(position.getQuota()).append("</td>");
            table.append("<td>").append(statusBadge(position.getStatus())).append("</td>");
            table.append("<td><a class='text-link' href='/recruiter/positions/")
                    .append(HtmlUtil.escape(position.getId()))
                    .append("'>Review applications</a></td>");
            table.append("</tr>");
        }
        table.append("</tbody></table>");

        String body =
                """
                <section class='panel-grid'>
                    %s
                    %s
                </section>
                """
                        .formatted(
                                panel(
                                        "Create a new position",
                                        """
                                        <form method='post' action='/recruiter/positions' class='form-grid'>
                                            <label>Module code<input name='moduleCode' required></label>
                                            <label>Position title<input name='moduleName' required></label>
                                            <label>Recruiter name<input name='organiserName' required value='%s'></label>
                                            <label>Recruiter email<input name='organiserEmail' type='email' required></label>
                                            <label>Weekly hours<input name='weeklyHours' type='number' min='1' value='6' required></label>
                                            <label>Quota<input name='quota' type='number' min='1' value='1' required></label>
                                            <label class='full'>Description<textarea name='description' rows='4'></textarea></label>
                                            <label class='full'>Required skills<textarea name='requiredSkills' rows='3'></textarea></label>
                                            <label class='full'>Preferred skills<textarea name='preferredSkills' rows='3'></textarea></label>
                                            <div class='full actions'><button type='submit'>Publish position</button></div>
                                        </form>
                                        """
                                                .formatted(HtmlUtil.escape(account.getDisplayName()))),
                                panel("Position list", table.toString()));
        return portalLayout(
                "Recruiter Positions", "recruiter", "positions", account, body, notice, error);
    }

    public String renderRecruiterPositionDetail(
            UserAccount account,
            Position position,
            List<ApplicationRecord> applications,
            Map<String, ApplicantProfile> applicantIndex,
            Map<String, CvDocument> latestCvIndex,
            int selectedCount,
            String notice,
            String error) {
        StringBuilder body = new StringBuilder();
        body.append("<section class='panel-grid'>");
        body.append(
                panel(
                        "Position summary",
                        """
                        <div class='info-list'>
                            <div><strong>Module:</strong> %s | %s</div>
                            <div><strong>Recruiter:</strong> %s (%s)</div>
                            <div><strong>Hours:</strong> %d h/week</div>
                            <div><strong>Filled quota:</strong> %d/%d</div>
                            <div><strong>Status:</strong> %s</div>
                            <div><strong>Required skills:</strong> %s</div>
                            <div><strong>Preferred skills:</strong> %s</div>
                            <div><strong>Description:</strong> %s</div>
                        </div>
                        """
                                .formatted(
                                        HtmlUtil.escape(position.getModuleCode()),
                                        HtmlUtil.escape(position.getModuleName()),
                                        HtmlUtil.escape(position.getOrganiserName()),
                                        HtmlUtil.escape(position.getOrganiserEmail()),
                                        position.getWeeklyHours(),
                                        selectedCount,
                                        position.getQuota(),
                                        statusBadge(position.getStatus()),
                                        HtmlUtil.escape(position.getRequiredSkills()),
                                        HtmlUtil.escape(position.getPreferredSkills()),
                                        HtmlUtil.nl2br(position.getDescription()))));
        body.append(
                panel(
                        "Navigation",
                        "<a class='text-link' href='/recruiter/positions'>Back to recruiter positions</a>"));
        body.append("</section>");

        if (applications.isEmpty()) {
            body.append("<section class='single-panel'>");
            body.append(panel("Applications", "<p class='empty'>No one has applied for this position yet.</p>"));
            body.append("</section>");
        } else {
            body.append("<section class='position-list'>");
            for (ApplicationRecord application : applications) {
                ApplicantProfile applicant = applicantIndex.get(application.getApplicantId());
                CvDocument cvDocument = latestCvIndex.get(application.getApplicantId());
                body.append(renderApplicationCard(position, application, applicant, cvDocument));
            }
            body.append("</section>");
        }

        return portalLayout(
                "Review Applications", "recruiter", "positions", account, body.toString(), notice, error);
    }

    public String renderRecruiterWorkload(
            UserAccount account,
            List<WorkloadEntry> workloadEntries,
            String notice,
            String error) {
        int overloadedCount = 0;
        int atRiskCount = 0;
        for (WorkloadEntry entry : workloadEntries) {
            if ("OVERLOADED".equals(entry.getStatus())) {
                overloadedCount++;
            } else if ("AT_RISK".equals(entry.getStatus())) {
                atRiskCount++;
            }
        }

        StringBuilder table = new StringBuilder();
        table.append("<table><thead><tr>");
        table.append("<th>Applicant</th><th>Assigned hours</th><th>Selected positions</th><th>Modules</th><th>Status</th><th>Recommendation</th>");
        table.append("</tr></thead><tbody>");
        for (WorkloadEntry entry : workloadEntries) {
            table.append("<tr>");
            table.append("<td>").append(HtmlUtil.escape(entry.getApplicantName())).append("</td>");
            table.append("<td>").append(entry.getAssignedHours()).append("/").append(entry.getMaxHours()).append(" h</td>");
            table.append("<td>").append(entry.getSelectedPositionCount()).append("</td>");
            table.append("<td>")
                    .append(HtmlUtil.isBlank(entry.getModules()) ? "-" : HtmlUtil.escape(entry.getModules()))
                    .append("</td>");
            table.append("<td>").append(statusBadge(entry.getStatus())).append("</td>");
            table.append("<td>").append(HtmlUtil.escape(entry.getRecommendation())).append("</td>");
            table.append("</tr>");
        }
        table.append("</tbody></table>");

        String body =
                metrics(
                                metricCard("Applicants tracked", String.valueOf(workloadEntries.size()), "Applicants included in workload analysis."),
                                metricCard("Overloaded", String.valueOf(overloadedCount), "Applicants already above their stated weekly availability."),
                                metricCard("At risk", String.valueOf(atRiskCount), "Applicants close to their weekly limit."))
                        + "<section class='single-panel'>" + panel("Workload board", table.toString()) + "</section>";

        return portalLayout(
                "Workload Board", "recruiter", "workload", account, body, notice, error);
    }

    public String renderErrorPage(String title, String message) {
    String template = "<section class='auth-shell'>"
            + "<article class='auth-card'>"
            + "<h1>%s</h1>"
            + "<p>%s</p>"
            + "<p class='hint'><a class='text-link' href='/'>Return to portal selection</a></p>"
            + "</article></section>";

    String body = String.format(template, HtmlUtil.escape(title), HtmlUtil.escape(message));
    
    return publicLayout(title, body, null, null);
}

    private String renderPositionCard(
            Position position,
            ApplicantProfile profile,
            ApplicationRecord existingApplication,
            MatchingResult matchingResult) {
        StringBuilder card = new StringBuilder();
        card.append("<article class='position-card'>");
        card.append("<div class='card-head'><div><h3>")
                .append(HtmlUtil.escape(position.getModuleCode()))
                .append(" | ")
                .append(HtmlUtil.escape(position.getModuleName()))
                .append("</h3><p class='meta'>Recruiter: ")
                .append(HtmlUtil.escape(position.getOrganiserName()))
                .append(" | Hours: ")
                .append(position.getWeeklyHours())
                .append(" h/week | Quota: ")
                .append(position.getQuota())
                .append("</p></div>")
                .append(statusBadge(position.getStatus()))
                .append("</div>");
        card.append("<p>").append(HtmlUtil.nl2br(position.getDescription())).append("</p>");
        card.append("<div class='tags'><span>Required: ")
                .append(HtmlUtil.escape(position.getRequiredSkills()))
                .append("</span><span>Preferred: ")
                .append(HtmlUtil.escape(position.getPreferredSkills()))
                .append("</span></div>");

        if (matchingResult != null) {
            card.append("<div class='match-box'>");
            card.append("<div class='match-score'>Match score ").append(matchingResult.getScore()).append("</div>");
            card.append("<div class='match-note'>")
                    .append(HtmlUtil.escape(matchingResult.getExplanation()))
                    .append("</div>");
            card.append("<div><strong>Matched skills:</strong> ")
                    .append(
                            HtmlUtil.isBlank(matchingResult.getMatchedSkills())
                                    ? "None"
                                    : HtmlUtil.escape(matchingResult.getMatchedSkills()))
                    .append("</div>");
            card.append("<div><strong>Missing skills:</strong> ")
                    .append(
                            HtmlUtil.isBlank(matchingResult.getMissingSkills())
                                    ? "None"
                                    : HtmlUtil.escape(matchingResult.getMissingSkills()))
                    .append("</div>");
            card.append("</div>");
        }

        if (existingApplication == null) {
            if (profile == null || HtmlUtil.isBlank(profile.getEmail())) {
                card.append("<p class='hint'>Complete your profile before applying to this position.</p>");
            } else {
                card.append(
                        """
                        <form method='post' action='/applicant/applications' class='stack-form'>
                            <input type='hidden' name='positionId' value='%s'>
                            <label>Application note<textarea name='statement' rows='3' placeholder='Optional: explain why you are a strong fit'></textarea></label>
                            <button type='submit'>Submit application</button>
                        </form>
                        """
                                .formatted(HtmlUtil.escape(position.getId())));
            }
        } else {
            card.append("<div class='applied-box'>Application status: ")
                    .append(statusBadge(existingApplication.getStatus()))
                    .append("</div>");
            if (!HtmlUtil.isBlank(existingApplication.getDecisionNote())) {
                card.append("<p class='hint'>Note: ")
                        .append(HtmlUtil.escape(existingApplication.getDecisionNote()))
                        .append("</p>");
            }
        }

        card.append("</article>");
        return card.toString();
    }

    private String renderApplicationCard(
            Position position,
            ApplicationRecord application,
            ApplicantProfile applicant,
            CvDocument cvDocument) {
        String applicantName = applicant == null ? "Unknown applicant" : applicant.getFullName();
        String applicantMeta =
                applicant == null
                        ? "Applicant profile is missing."
                        : HtmlUtil.escape(
                                safe(applicant.getMajor())
                                        + " | "
                                        + safe(applicant.getYearOfStudy())
                                        + " | Availability: "
                                        + applicant.getAvailableHoursPerWeek()
                                        + " h/week");
        String cvLink =
                cvDocument == null
                        ? "<span class='hint'>No CV uploaded</span>"
                        : "<a class='text-link' href='/uploads?file="
                                + HtmlUtil.urlEncode(cvDocument.getStoredFileName())
                                + "&name="
                                + HtmlUtil.urlEncode(cvDocument.getOriginalFileName())
                                + "'>Download CV</a>";

        StringBuilder card = new StringBuilder();
        card.append("<article class='position-card'>");
        card.append("<div class='card-head'><div><h3>")
                .append(HtmlUtil.escape(applicantName))
                .append("</h3><p class='meta'>")
                .append(applicantMeta)
                .append("</p></div>")
                .append(statusBadge(application.getStatus()))
                .append("</div>");
        card.append("<div class='info-list'>");
        card.append("<div><strong>Contact:</strong> ")
                .append(applicant == null ? "-" : HtmlUtil.escape(applicant.getEmail()))
                .append("</div>");
        card.append("<div><strong>Match score:</strong> ").append(application.getMatchScore()).append("</div>");
        card.append("<div><strong>Missing skills:</strong> ")
                .append(
                        HtmlUtil.isBlank(application.getMissingSkills())
                                ? "None"
                                : HtmlUtil.escape(application.getMissingSkills()))
                .append("</div>");
        card.append("<div><strong>Applied at:</strong> ")
                .append(HtmlUtil.escape(application.getAppliedAt()))
                .append("</div>");
        card.append("<div><strong>CV:</strong> ").append(cvLink).append("</div>");
        card.append("<div><strong>Skills:</strong> ")
                .append(applicant == null ? "-" : HtmlUtil.escape(applicant.getSkills()))
                .append("</div>");
        card.append("<div><strong>Application note:</strong> ")
                .append(
                        HtmlUtil.isBlank(application.getStatement())
                                ? "No note provided"
                                : HtmlUtil.escape(application.getStatement()))
                .append("</div>");
        if (!HtmlUtil.isBlank(application.getDecisionNote())) {
            card.append("<div><strong>Decision note:</strong> ")
                    .append(HtmlUtil.escape(application.getDecisionNote()))
                    .append("</div>");
        }
        card.append("</div>");

        if ("PENDING".equals(application.getStatus()) && !"FILLED".equals(position.getStatus())) {
            card.append(
                    """
                    <div class='decision-grid'>
                        <form method='post' action='/recruiter/positions/%s/select' class='stack-form'>
                            <input type='hidden' name='applicationId' value='%s'>
                            <label>Offer note<input name='note' placeholder='Optional recruiter note'></label>
                            <button type='submit'>Select applicant</button>
                        </form>
                        <form method='post' action='/recruiter/positions/%s/reject' class='stack-form'>
                            <input type='hidden' name='applicationId' value='%s'>
                            <label>Rejection note<input name='note' placeholder='Optional recruiter note'></label>
                            <button class='secondary' type='submit'>Reject application</button>
                        </form>
                    </div>
                    """
                            .formatted(
                                    HtmlUtil.escape(position.getId()),
                                    HtmlUtil.escape(application.getId()),
                                    HtmlUtil.escape(position.getId()),
                                    HtmlUtil.escape(application.getId())));
        }

        card.append("</article>");
        return card.toString();
    }

    private String publicLayout(String title, String bodyHtml, String notice, String error) {
        String nav =
                """
                <a class='nav-link %s' href='/'>Home</a>
                <a class='nav-link' href='/login?role=APPLICANT'>Applicant sign in</a>
                <a class='nav-link' href='/login?role=RECRUITER'>Recruiter sign in</a>
                """
                        .formatted("current");
        return layout(title, bodyHtml, nav, notice, error, null, null);
    }

    private String portalLayout(
            String title,
            String portal,
            String activeNav,
            UserAccount account,
            String bodyHtml,
            String notice,
            String error) {
        String nav = "applicant".equals(portal) ? applicantNav(activeNav) : recruiterNav(activeNav);
        return layout(title, bodyHtml, nav, notice, error, account, portal);
    }

    private String layout(
            String title,
            String bodyHtml,
            String navHtml,
            String notice,
            String error,
            UserAccount account,
            String portal) {
        String shellClass = account == null ? "public-shell" : "portal-shell";
        String asideClass =
                account == null
                        ? "sidebar public-sidebar"
                        : "sidebar " + ("applicant".equals(portal) ? "applicant-sidebar" : "recruiter-sidebar");
        String subtitle =
                account == null
                        ? "Choose the correct role before entering the system."
                        : HtmlUtil.escape(account.getDisplayName() + " | " + portalTitle(account.getRole()));

        String accountBlock =
                account == null
                        ? ""
                        : """
                          <div class='account-box'>
                              <div class='account-name'>%s</div>
                              <div class='account-meta'>@%s</div>
                              <form method='post' action='/logout'>
                                  <button class='ghost-button' type='submit'>Sign out</button>
                              </form>
                          </div>
                          """
                                .formatted(
                                        HtmlUtil.escape(account.getDisplayName()),
                                        HtmlUtil.escape(account.getUsername()));

        return """
               <!DOCTYPE html>
               <html lang='en'>
               <head>
                   <meta charset='UTF-8'>
                   <meta name='viewport' content='width=device-width, initial-scale=1.0'>
                   <title>%s</title>
                   <style>%s</style>
               </head>
               <body>
                   <div class='%s'>
                       <aside class='%s'>
                           <div class='brand'>
                               <span>TA Recruitment</span>
                               <small>BUPT International School</small>
                           </div>
                           <nav class='nav-stack'>%s</nav>
                           %s
                       </aside>
                       <main class='content'>
                           <header class='page-header'>
                               <h1>%s</h1>
                               <p>%s</p>
                           </header>
                           %s
                           %s
                       </main>
                   </div>
               </body>
               </html>
               """
                .formatted(
                        HtmlUtil.escape(title),
                        styles(),
                        shellClass,
                        asideClass,
                        navHtml,
                        accountBlock,
                        HtmlUtil.escape(title),
                        subtitle,
                        renderFlash(notice, error),
                        bodyHtml);
    }

    private String metrics(String... cards) {
        StringBuilder builder = new StringBuilder("<section class='metrics'>");
        for (String card : cards) {
            builder.append(card);
        }
        builder.append("</section>");
        return builder.toString();
    }

    private String metricCard(String label, String value, String hint) {
        return """
               <article class='metric-card'>
                   <div class='metric-label'>%s</div>
                   <div class='metric-value'>%s</div>
                   <div class='metric-hint'>%s</div>
               </article>
               """
                .formatted(HtmlUtil.escape(label), HtmlUtil.escape(value), HtmlUtil.escape(hint));
    }

    private String panel(String title, String innerHtml) {
        return "<section class='panel'><h2>" + HtmlUtil.escape(title) + "</h2>" + innerHtml + "</section>";
    }

    private String renderFlash(String notice, String error) {
        if (!HtmlUtil.isBlank(error)) {
            return "<div class='flash error'>" + HtmlUtil.escape(error) + "</div>";
        }
        if (!HtmlUtil.isBlank(notice)) {
            return "<div class='flash notice'>" + HtmlUtil.escape(notice) + "</div>";
        }
        return "";
    }

    private String applicantNav(String activeNav) {
        return navLink("/applicant", "Dashboard", "dashboard".equals(activeNav))
                + navLink("/applicant/profile", "My Profile", "profile".equals(activeNav))
                + navLink("/applicant/cv", "Upload CV", "cv".equals(activeNav))
                + navLink("/applicant/positions", "Browse Positions", "positions".equals(activeNav))
                + navLink("/applicant/applications", "My Applications", "applications".equals(activeNav));
    }

    private String recruiterNav(String activeNav) {
        return navLink("/recruiter", "Dashboard", "dashboard".equals(activeNav))
                + navLink("/recruiter/positions", "Positions", "positions".equals(activeNav))
                + navLink("/recruiter/workload", "Workload Board", "workload".equals(activeNav));
    }

    private String navLink(String href, String label, boolean active) {
        return "<a class='nav-link" + (active ? " active" : "") + "' href='" + href + "'>" + HtmlUtil.escape(label) + "</a>";
    }

    private String portalTitle(String role) {
        if (RecruitmentService.ROLE_RECRUITER.equals(role)) {
            return "Recruiter Portal";
        }
        return "Applicant Portal";
    }

    private String statusBadge(String status) {
        String normalized = HtmlUtil.nonNull(status);
        String label =
                switch (normalized) {
                    case "OPEN" -> "Open";
                    case "FILLED" -> "Filled";
                    case "PENDING" -> "Pending";
                    case "SELECTED" -> "Selected";
                    case "REJECTED" -> "Rejected";
                    case "OVERLOADED" -> "Overloaded";
                    case "AT_RISK" -> "At risk";
                    case "BALANCED" -> "Balanced";
                    default -> normalized;
                };
        String type =
                switch (normalized) {
                    case "SELECTED", "OPEN", "BALANCED" -> "success";
                    case "FILLED", "AT_RISK" -> "warning";
                    case "REJECTED", "OVERLOADED" -> "danger";
                    default -> "neutral";
                };
        return "<span class='badge " + type + "'>" + HtmlUtil.escape(label) + "</span>";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String styles() {
        return """
               :root {
                   --paper: #fffdfa;
                   --ink: #193045;
                   --muted: #617381;
                   --line: #d7cab8;
                   --applicant: #216869;
                   --applicant-soft: #d4ece7;
                   --recruiter: #b85c38;
                   --recruiter-soft: #f5d8c6;
                   --success: #2f855a;
                   --warning: #b7791f;
                   --danger: #c53030;
                   --shadow: 0 18px 40px rgba(24, 42, 58, 0.08);
               }

               * { box-sizing: border-box; }

               body {
                   margin: 0;
                   font-family: "Avenir Next", "PingFang SC", sans-serif;
                   color: var(--ink);
                   background:
                       radial-gradient(circle at top right, rgba(184, 92, 56, 0.14), transparent 20rem),
                       linear-gradient(180deg, #fbf6ee 0%, #f0e6db 100%);
               }

               a { color: inherit; text-decoration: none; }

               .public-shell, .portal-shell {
                   min-height: 100vh;
                   display: grid;
                   grid-template-columns: 280px 1fr;
               }

               .sidebar {
                   padding: 28px 22px;
                   color: #fff;
                   display: flex;
                   flex-direction: column;
                   gap: 14px;
                   position: sticky;
                   top: 0;
                   height: 100vh;
               }

               .public-sidebar { background: rgba(19, 48, 73, 0.96); }
               .applicant-sidebar { background: rgba(33, 104, 105, 0.97); }
               .recruiter-sidebar { background: rgba(184, 92, 56, 0.97); }

               .brand {
                   display: flex;
                   flex-direction: column;
                   gap: 6px;
                   margin-bottom: 8px;
               }

               .brand span { font-size: 1.4rem; font-weight: 700; }
               .brand small { color: rgba(255, 255, 255, 0.72); }

               .nav-stack {
                   display: flex;
                   flex-direction: column;
                   gap: 8px;
               }

               .nav-link {
                   padding: 12px 14px;
                   border-radius: 14px;
                   color: rgba(255, 255, 255, 0.88);
                   transition: transform 0.2s ease, background 0.2s ease;
               }

               .nav-link:hover, .nav-link.active {
                   background: rgba(255, 255, 255, 0.14);
                   color: #fff;
                   transform: translateX(4px);
               }

               .account-box {
                   margin-top: auto;
                   padding: 16px;
                   border-radius: 18px;
                   background: rgba(255, 255, 255, 0.12);
               }

               .account-name { font-weight: 700; }
               .account-meta { color: rgba(255, 255, 255, 0.72); margin-top: 4px; margin-bottom: 12px; }

               .ghost-button {
                   width: 100%;
                   background: rgba(255, 255, 255, 0.14);
                   color: #fff;
                   border: 1px solid rgba(255, 255, 255, 0.24);
                   border-radius: 12px;
                   padding: 10px 12px;
                   cursor: pointer;
               }

               .content {
                   padding: 28px;
                   display: flex;
                   flex-direction: column;
                   gap: 22px;
               }

               .page-header h1 {
                   margin: 0;
                   font-size: 2.1rem;
               }

               .page-header p {
                   margin: 8px 0 0;
                   color: var(--muted);
               }

               .hero {
                   display: grid;
                   grid-template-columns: 1.3fr 1fr;
                   gap: 20px;
                   align-items: stretch;
               }

               .hero h1 {
                   margin: 10px 0;
                   font-size: 3rem;
                   line-height: 1.06;
               }

               .hero p {
                   color: var(--muted);
                   font-size: 1.05rem;
                   line-height: 1.7;
               }

               .hero-card, .portal-card, .auth-card, .panel, .metric-card, .position-card {
                   background: rgba(255, 253, 250, 0.93);
                   border: 1px solid rgba(215, 202, 184, 0.85);
                   border-radius: 22px;
                   box-shadow: var(--shadow);
               }

               .hero-card, .portal-card, .auth-card, .panel, .position-card { padding: 22px; }
               .metric-card { padding: 20px; }

               .portal-grid {
                   display: grid;
                   grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
                   gap: 18px;
               }

               .portal-card h2, .auth-card h1, .panel h2, .position-card h3 { margin-top: 0; }
               .applicant-theme { border-color: rgba(33, 104, 105, 0.2); }
               .recruiter-theme { border-color: rgba(184, 92, 56, 0.2); }

               .portal-actions {
                   display: flex;
                   flex-direction: column;
                   gap: 10px;
                   margin-top: 18px;
               }

               .button-link, button {
                   display: inline-flex;
                   align-items: center;
                   justify-content: center;
                   border: none;
                   border-radius: 14px;
                   padding: 12px 18px;
                   font: inherit;
                   font-weight: 700;
                   cursor: pointer;
                   background: var(--recruiter);
                   color: #fff;
                   box-shadow: 0 10px 24px rgba(184, 92, 56, 0.22);
               }

               .secondary-link { background: var(--applicant); box-shadow: 0 10px 24px rgba(33, 104, 105, 0.22); }
               button.secondary { background: #768390; box-shadow: none; }

               .auth-shell {
                   min-height: calc(100vh - 120px);
                   display: grid;
                   place-items: center;
               }

               .auth-card {
                   width: min(520px, 100%);
               }

               .eyebrow {
                   display: inline-block;
                   color: var(--recruiter);
                   font-weight: 700;
                   letter-spacing: 0.04em;
                   text-transform: uppercase;
                   font-size: 0.82rem;
               }

               .metrics {
                   display: grid;
                   grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
                   gap: 18px;
               }

               .metric-label { color: var(--muted); font-size: 0.94rem; }
               .metric-value { margin: 10px 0 8px; font-size: 2rem; font-weight: 700; }
               .metric-hint { color: var(--muted); line-height: 1.5; }

               .panel-grid {
                   display: grid;
                   grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
                   gap: 18px;
               }

               .single-panel { display: grid; }
               .panel h2 { margin-bottom: 16px; font-size: 1.2rem; }

               .form-grid {
                   display: grid;
                   grid-template-columns: repeat(2, minmax(0, 1fr));
                   gap: 14px;
               }

               .stack-form {
                   display: flex;
                   flex-direction: column;
                   gap: 12px;
               }

               .decision-grid {
                   display: grid;
                   grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
                   gap: 12px;
                   margin-top: 14px;
               }

               label {
                   display: flex;
                   flex-direction: column;
                   gap: 8px;
                   color: var(--muted);
               }

               input, textarea, select {
                   width: 100%;
                   padding: 12px 14px;
                   border-radius: 14px;
                   border: 1px solid var(--line);
                   background: #fffdfa;
                   color: var(--ink);
                   font: inherit;
               }

               textarea { resize: vertical; min-height: 92px; }
               .full { grid-column: 1 / -1; }
               .actions { display: flex; justify-content: flex-start; }

               .flash {
                   padding: 14px 18px;
                   border-radius: 16px;
                   font-weight: 600;
               }

               .flash.notice { background: rgba(47, 133, 90, 0.12); color: var(--success); }
               .flash.error { background: rgba(197, 48, 48, 0.12); color: var(--danger); }

               .bullet-list { margin: 0; padding-left: 18px; line-height: 1.8; color: var(--muted); }
               .hint, .empty, .info-list, .meta { color: var(--muted); line-height: 1.6; }
               .text-link { color: var(--recruiter); font-weight: 700; }

               .position-list {
                   display: grid;
                   grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
                   gap: 18px;
               }

               .position-card {
                   display: flex;
                   flex-direction: column;
                   gap: 14px;
               }

               .card-head {
                   display: flex;
                   justify-content: space-between;
                   gap: 14px;
                   align-items: flex-start;
               }

               .tags {
                   display: flex;
                   flex-wrap: wrap;
                   gap: 10px;
               }

               .tags span, .match-box, .applied-box {
                   background: rgba(245, 216, 198, 0.42);
                   border-radius: 16px;
                   padding: 12px;
               }

               .match-score {
                   font-size: 1.24rem;
                   font-weight: 700;
                   margin-bottom: 8px;
               }

               .badge {
                   display: inline-flex;
                   align-items: center;
                   justify-content: center;
                   padding: 6px 12px;
                   border-radius: 999px;
                   font-size: 0.88rem;
                   font-weight: 700;
               }

               .badge.success { background: rgba(47, 133, 90, 0.14); color: var(--success); }
               .badge.warning { background: rgba(183, 121, 31, 0.14); color: var(--warning); }
               .badge.danger { background: rgba(197, 48, 48, 0.14); color: var(--danger); }
               .badge.neutral { background: rgba(97, 115, 129, 0.14); color: var(--muted); }

               .mini-list {
                   display: flex;
                   flex-direction: column;
                   gap: 10px;
               }

               .mini-link {
                   padding: 12px;
                   border-radius: 14px;
                   background: rgba(245, 216, 198, 0.38);
               }

               table {
                   width: 100%;
                   border-collapse: collapse;
               }

               th, td {
                   padding: 12px 10px;
                   border-bottom: 1px solid rgba(215, 202, 184, 0.8);
                   text-align: left;
                   vertical-align: top;
               }

               th { color: var(--muted); font-weight: 700; }

               @media (max-width: 960px) {
                   .public-shell, .portal-shell { grid-template-columns: 1fr; }
                   .sidebar { position: relative; height: auto; }
                   .hero { grid-template-columns: 1fr; }
                   .form-grid { grid-template-columns: 1fr; }
               }
               """;
    }
}
