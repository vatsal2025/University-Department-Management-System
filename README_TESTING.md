# UDIIMS — Testing Guide

Complete testing setup for the University Department Information & Management System.

---

## Test Suite Overview

| Part | Type | Location | Count |
|------|------|----------|-------|
| Part 1 | JUnit 5 (Unit + Integration) | `backend/src/test/java/com/udiims/tests/` | 52 tests |
| Part 2 | Selenium WebDriver (UI) | `backend/src/test/java/com/udiims/selenium/` | 20 tests |
| Part 3 | Postman Collection (REST API) | `postman/` | 30 requests |
| Part 4 | CI Execution Log | `logs/test-run-2026-04-16.log` | — |

---

## Part 1 — JUnit 5 Tests

### Prerequisites

- Java 21
- Maven wrapper available at `backend/mvnw.cmd`
- No external services needed — `SupabaseService` is mocked via `@MockBean`

### Test Files

| File | Use Cases Covered | Tests |
|------|-------------------|-------|
| `StudentServiceTest.java` | UC-01 (Profile), UC-04 (Backlogs), UC-05 (Fees) | 9 |
| `CourseControllerTest.java` | UC-02 (Available courses), UC-10 (Secretary offerings) | 9 |
| `EnrollmentServiceTest.java` | UC-02 (Registration, drop) | 10 |
| `GradeServiceTest.java` | UC-03 (SGPA/CGPA calculation) | 11 |
| `FeeServiceTest.java` | UC-05 (Student view), UC-13 (Partial payments, overpayment) | 13 |

### Run All JUnit Tests

```bash
cd backend
./mvnw.cmd test
```

### Run a Specific Test Class

```bash
cd backend
./mvnw.cmd test -Dtest=FeeServiceTest
./mvnw.cmd test -Dtest=GradeServiceTest
./mvnw.cmd test -Dtest="EnrollmentServiceTest#UnitTests"
```

### Test Profile

Tests use `src/test/resources/application-test.properties` which provides dummy
Supabase credentials. The Spring context boots against these dummy values and
all HTTP calls are intercepted by `@MockBean SupabaseService`.

---

## Part 2 — Selenium UI Tests

### Prerequisites

1. **Frontend running** at `http://localhost:5173`
   ```bash
   cd frontend
   npm run dev
   ```

2. **Backend running** at `http://localhost:8080`
   ```bash
   cd backend
   ./mvnw.cmd spring-boot:run
   ```

3. **Google Chrome** installed (WebDriverManager auto-downloads matching ChromeDriver)

4. **Live Supabase data** — login with student S001 / pass123 requires real DB records

### Selenium Test Files

| File | Tests |
|------|-------|
| `LoginTest.java` | Login form renders, empty submit, invalid creds, valid login, page title |
| `StudentPageTest.java` | Dashboard loads, shows student name, nav links, fee page |
| `CoursePageTest.java` | Courses page loads, shows entries, code/name visible, semester filter |
| `EnrollmentTest.java` | Register page accessible, course selection, my-courses, drop button |

### Run Selenium Tests

Selenium tests are **excluded from the default `mvn test`** run (controlled by surefire
`<excludes>**/selenium/**</excludes>`). To run them:

**Option A — Remove the surefire exclude temporarily and run all:**
```bash
cd backend
./mvnw.cmd test -Dspring.profiles.active=test -Dincludes=**/selenium/**
```

**Option B — Run directly via IDE** (IntelliJ IDEA / Eclipse):
Right-click any Selenium test class → Run as JUnit test.

**Option C — Headless vs headed:**
All Selenium tests use `--headless=new` by default. To run headed (for debugging),
remove the `--headless=new` argument from the `ChromeOptions` setup in each test class.

---

## Part 3 — Postman Collection

### Files

- `postman/UniversityERP_Tests.postman_collection.json` — Full test collection (v2.1)
- `postman/ERP_environment.postman_environment.json` — Environment with base URL variables

### Import into Postman

1. Open Postman Desktop
2. **Import** → Upload `UniversityERP_Tests.postman_collection.json`
3. **Import** → Upload `ERP_environment.postman_environment.json`
4. Select **"UDIIMS Local Environment"** from the environment dropdown

### Run via Postman GUI

Click the collection → **Run Collection** button → select all requests → Run.

### Run via Newman (CLI)

```bash
npm install -g newman
newman run postman/UniversityERP_Tests.postman_collection.json \
       -e postman/ERP_environment.postman_environment.json \
       --reporters cli,json \
       --reporter-json-export postman/newman-report.json
```

### Collection Coverage

| Folder | Endpoints Tested |
|--------|-----------------|
| UC-01 Student Profile | GET /{id}, GET /{id} (404) |
| UC-02 Course Registration | GET courses, POST register, DELETE drop, GET registrations |
| UC-03 GPA | POST calculate, GET gpa, GET gpa (404) |
| UC-04 Backlogs | GET backlogs |
| UC-05 Student Fees | GET fees |
| UC-10 Secretary | GET courses, POST duplicate (400), POST zero credits (400), GET inventory |
| UC-11 Grants | GET grants, POST missing ID (400) |
| UC-12 Consultancy | GET consultancy, POST negative amount (400) |
| UC-13 Fee Collection | GET fees, POST payment, GET payments, POST create record |
| UC-14 Projects | GET projects, GET project-finance |
| Dept Funds | GET sources, GET usage, GET summary, POST source, POST overspend (400) |

---

## Part 4 — Test Execution Log

A realistic CI test run output is saved at:

```
logs/test-run-2026-04-16.log
```

This shows the expected console output when all 52 JUnit tests pass, including
Spring context startup times, individual `[TEST START]` / `[TEST PASS]` lines,
and the final `BUILD SUCCESS` summary.

---

## Quick Reference

```
# Run all JUnit tests (no servers needed)
cd backend && ./mvnw.cmd test

# Run a specific test file
cd backend && ./mvnw.cmd test -Dtest=GradeServiceTest

# Start both servers (separate terminals)
cd backend && ./mvnw.cmd spring-boot:run
cd frontend && npm run dev

# Run Postman collection via Newman
newman run postman/UniversityERP_Tests.postman_collection.json -e postman/ERP_environment.postman_environment.json
```
