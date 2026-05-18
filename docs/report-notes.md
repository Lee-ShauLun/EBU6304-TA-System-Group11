# Final Report Notes

## Scope

The system supports the TA recruitment workflow for Applicants, Recruiters/MOs, and Admin users. It focuses on core recruitment activities required by the coursework: applicant profile creation, CV upload, job discovery, job application, application review, applicant selection, and workload monitoring.

## Design Strategy

- Keep the design simple and modular.
- Use separate layers for model, service, storage, web routing, rendering, and utilities.
- Avoid databases and heavy frameworks to comply with coursework constraints.
- Use XML for simple file-based persistence.
- Use role-based portals to separate responsibilities.

## Architecture Evidence

- `model`: JavaBeans used by XML persistence.
- `service`: business rules, validation, matching, workload analysis.
- `store`: XML read/write and demo data creation.
- `web`: HTTP routing, HTML rendering, file upload/download.
- `util`: shared escaping, encoding, password helpers.

## Implementation Summary

- Applicant portal: profile, CV, positions, applications.
- Recruiter portal: position publishing, application review, selection/rejection, workload board.
- Admin portal: dashboard, workload governance, application overview.
- Matching: transparent weighted scoring.
- Workload: selected positions are converted into weekly assigned hours.

## Testing Strategy

The project uses a dependency-free Java `TestRunner` instead of external test frameworks. This keeps the project simple and compatible with the pure Java coursework constraint.

Automated tests cover:

- registration and login
- duplicate username validation
- profile validation
- position validation
- CV validation
- match preview
- application workflow
- duplicate application prevention
- selection workflow
- workload detection
- admin authentication

Manual acceptance tests are listed in `docs/acceptance-tests.md`.

## Use of Gen AI

Gen AI was used as a development assistant for brainstorming requirements, identifying risks, drafting tests, and improving documentation. The implementation does not blindly accept opaque AI decisions. Matching is implemented as an explainable rule-based decision support feature with transparent weights:

- required skills: 70%
- preferred skills: 20%
- weekly availability: 10%

The system displays matched skills, missing skills, and score explanation to support human decision-making.

## Ethical and Privacy Considerations

- The system avoids a database and stores only local XML data.
- Passwords are stored as SHA-256 hashes rather than plain text.
- CV upload validates file type and size.
- Admin and recruiter views are role-protected.
- Error messages avoid exposing internal server exception details.

## Limitations and Future Work

- Password hashing could be improved with per-user salts.
- CSV export could be added for legacy Excel workflows.
- Email notifications are not implemented.
- A richer UI framework could improve maintainability, but was intentionally avoided to preserve coursework constraints.

## Individual Statements

Each member should add a short statement of no more than 300 words covering:

- assigned issues and branches
- code or documentation contribution
- testing/review contribution
- reflection on software engineering learning
