# University Department Information Management System (UDIIMS)

A full-stack web application for managing university department operations across three user roles: Students, Department Secretaries, and Finance Officers.

---

## Tech Stack

| Layer     | Technology                        |
|-----------|-----------------------------------|
| Frontend  | React 18 + Vite                   |
| Backend   | Java 21 + Spring Boot 3.5         |
| Database  | Supabase (PostgreSQL)             |
| HTTP      | Axios (frontend) / RestTemplate (backend) |

---

## Project Structure

```
UDIIMS/
├── backend/                    # Spring Boot REST API (port 8080)
│   └── src/main/java/com/udiims/
│       ├── controller/         # AuthController, StudentController, SecretaryController, FinanceController
│       ├── service/            # Business logic per role + SupabaseService
│       └── config/             # CorsConfig
├── frontend/                   # React + Vite app (port 5173)
│   └── src/
│       ├── api/                # Axios API client (api.js)
│       ├── context/            # Auth context
│       └── pages/
│           ├── student/        # UC-01 to UC-05
│           ├── secretary/      # UC-06 to UC-10
│           └── finance/        # UC-11 to UC-14
├── schema.sql                  # Supabase database schema
└── swe docs/                   # SRS, PRD, Use Case, DFD, Class & Sequence diagrams
```

---

## Modules & Use Cases

### Module 1 — Student
| Use Case | Description |
|----------|-------------|
| UC-01 | Student Authentication (roll number login) |
| UC-02 | Course Registration |
| UC-03 | GPA Calculation (SGPA / CGPA) |
| UC-04 | Program & Backlog Tracking |
| UC-05 | Fee Status View (read-only) |

### Module 2 — Department Secretary
| Use Case | Description |
|----------|-------------|
| UC-06 | Faculty Management (CRUD) |
| UC-07 | Project Management (CRUD) |
| UC-08 | Inventory Management (CRUD) |
| UC-09 | Department Accounts View (read-only) |
| UC-10 | Course Offering Management (CRUD) |

### Module 3 — Finance Officer
| Use Case | Description |
|----------|-------------|
| UC-11 | Grant Management |
| UC-12 | Consultancy Fund Management |
| UC-13 | Fee Collection & Modification |
| UC-14 | Project Financial Management |

---

## Getting Started

### Prerequisites
- Java 21+
- Node.js 18+ / npm
- A [Supabase](https://supabase.com) project with the schema from `schema.sql`

### 1. Configure the Backend

```bash
cd backend/src/main/resources
cp application.properties.example application.properties
```

Open `application.properties` and fill in your Supabase credentials:

```properties
supabase.url=https://<your-project-ref>.supabase.co
supabase.anon-key=<your-supabase-anon-key>
supabase.service-key=<your-supabase-service-role-key>
```

### 2. Run the Backend

```bash
cd backend
./mvnw spring-boot:run          # Linux / macOS
mvnw.cmd spring-boot:run        # Windows
```

The API will be available at `http://localhost:8080`.

### 3. Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`.

---

## Database Setup

Run `schema.sql` against your Supabase project (SQL Editor) to create all tables:

- `students`
- `courses` + `course_registrations`
- `faculty` + `projects`
- `inventory`
- `financial_records`

---

## API Overview

| Prefix | Controller | Handles |
|--------|------------|---------|
| `/api/auth` | AuthController | Login for all roles |
| `/api/students` | StudentController | UC-01 to UC-05 |
| `/api/secretary` | SecretaryController | UC-06 to UC-10 |
| `/api/finance` | FinanceController | UC-11 to UC-14 |

---

## Group

**Group 11** — Software Engineering Project
