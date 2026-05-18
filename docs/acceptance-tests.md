# Acceptance Test Checklist

Use this checklist before final submission and during viva preparation.

## Applicant Tests

| ID | Test | Expected Result | Status |
| --- | --- | --- | --- |
| AT-01 | Register applicant account | Account is created and redirected to profile page | TODO |
| AT-02 | Login as applicant | Applicant dashboard is displayed | TODO |
| AT-03 | Save valid profile | Success message appears and data persists | TODO |
| AT-04 | Save invalid email | Validation error is shown | TODO |
| AT-05 | Upload valid CV | CV metadata is shown and file can be downloaded | TODO |
| AT-06 | Upload invalid CV type | Upload is rejected | TODO |
| AT-07 | Apply without CV | Application is rejected | TODO |
| AT-08 | Browse positions | Open positions and match scores are displayed | TODO |
| AT-09 | Apply for position | Application appears as pending | TODO |
| AT-10 | Apply twice to same position | Duplicate application is rejected | TODO |
| AT-11 | Check application status | Pending, selected, rejected, and notes are visible | TODO |

## Recruiter Tests

| ID | Test | Expected Result | Status |
| --- | --- | --- | --- |
| AT-12 | Register/login recruiter | Recruiter dashboard is displayed | TODO |
| AT-13 | Publish valid position | New position appears in position list | TODO |
| AT-14 | Publish invalid position email | Validation error is shown | TODO |
| AT-15 | Review applications | Applicant, CV, match score, and missing skills are visible | TODO |
| AT-16 | Select applicant | Application becomes selected | TODO |
| AT-17 | Reject applicant | Application becomes rejected | TODO |
| AT-18 | Fill quota | Position becomes filled after quota is reached | TODO |
| AT-19 | View workload board | Balanced, at-risk, and overloaded states are visible | TODO |

## Admin Tests

| ID | Test | Expected Result | Status |
| --- | --- | --- | --- |
| AT-20 | Register/login admin | Admin dashboard is displayed | TODO |
| AT-21 | View admin workload | All applicant workload entries are visible | TODO |
| AT-22 | Identify overloaded applicant | Overloaded status and recommendation are visible | TODO |
| AT-23 | View all applications | Cross-position application overview is visible | TODO |

## System Tests

| ID | Test | Expected Result | Status |
| --- | --- | --- | --- |
| AT-24 | Restart server | XML data persists | TODO |
| AT-25 | Access page without login | User is redirected to login | TODO |
| AT-26 | Access another role's portal | User is redirected back to own portal | TODO |
| AT-27 | Visit invalid route | User-friendly 404 page is shown | TODO |
| AT-28 | Run automated tests | `scripts/test.bat` reports all tests passed | TODO |
