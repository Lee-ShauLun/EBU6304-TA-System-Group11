# TA Recruitment System

Lightweight Java web application for the BUPT International School Teaching Assistant recruitment workflow. The project follows the coursework constraints: pure Java, no database, no Spring Boot, and XML file-based persistence.

## Features

- Applicant portal: registration, login, profile management, CV upload, job search, match preview, job application, and application status tracking.
- Recruiter portal: registration, login, position publishing, applicant review, CV download, selection/rejection decisions, and workload board.
- Admin portal: registration, login, overall dashboard, all-TA workload monitoring, and application overview across positions.
- Explainable matching: weighted rule-based matching using required skills, preferred skills, and weekly availability.
- Workload governance: balanced, at-risk, and overloaded recommendations based on selected TA workload.
- XML persistence: system data is saved to local XML files, with uploaded CVs stored in the local uploads folder.

## Technical Stack

- Java 17+
- `com.sun.net.httpserver.HttpServer`
- Server-rendered HTML and inline CSS
- `XMLEncoder` / `XMLDecoder` persistence
- Handwritten `multipart/form-data` upload parsing
- No external runtime framework or database

## Project Structure

```text
src/main/java/cn/edu/bupt/tarecruitment/
  config/      Application configuration
  model/       Plain Java data models
  service/     Business logic, matching, workload analysis, validation
  store/       XML data store and demo data
  util/        Shared utility helpers
  web/         HTTP routing, rendering, uploads, responses

src/test/java/cn/edu/bupt/tarecruitment/
  TestRunner.java  Dependency-free service-level tests

scripts/
  run.bat          Compile and run on Windows
  run.sh           Compile and run on macOS/Linux
  test.bat         Compile and run tests on Windows
  test.sh          Compile and run tests on macOS/Linux
  javadoc.bat      Generate JavaDocs on Windows
  package.bat      Build final submission ZIP on Windows

docs/
  user-manual.md
  acceptance-tests.md
  report-notes.md
  screenshots/
```

## Requirements

- JDK 17 or later
- A terminal or command prompt
- A modern web browser

Check Java:

```bash
java -version
javac -version
```

## Run on Windows

Open PowerShell or Command Prompt in the project folder:

```bat
scripts\run.bat
```

Then open:

```text
http://localhost:8080
```

## Run on macOS/Linux

```bash
chmod +x scripts/run.sh
./scripts/run.sh
```

Then open:

```text
http://localhost:8080
```

## Optional Port Configuration

The default port is `8080`. You can override it with either:

```bash
java -Dtrs.port=18080 -cp out cn.edu.bupt.tarecruitment.Main
```

or an environment variable:

```bash
TRS_PORT=18080
```

## Demo Accounts

When the XML data file does not exist, the application creates seed data with these accounts:

| Role | Username | Password |
| --- | --- | --- |
| Applicant | `demo_applicant` | `password123` |
| Recruiter | `demo_recruiter` | `password123` |
| Admin | `demo_admin` | `password123` |

You can also create new accounts from the landing page.

## Run Tests

Windows:

```bat
scripts\test.bat
```

macOS/Linux:

```bash
chmod +x scripts/test.sh
./scripts/test.sh
```

Expected result:

```text
All tests passed: 16/16
```

## Generate JavaDocs

Windows:

```bat
scripts\javadoc.bat
```

Output:

```text
docs\javadocs\
```

## Build Submission ZIP

Windows:

```bat
scripts\package.bat
```

Output:

```text
Software_group11.zip
```

The package script excludes generated classes, local XML data, uploaded files, Git metadata, logs, and local JDK tools.

## Data Storage

Runtime data is stored under:

```text
data/trs-data.xml
data/uploads/
```

These files are intentionally ignored by Git so that local test/demo data does not pollute the submitted source code.

## Main Workflows

Applicant:

1. Register or sign in.
2. Complete profile and availability.
3. Upload a CV.
4. Browse positions and review match explanations.
5. Apply for a position.
6. Track application status and decision notes.

Recruiter:

1. Register or sign in.
2. Publish a TA position.
3. Review applicants, match scores, missing skills, and CVs.
4. Select or reject applicants.
5. Review workload before making final decisions.

Admin:

1. Register or sign in.
2. Review overall dashboard metrics.
3. Check the workload board for overloaded or at-risk applicants.
4. Audit all applications across positions.

## Troubleshooting

- If `java` or `javac` is not recognized, install JDK 17+ and add it to `PATH`.
- If port `8080` is already in use, run with a different `trs.port`.
- If you want a clean demo data set, stop the server and remove `data/trs-data.xml` and `data/uploads/`; they will be recreated on the next run.
- If CV upload fails, confirm the file is PDF, DOC, DOCX, or TXT and is no larger than 5 MB.
