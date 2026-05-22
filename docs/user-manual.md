# User Manual

## System Overview

The TA Recruitment System supports three user roles:

- Applicant: creates a profile, uploads a CV, browses positions, applies for jobs, and checks application status.
- Recruiter: publishes positions, reviews applicants, downloads CVs, selects/rejects applicants, and checks workload.
- Admin: monitors overall workload and audits application progress across the system.

## Starting the System

Windows:

```bat
scripts\run.bat
```

macOS/Linux:

```bash
./scripts/run.sh
```

Open:

```text
http://localhost:8080
```

## Demo Accounts

| Role | Username | Password |
| --- | --- | --- |
| Applicant | `demo_applicant` | `password123` |
| Recruiter | `demo_recruiter` | `password123` |
| Admin | `demo_admin` | `password123` |

## Applicant Workflow

1. Select Applicant Portal from the landing page.
2. Sign in or create an applicant account.
3. Open My Profile and enter name, email, major, year of study, skills, and weekly availability.
4. Open Upload CV and upload a PDF, DOC, DOCX, or TXT file no larger than 5 MB.
5. Open Browse Positions.
6. Review each position's match score, matched skills, missing skills, and explanation.
7. Submit an application note.
8. Open My Applications to check pending, selected, or rejected status.

## Recruiter Workflow

1. Select Recruiter Portal from the landing page.
2. Sign in or create a recruiter account.
3. Open Positions and publish a new TA position.
4. Open a position detail page to review applications.
5. Check applicant profile information, CV link, match score, missing skills, and application note.
6. Select or reject applications with an optional decision note.
7. Open Workload Board before final allocation decisions.

## Admin Workflow

1. Select Admin Portal from the landing page.
2. Sign in or create an admin account.
3. Open Admin Dashboard to view system-level metrics.
4. Open Workload Board to identify overloaded or at-risk TAs.
5. Open Applications to audit all application statuses across positions.

## Error Handling Examples

- Duplicate username: the registration page shows a validation message.
- Invalid email: profile or position save is rejected.
- Missing CV: applicant cannot apply until a CV is uploaded.
- Invalid CV type: executable and unsupported file types are rejected.
- Wrong role access: users are redirected to their own portal.

## Screenshots Checklist

Place final screenshots in `docs/screenshots/` before submission:

- `landing-page.png`
- `applicant-dashboard.png`
- `applicant-profile.png`
- `cv-upload.png`
- `browse-positions.png`
- `my-applications.png`
- `recruiter-dashboard.png`
- `recruiter-positions.png`
- `review-applications.png`
- `recruiter-workload.png`
- `admin-dashboard.png`
- `admin-workload.png`
- `admin-applications.png`

## Data Storage

The system stores data in:

```text
data/trs-data.xml
data/uploads/
```

The XML file stores accounts, profiles, positions, CV metadata, and applications. Uploaded files are stored separately in the uploads directory.
