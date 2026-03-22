-- ============================================================
-- UDIIMS Supabase Schema — Group 11
-- Run this in Supabase SQL Editor to initialize all tables
-- ============================================================

-- 1. Departments
CREATE TABLE IF NOT EXISTS departments (
    department_id   TEXT PRIMARY KEY,
    department_name TEXT NOT NULL,
    is_technical    BOOLEAN DEFAULT false
);

-- 2. D1: Students
CREATE TABLE IF NOT EXISTS students (
    student_id              TEXT PRIMARY KEY,
    student_name            TEXT NOT NULL,
    program                 TEXT NOT NULL,
    semester                INTEGER NOT NULL DEFAULT 1,
    sgpa                    FLOAT DEFAULT 0,
    cgpa                    FLOAT DEFAULT 0,
    backlog_count           INTEGER DEFAULT 0,
    dashboard_access        BOOLEAN DEFAULT false,
    password                TEXT NOT NULL,
    department_id           TEXT REFERENCES departments(department_id),
    gpa_updated_timestamp   TIMESTAMPTZ
);

-- 3. D2: Course Offerings (available courses per semester)
CREATE TABLE IF NOT EXISTS courses (
    course_code     TEXT NOT NULL,
    course_name     TEXT NOT NULL,
    credit_hours    INTEGER NOT NULL CHECK (credit_hours > 0),
    semester_term   TEXT NOT NULL,
    department_id   TEXT REFERENCES departments(department_id),
    PRIMARY KEY (course_code, semester_term, department_id)
);

-- 4. D2: Course Registrations
CREATE TABLE IF NOT EXISTS course_registrations (
    registration_id     TEXT PRIMARY KEY,
    student_id          TEXT REFERENCES students(student_id),
    course_code         TEXT NOT NULL,
    course_name         TEXT,
    semester_term       TEXT NOT NULL,
    credit_hours        INTEGER,
    registration_status TEXT CHECK (registration_status IN ('active', 'dropped', 'completed')) DEFAULT 'active',
    backlog_flag        BOOLEAN DEFAULT false,
    grade               TEXT
);

-- 5. D3: Faculty
CREATE TABLE IF NOT EXISTS faculty (
    faculty_id      TEXT PRIMARY KEY,
    faculty_name    TEXT NOT NULL,
    designation     TEXT NOT NULL,
    department_name TEXT,
    department_id   TEXT REFERENCES departments(department_id),
    specialization  TEXT,
    active_status   BOOLEAN DEFAULT true
);

-- 6. D3: Projects
CREATE TABLE IF NOT EXISTS projects (
    project_id      TEXT PRIMARY KEY,
    project_title   TEXT NOT NULL,
    faculty_id      TEXT REFERENCES faculty(faculty_id),
    project_budget  FLOAT DEFAULT 0,
    project_status  TEXT DEFAULT 'active',
    department_id   TEXT REFERENCES departments(department_id),
    abstract        TEXT,
    publication_link TEXT
);

-- 7. D4: Inventory
CREATE TABLE IF NOT EXISTS inventory (
    item_id             TEXT PRIMARY KEY,
    item_name           TEXT NOT NULL,
    category            TEXT NOT NULL,
    quantity            INTEGER NOT NULL DEFAULT 1,
    location            TEXT,
    condition           TEXT CHECK (condition IN ('new', 'good', 'fair', 'poor', 'disposed')) DEFAULT 'good',
    assigned_department TEXT REFERENCES departments(department_id),
    is_lab_item         BOOLEAN DEFAULT false
);

-- 8. D5: Financial Records (unified table)
CREATE TABLE IF NOT EXISTS financial_records (
    record_id               TEXT PRIMARY KEY,
    record_type             TEXT NOT NULL CHECK (record_type IN ('grant', 'consultancy', 'student-fee', 'project-budget', 'expense')),
    amount                  FLOAT NOT NULL,
    transaction_date        TIMESTAMPTZ DEFAULT NOW(),
    department_id           TEXT REFERENCES departments(department_id),
    -- Student fee fields
    student_id              TEXT REFERENCES students(student_id),
    semester_term           TEXT,
    fee_status              TEXT CHECK (fee_status IN ('paid', 'pending', 'partial', 'waived')),
    fee_updated_timestamp   TIMESTAMPTZ,
    -- Grant fields
    grant_id                TEXT,
    -- Consultancy fields
    consultancy_id          TEXT,
    -- Project fields
    project_id              TEXT REFERENCES projects(project_id),
    description             TEXT
);

-- 9. Department Secretaries
CREATE TABLE IF NOT EXISTS department_secretaries (
    secretary_id    TEXT PRIMARY KEY,
    secretary_name  TEXT NOT NULL,
    department_id   TEXT REFERENCES departments(department_id),
    password        TEXT NOT NULL
);

-- 10. Finance Officers
CREATE TABLE IF NOT EXISTS finance_officers (
    officer_id      TEXT PRIMARY KEY,
    officer_name    TEXT NOT NULL,
    password        TEXT NOT NULL
);

-- ============================================================
-- SEED DATA — Demo data for testing
-- ============================================================

-- Departments
INSERT INTO departments VALUES ('CSE', 'Computer Science & Engineering', true) ON CONFLICT DO NOTHING;
INSERT INTO departments VALUES ('PHY', 'Physics', true) ON CONFLICT DO NOTHING;
INSERT INTO departments VALUES ('ENG', 'English', false) ON CONFLICT DO NOTHING;
INSERT INTO departments VALUES ('MATH', 'Mathematics', false) ON CONFLICT DO NOTHING;

-- Students
INSERT INTO students VALUES ('S001', 'Alice Johnson', 'B.Tech CSE', 4, 8.5, 8.2, 1, false, 'pass123', 'CSE', NULL) ON CONFLICT DO NOTHING;
INSERT INTO students VALUES ('S002', 'Bob Smith', 'B.Tech CSE', 3, 7.8, 7.5, 0, false, 'pass123', 'CSE', NULL) ON CONFLICT DO NOTHING;
INSERT INTO students VALUES ('S003', 'Carol Davis', 'B.Sc Physics', 2, 9.1, 8.9, 0, false, 'pass123', 'PHY', NULL) ON CONFLICT DO NOTHING;

-- Faculty
INSERT INTO faculty VALUES ('F001', 'Dr. Ramesh Kumar', 'Professor', 'Computer Science & Engineering', 'CSE', 'Machine Learning', true) ON CONFLICT DO NOTHING;
INSERT INTO faculty VALUES ('F002', 'Dr. Priya Sharma', 'Associate Professor', 'Computer Science & Engineering', 'CSE', 'Databases', true) ON CONFLICT DO NOTHING;
INSERT INTO faculty VALUES ('F003', 'Dr. Anil Verma', 'Assistant Professor', 'Physics', 'PHY', 'Quantum Mechanics', true) ON CONFLICT DO NOTHING;

-- Department Secretaries
INSERT INTO department_secretaries VALUES ('SEC001', 'Meena Gupta', 'CSE', 'sec123') ON CONFLICT DO NOTHING;
INSERT INTO department_secretaries VALUES ('SEC002', 'Rajan Patel', 'PHY', 'sec123') ON CONFLICT DO NOTHING;

-- Finance Officers
INSERT INTO finance_officers VALUES ('FIN001', 'Sunita Reddy', 'fin123') ON CONFLICT DO NOTHING;

-- Courses (D2 - offerings)
INSERT INTO courses VALUES ('CS101', 'Data Structures', 4, 'Sem-1-2025', 'CSE') ON CONFLICT DO NOTHING;
INSERT INTO courses VALUES ('CS102', 'Algorithms', 4, 'Sem-1-2025', 'CSE') ON CONFLICT DO NOTHING;
INSERT INTO courses VALUES ('CS201', 'Database Systems', 3, 'Sem-1-2025', 'CSE') ON CONFLICT DO NOTHING;
INSERT INTO courses VALUES ('CS301', 'Machine Learning', 4, 'Sem-2-2025', 'CSE') ON CONFLICT DO NOTHING;
INSERT INTO courses VALUES ('CS302', 'Operating Systems', 3, 'Sem-2-2025', 'CSE') ON CONFLICT DO NOTHING;
INSERT INTO courses VALUES ('PH101', 'Mechanics', 4, 'Sem-1-2025', 'PHY') ON CONFLICT DO NOTHING;

-- Course Registrations (D2) — with grades for GPA testing
INSERT INTO course_registrations VALUES ('REG001', 'S001', 'CS101', 'Data Structures', 'Sem-1-2024', 4, 'completed', false, 'A') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG002', 'S001', 'CS102', 'Algorithms', 'Sem-1-2024', 4, 'completed', false, 'B') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG003', 'S001', 'CS201', 'Database Systems', 'Sem-2-2024', 3, 'completed', true, 'F') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG004', 'S002', 'CS101', 'Data Structures', 'Sem-1-2024', 4, 'completed', false, 'B-') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG005', 'S002', 'CS102', 'Algorithms', 'Sem-1-2024', 4, 'completed', false, 'C') ON CONFLICT DO NOTHING;

-- Projects (D3)
INSERT INTO projects VALUES ('PRJ001', 'AI-based Student Performance Prediction', 'F001', 150000, 'active', 'CSE', 'Using ML to predict student outcomes.', '') ON CONFLICT DO NOTHING;

-- Inventory (D4)
INSERT INTO inventory VALUES ('INV001', 'Dell Laptop', 'Computer', 10, 'Lab 1', 'good', 'CSE', true) ON CONFLICT DO NOTHING;
INSERT INTO inventory VALUES ('INV002', 'Projector', 'Electronics', 5, 'Seminar Hall', 'good', 'CSE', false) ON CONFLICT DO NOTHING;
INSERT INTO inventory VALUES ('INV003', 'Whiteboard', 'Furniture', 20, 'Classrooms', 'good', 'CSE', false) ON CONFLICT DO NOTHING;

-- Financial Records (D5)
INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, grant_id) VALUES
('GR-GRANT001-001', 'grant', 500000, '2025-01-15', 'CSE', 'GRANT001') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, consultancy_id) VALUES
('CON-CON001-001', 'consultancy', 75000, '2025-02-10', 'CSE', 'CON001') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, student_id, semester_term, fee_status, fee_updated_timestamp) VALUES
('FEE-S001-Sem12025-001', 'student-fee', 45000, '2025-01-01', 'CSE', 'S001', 'Sem-1-2025', 'paid', '2025-01-10') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, student_id, semester_term, fee_status, fee_updated_timestamp) VALUES
('FEE-S002-Sem12025-001', 'student-fee', 45000, '2025-01-01', 'CSE', 'S002', 'Sem-1-2025', 'pending', '2025-01-01') ON CONFLICT DO NOTHING;
