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
    faculty_id      TEXT REFERENCES faculty(faculty_id),   -- Feature: course-faculty visibility
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
    is_lab_item         BOOLEAN DEFAULT false,
    lab_incharge_id     TEXT REFERENCES faculty(faculty_id)  -- Feature: lab incharge display (applicable when is_lab_item = true)
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

-- 9. Fee Structures — defines total fee per student per semester
CREATE TABLE IF NOT EXISTS fee_structures (
    structure_id    TEXT PRIMARY KEY,
    student_id      TEXT NOT NULL REFERENCES students(student_id),
    semester_term   TEXT NOT NULL,
    department_id   TEXT REFERENCES departments(department_id),
    total_fee       FLOAT NOT NULL CHECK (total_fee > 0),
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (student_id, semester_term)          -- one structure per student per semester
);

-- 10. Fee Payments — individual installment records
CREATE TABLE IF NOT EXISTS fee_payments (
    payment_id      TEXT PRIMARY KEY,
    student_id      TEXT NOT NULL REFERENCES students(student_id),
    semester_term   TEXT NOT NULL,
    amount          FLOAT NOT NULL CHECK (amount > 0),
    payment_date    TIMESTAMPTZ DEFAULT NOW(),
    payment_method  TEXT CHECK (payment_method IN ('cash', 'online', 'cheque', 'dd', 'upi')) DEFAULT 'online',
    notes           TEXT
    -- Overpayment constraint enforced at application layer:
    -- SUM(fee_payments WHERE student_id AND semester_term) <= fee_structures.total_fee
);

-- 11. Department Fund Sources — incoming money per department
CREATE TABLE IF NOT EXISTS department_fund_sources (
    source_id       TEXT PRIMARY KEY,
    department_id   TEXT NOT NULL REFERENCES departments(department_id),
    source_name     TEXT NOT NULL,               -- e.g. "Government Grant", "Donation", "Research Funding"
    amount          FLOAT NOT NULL CHECK (amount > 0),
    received_date   TIMESTAMPTZ DEFAULT NOW(),
    description     TEXT
);

-- 12. Department Fund Usage — expenditures per department
CREATE TABLE IF NOT EXISTS department_fund_usage (
    usage_id        TEXT PRIMARY KEY,
    department_id   TEXT NOT NULL REFERENCES departments(department_id),
    amount          FLOAT NOT NULL CHECK (amount > 0),
    usage_date      TIMESTAMPTZ DEFAULT NOW(),
    purpose         TEXT NOT NULL CHECK (purpose IN ('lab_equipment', 'maintenance', 'events', 'salaries', 'research', 'other')),
    description     TEXT
    -- Overspend constraint enforced at application layer:
    -- SUM(department_fund_usage WHERE department_id) <= SUM(department_fund_sources WHERE department_id)
);

-- 13. Department Secretaries (was 9)
CREATE TABLE IF NOT EXISTS department_secretaries (
    secretary_id    TEXT PRIMARY KEY,
    secretary_name  TEXT NOT NULL,
    department_id   TEXT REFERENCES departments(department_id),
    password        TEXT NOT NULL
);

-- 14. Finance Officers (was 10)
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

-- Students (Indian names for sample data)
INSERT INTO students VALUES ('S001', 'Aarav Sharma', 'B.Tech CSE', 4, 8.5, 8.2, 1, false, 'pass123', 'CSE', NULL) ON CONFLICT DO NOTHING;
INSERT INTO students VALUES ('S002', 'Priya Verma', 'B.Tech CSE', 3, 7.8, 7.5, 0, false, 'pass123', 'CSE', NULL) ON CONFLICT DO NOTHING;
INSERT INTO students VALUES ('S003', 'Rohan Gupta', 'B.Sc Physics', 2, 9.1, 8.9, 0, false, 'pass123', 'PHY', NULL) ON CONFLICT DO NOTHING;
INSERT INTO students VALUES ('S004', 'Neha Joshi', 'B.Tech CSE', 2, 8.0, 7.9, 0, false, 'pass123', 'CSE', NULL) ON CONFLICT DO NOTHING;

-- Faculty
INSERT INTO faculty VALUES ('F001', 'Dr. Ramesh Kumar', 'Professor', 'Computer Science & Engineering', 'CSE', 'Machine Learning', true) ON CONFLICT DO NOTHING;
INSERT INTO faculty VALUES ('F002', 'Dr. Priya Sharma', 'Associate Professor', 'Computer Science & Engineering', 'CSE', 'Databases', true) ON CONFLICT DO NOTHING;
INSERT INTO faculty VALUES ('F003', 'Dr. Anil Verma', 'Assistant Professor', 'Physics', 'PHY', 'Quantum Mechanics', true) ON CONFLICT DO NOTHING;

-- Department Secretaries
INSERT INTO department_secretaries VALUES ('SEC001', 'Meena Gupta', 'CSE', 'sec123') ON CONFLICT DO NOTHING;
INSERT INTO department_secretaries VALUES ('SEC002', 'Rajan Patel', 'PHY', 'sec123') ON CONFLICT DO NOTHING;

-- Finance Officers
INSERT INTO finance_officers VALUES ('FIN001', 'Sunita Reddy', 'fin123') ON CONFLICT DO NOTHING;

-- Courses (D2 - offerings) with faculty_id linked
INSERT INTO courses VALUES ('CS101', 'Data Structures', 4, 'Sem-1-2025', 'CSE', 'F001') ON CONFLICT DO NOTHING;
INSERT INTO courses VALUES ('CS102', 'Algorithms', 4, 'Sem-1-2025', 'CSE', 'F002') ON CONFLICT DO NOTHING;
INSERT INTO courses VALUES ('CS201', 'Database Systems', 3, 'Sem-1-2025', 'CSE', 'F002') ON CONFLICT DO NOTHING;
INSERT INTO courses VALUES ('CS301', 'Machine Learning', 4, 'Sem-2-2025', 'CSE', 'F001') ON CONFLICT DO NOTHING;
INSERT INTO courses VALUES ('CS302', 'Operating Systems', 3, 'Sem-2-2025', 'CSE', NULL) ON CONFLICT DO NOTHING;
INSERT INTO courses VALUES ('PH101', 'Mechanics', 4, 'Sem-1-2025', 'PHY', 'F003') ON CONFLICT DO NOTHING;

-- Course Registrations (D2) — with grades for GPA testing
INSERT INTO course_registrations VALUES ('REG001', 'S001', 'CS101', 'Data Structures', 'Sem-1-2024', 4, 'completed', false, 'A') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG002', 'S001', 'CS102', 'Algorithms', 'Sem-1-2024', 4, 'completed', false, 'B') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG003', 'S001', 'CS201', 'Database Systems', 'Sem-2-2024', 3, 'completed', true, 'F') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG004', 'S002', 'CS101', 'Data Structures', 'Sem-1-2024', 4, 'completed', false, 'B-') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG005', 'S002', 'CS102', 'Algorithms', 'Sem-1-2024', 4, 'completed', false, 'C') ON CONFLICT DO NOTHING;

-- Projects (D3)
INSERT INTO projects VALUES ('PRJ001', 'AI-based Student Performance Prediction', 'F001', 150000, 'active', 'CSE', 'Using ML to predict student outcomes.', '') ON CONFLICT DO NOTHING;

-- Inventory (D4) with lab_incharge_id for lab items
INSERT INTO inventory VALUES ('INV001', 'Dell Laptop', 'Computer', 10, 'Lab 1', 'good', 'CSE', true, 'F001') ON CONFLICT DO NOTHING;
INSERT INTO inventory VALUES ('INV002', 'Projector', 'Electronics', 5, 'Seminar Hall', 'good', 'CSE', false, NULL) ON CONFLICT DO NOTHING;
INSERT INTO inventory VALUES ('INV003', 'Whiteboard', 'Furniture', 20, 'Classrooms', 'good', 'CSE', false, NULL) ON CONFLICT DO NOTHING;
INSERT INTO inventory VALUES ('INV004', 'Oscilloscope', 'Electronics', 6, 'Physics Lab', 'good', 'PHY', true, 'F003') ON CONFLICT DO NOTHING;

-- Financial Records (D5)
INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, grant_id) VALUES
('GR-GRANT001-001', 'grant', 500000, '2025-01-15', 'CSE', 'GRANT001') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, consultancy_id) VALUES
('CON-CON001-001', 'consultancy', 75000, '2025-02-10', 'CSE', 'CON001') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, student_id, semester_term, fee_status, fee_updated_timestamp) VALUES
('FEE-S001-Sem12025-001', 'student-fee', 45000, '2025-01-01', 'CSE', 'S001', 'Sem-1-2025', 'paid', '2025-01-10') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, student_id, semester_term, fee_status, fee_updated_timestamp) VALUES
('FEE-S002-Sem12025-001', 'student-fee', 45000, '2025-01-01', 'CSE', 'S002', 'Sem-1-2025', 'pending', '2025-01-01') ON CONFLICT DO NOTHING;

-- Fee Structures (total fee per student per semester)
INSERT INTO fee_structures VALUES ('FS-S001-Sem12025', 'S001', 'Sem-1-2025', 'CSE', 45000, '2025-01-01') ON CONFLICT DO NOTHING;
INSERT INTO fee_structures VALUES ('FS-S002-Sem12025', 'S002', 'Sem-1-2025', 'CSE', 45000, '2025-01-01') ON CONFLICT DO NOTHING;
INSERT INTO fee_structures VALUES ('FS-S003-Sem12025', 'S003', 'Sem-1-2025', 'PHY', 40000, '2025-01-01') ON CONFLICT DO NOTHING;
INSERT INTO fee_structures VALUES ('FS-S004-Sem12025', 'S004', 'Sem-1-2025', 'CSE', 45000, '2025-01-01') ON CONFLICT DO NOTHING;

-- Fee Payments (installments — S001 paid in full, S002 partially paid, S004 partially paid)
INSERT INTO fee_payments VALUES ('FP-S001-001', 'S001', 'Sem-1-2025', 45000, '2025-01-10', 'online', 'Full payment') ON CONFLICT DO NOTHING;
INSERT INTO fee_payments VALUES ('FP-S002-001', 'S002', 'Sem-1-2025', 20000, '2025-01-15', 'online', 'First installment') ON CONFLICT DO NOTHING;
INSERT INTO fee_payments VALUES ('FP-S002-002', 'S002', 'Sem-1-2025', 10000, '2025-02-15', 'upi', 'Second installment') ON CONFLICT DO NOTHING;
INSERT INTO fee_payments VALUES ('FP-S004-001', 'S004', 'Sem-1-2025', 15000, '2025-01-20', 'cheque', 'First installment') ON CONFLICT DO NOTHING;

-- Department Fund Sources
INSERT INTO department_fund_sources VALUES ('DFS-CSE-001', 'CSE', 'Government Grant', 500000, '2025-01-15', 'Annual government funding for CSE dept') ON CONFLICT DO NOTHING;
INSERT INTO department_fund_sources VALUES ('DFS-CSE-002', 'CSE', 'Research Funding', 200000, '2025-03-01', 'DRDO research grant') ON CONFLICT DO NOTHING;
INSERT INTO department_fund_sources VALUES ('DFS-CSE-003', 'CSE', 'Donation', 50000, '2025-02-10', 'Alumni donation by Sanjay Mehta') ON CONFLICT DO NOTHING;
INSERT INTO department_fund_sources VALUES ('DFS-PHY-001', 'PHY', 'Government Grant', 300000, '2025-01-20', 'Annual government funding for Physics dept') ON CONFLICT DO NOTHING;

-- Department Fund Usage
INSERT INTO department_fund_usage VALUES ('DFU-CSE-001', 'CSE', 120000, '2025-02-05', 'lab_equipment', 'Purchased 10 Dell laptops for Lab 1') ON CONFLICT DO NOTHING;
INSERT INTO department_fund_usage VALUES ('DFU-CSE-002', 'CSE', 35000, '2025-03-10', 'maintenance', 'Annual maintenance of server room') ON CONFLICT DO NOTHING;
INSERT INTO department_fund_usage VALUES ('DFU-CSE-003', 'CSE', 25000, '2025-04-01', 'events', 'Tech Fest 2025 event expenses') ON CONFLICT DO NOTHING;
INSERT INTO department_fund_usage VALUES ('DFU-PHY-001', 'PHY', 80000, '2025-02-20', 'lab_equipment', 'Oscilloscopes and lab instruments') ON CONFLICT DO NOTHING;
