# TA Recruitment System

This project is a lightweight Java recruitment system designed for the BUPT International School teaching-assistant workflow. It follows the coursework constraints closely: pure Java, no database, no Spring Boot, and file-based persistence only.

## Current Product Structure

- Public entry:
  Portal selection page
  Applicant login and registration
  Recruiter login and registration
- Applicant Portal:
  Dashboard
  Profile management
  CV upload
  Open position browsing
  Application tracking
- Recruiter Portal:
  Dashboard
  Position publishing
  Applicant review and selection
  Workload board

## Technical Stack

- Backend: `com.sun.net.httpserver.HttpServer`
- Persistence: `XMLEncoder / XMLDecoder`
- File upload: handwritten `multipart/form-data` parsing
- Frontend: server-rendered HTML + inline CSS
- Runtime: JDK 17+

## Project Structure

```text
src/main/java/cn/edu/bupt/tarecruitment
├── config      application configuration
├── model       entity classes
├── service     business logic, matching, workload analysis
├── store       XML data persistence
├── util        shared utilities
└── web         HTTP server, routing, rendering, upload handling
```

## Java Environment

The workspace already includes a local JDK:

`/Users/fjz/software11/.tools/jdk-17.0.18+8`

Your shell config now exposes it globally through:

- `JAVA_HOME=/Users/fjz/software11/.tools/jdk-17/Contents/Home`
- `PATH=$JAVA_HOME/bin:$PATH`

You can verify it with:

```bash
java -version
javac -version
```

## Run the Project

Recommended:

```bash
/Users/fjz/software11/scripts/run.sh
```

Then open:

```text
http://localhost:8080
```

## Run on Windows

Important:

- The bundled `.tools` JDK in this project was downloaded for macOS and cannot be used on Windows.
- On Windows, install a Windows version of JDK 17 first.

Recommended steps for your classmate:

1. Install JDK 17 for Windows.
2. Open `cmd` or PowerShell in the project folder.
3. Verify Java:

```bat
java -version
javac -version
```

4. Run:

```bat
scripts\run.bat
```

5. Open:

```text
http://localhost:8080
```

If `java` or `javac` is not recognized, they need to configure `JAVA_HOME` and add `%JAVA_HOME%\bin` to `Path`.

Optional manual activation for the current shell:

```bash
source /Users/fjz/software11/scripts/use-local-jdk.sh
```

## Data Location

- System data: `/Users/fjz/software11/data/trs-data.xml`
- Uploaded CVs: `/Users/fjz/software11/data/uploads/`

## Demo Flow

1. Open the public landing page and choose either Applicant Portal or Recruiter Portal.
2. Register an applicant account and complete the profile.
3. Upload a CV and apply for a position.
4. Register a recruiter account and publish or review positions.
5. Select or reject applicants, then check the workload board.

## Notes

- The application keeps business data in XML and uploaded files in the local workspace.
- Login sessions are cookie-based and held in server memory.
- The interface has been switched to English to support presentation and review scenarios.
