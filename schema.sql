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

-- Course Registrations (D2)
-- S001: Aarav Sharma — B.Tech CSE, Semester 4, 1 backlog
-- Sem 1 (Sem-1-2024)
INSERT INTO course_registrations VALUES ('REG001', 'S001', 'CS101', 'Data Structures', 'Sem-1-2024', 4, 'completed', false, 'A') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG002', 'S001', 'CS102', 'Algorithms', 'Sem-1-2024', 4, 'completed', false, 'B+') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG003', 'S001', 'CS103', 'Discrete Mathematics', 'Sem-1-2024', 3, 'completed', false, 'A-') ON CONFLICT DO NOTHING;
-- Sem 2 (Sem-2-2024) — F in CS201 is the backlog
INSERT INTO course_registrations VALUES ('REG004', 'S001', 'CS201', 'Database Systems', 'Sem-2-2024', 3, 'completed', true, 'F') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG005', 'S001', 'CS202', 'Computer Networks', 'Sem-2-2024', 4, 'completed', false, 'A') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG006', 'S001', 'CS203', 'Object Oriented Programming', 'Sem-2-2024', 4, 'completed', false, 'A') ON CONFLICT DO NOTHING;
-- Sem 3 (Sem-1-2025) — completed
INSERT INTO course_registrations VALUES ('REG007', 'S001', 'CS310', 'Software Engineering', 'Sem-1-2025', 3, 'completed', false, 'B+') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG008', 'S001', 'CS311', 'Computer Architecture', 'Sem-1-2025', 4, 'completed', false, 'A-') ON CONFLICT DO NOTHING;
-- Sem 4 (Sem-2-2025, current — active)
INSERT INTO course_registrations VALUES ('REG009', 'S001', 'CS301', 'Machine Learning', 'Sem-2-2025', 4, 'active', false, NULL) ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG010', 'S001', 'CS302', 'Operating Systems', 'Sem-2-2025', 3, 'active', false, NULL) ON CONFLICT DO NOTHING;

-- S002: Priya Verma — B.Tech CSE, Semester 3, 0 backlogs
-- Sem 1 (Sem-1-2024)
INSERT INTO course_registrations VALUES ('REG012', 'S002', 'CS110', 'Introduction to Programming', 'Sem-1-2024', 4, 'completed', false, 'B+') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG013', 'S002', 'MA110', 'Engineering Mathematics I', 'Sem-1-2024', 4, 'completed', false, 'A-') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG014', 'S002', 'PH110', 'Engineering Physics', 'Sem-1-2024', 3, 'completed', false, 'B+') ON CONFLICT DO NOTHING;
-- Sem 2 (Sem-2-2024)
INSERT INTO course_registrations VALUES ('REG015', 'S002', 'CS101', 'Data Structures', 'Sem-2-2024', 4, 'completed', false, 'B+') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG016', 'S002', 'CS102', 'Algorithms', 'Sem-2-2024', 4, 'completed', false, 'B') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG017', 'S002', 'CS103', 'Discrete Mathematics', 'Sem-2-2024', 3, 'completed', false, 'A-') ON CONFLICT DO NOTHING;
-- Sem 3 (Sem-1-2025, current)
INSERT INTO course_registrations VALUES ('REG018', 'S002', 'CS201', 'Database Systems', 'Sem-1-2025', 3, 'active', false, NULL) ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG019', 'S002', 'CS202', 'Computer Networks', 'Sem-1-2025', 4, 'active', false, NULL) ON CONFLICT DO NOTHING;

-- S003: Rohan Gupta — B.Sc Physics, Semester 2, 0 backlogs
-- Sem 1 (Sem-2-2024)
INSERT INTO course_registrations VALUES ('REG020', 'S003', 'PH110', 'Engineering Physics', 'Sem-2-2024', 4, 'completed', false, 'A') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG021', 'S003', 'MA110', 'Engineering Mathematics I', 'Sem-2-2024', 4, 'completed', false, 'A-') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG022', 'S003', 'PH111', 'Waves and Optics', 'Sem-2-2024', 3, 'completed', false, 'A') ON CONFLICT DO NOTHING;
-- Sem 2 (Sem-1-2025, current)
INSERT INTO course_registrations VALUES ('REG023', 'S003', 'PH101', 'Mechanics', 'Sem-1-2025', 4, 'active', false, NULL) ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG024', 'S003', 'PH201', 'Electromagnetism', 'Sem-1-2025', 4, 'active', false, NULL) ON CONFLICT DO NOTHING;

-- S004: Neha Joshi — B.Tech CSE, Semester 2, 0 backlogs
-- Sem 1 (Sem-2-2024)
INSERT INTO course_registrations VALUES ('REG025', 'S004', 'CS110', 'Introduction to Programming', 'Sem-2-2024', 4, 'completed', false, 'B+') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG026', 'S004', 'MA110', 'Engineering Mathematics I', 'Sem-2-2024', 4, 'completed', false, 'A-') ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG027', 'S004', 'PH110', 'Engineering Physics', 'Sem-2-2024', 3, 'completed', false, 'B') ON CONFLICT DO NOTHING;
-- Sem 2 (Sem-1-2025, current)
INSERT INTO course_registrations VALUES ('REG028', 'S004', 'CS101', 'Data Structures', 'Sem-1-2025', 4, 'active', false, NULL) ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG029', 'S004', 'CS102', 'Algorithms', 'Sem-1-2025', 4, 'active', false, NULL) ON CONFLICT DO NOTHING;
INSERT INTO course_registrations VALUES ('REG030', 'S004', 'CS103', 'Discrete Mathematics', 'Sem-1-2025', 3, 'active', false, NULL) ON CONFLICT DO NOTHING;

-- Projects (D3)
INSERT INTO projects VALUES ('PRJ001', 'AI-based Student Performance Prediction', 'F001', 150000, 'active', 'CSE', 'Using ML to predict student outcomes.', '') ON CONFLICT DO NOTHING;
INSERT INTO projects VALUES ('PRJ002', 'Quantum Simulation for Condensed Matter Physics', 'F003', 200000, 'active', 'PHY', 'Simulating quantum behaviour of condensed matter systems.', '') ON CONFLICT DO NOTHING;
INSERT INTO projects VALUES ('PRJ003', 'NLP Tools for Regional Language Processing', 'F001', 100000, 'completed', 'CSE', 'Developing NLP models for Indian regional languages.', '') ON CONFLICT DO NOTHING;

-- Inventory (D4) with lab_incharge_id for lab items
INSERT INTO inventory VALUES ('INV001', 'Dell Laptop', 'Computer', 10, 'Lab 1', 'good', 'CSE', true, 'F001') ON CONFLICT DO NOTHING;
INSERT INTO inventory VALUES ('INV002', 'Projector', 'Electronics', 5, 'Seminar Hall', 'good', 'CSE', false, NULL) ON CONFLICT DO NOTHING;
INSERT INTO inventory VALUES ('INV003', 'Whiteboard', 'Furniture', 20, 'Classrooms', 'good', 'CSE', false, NULL) ON CONFLICT DO NOTHING;
INSERT INTO inventory VALUES ('INV004', 'Oscilloscope', 'Electronics', 6, 'Physics Lab', 'good', 'PHY', true, 'F003') ON CONFLICT DO NOTHING;

-- Financial Records (D5)

-- Grants (format: GRANT-<DeptID>-<Number>)
INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, grant_id, description) VALUES
('GR-GRANT-CSE-001-001', 'grant', 800000, '2025-01-10', 'CSE', 'GRANT-CSE-001',
'Grant Title: DST Machine Learning Research Infrastructure
Funding Agency: Department of Science and Technology (DST)
Principal Investigator: Dr. Ramesh Kumar
Allocated To: AI & ML Research Lab, CSE
Objectives: Procurement of high-performance computing infrastructure for ML research
Grant Period: Jan 2025 to Dec 2026') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, grant_id, description) VALUES
('GR-GRANT-CSE-002-001', 'grant', 350000, '2025-03-05', 'CSE', 'GRANT-CSE-002',
'Grant Title: UGC Curriculum Development & Modernization
Funding Agency: University Grants Commission (UGC)
Principal Investigator: Dr. Priya Sharma
Allocated To: CSE Department (Academic Division)
Objectives: Modernizing undergraduate curriculum with AI and cloud computing electives
Grant Period: Mar 2025 to Feb 2026') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, grant_id, description) VALUES
('GR-GRANT-PHY-001-001', 'grant', 600000, '2025-02-14', 'PHY', 'GRANT-PHY-001',
'Grant Title: SERB Quantum Optics Laboratory Setup
Funding Agency: Science and Engineering Research Board (SERB)
Principal Investigator: Dr. Anil Verma
Allocated To: Quantum Mechanics Research Group, PHY
Objectives: Establishing quantum optics lab for experimental research in photonics
Grant Period: Feb 2025 to Jan 2027') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, grant_id, description) VALUES
('GR-GRANT-MATH-001-001', 'grant', 200000, '2025-04-01', 'MATH', 'GRANT-MATH-001',
'Grant Title: NBHM Research Promotion Grant
Funding Agency: National Board for Higher Mathematics (NBHM)
Principal Investigator: To Be Assigned
Allocated To: Mathematics Department
Objectives: Supporting graduate-level research in applied mathematics and number theory
Grant Period: Apr 2025 to Mar 2026') ON CONFLICT DO NOTHING;

-- Consultancy (format: CONSULT-<DeptID>-<Number>)
INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, consultancy_id, description) VALUES
('CON-CONSULT-CSE-001-001', 'consultancy', 150000, '2025-02-20', 'CSE', 'CONSULT-CSE-001',
'Project Title: Machine Learning Model Optimization for Customer Analytics
Client / Industry Partner: Tata Consultancy Services (TCS)
Lead Consultant: Dr. Ramesh Kumar
Scope of Work: Development and optimization of ML models for retail customer segmentation
Contract Period: Jan 2025 to Apr 2025') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, consultancy_id, description) VALUES
('CON-CONSULT-CSE-002-001', 'consultancy', 85000, '2025-03-15', 'CSE', 'CONSULT-CSE-002',
'Project Title: Database Performance Tuning and Query Optimization
Client / Industry Partner: Infosys Ltd.
Lead Consultant: Dr. Priya Sharma
Scope of Work: Analyzing and restructuring legacy database architecture for improved performance
Contract Period: Feb 2025 to Mar 2025') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, consultancy_id, description) VALUES
('CON-CONSULT-PHY-001-001', 'consultancy', 220000, '2025-01-25', 'PHY', 'CONSULT-PHY-001',
'Project Title: Material Stress Testing and Thermal Analysis Services
Client / Industry Partner: Indian Space Research Organisation (ISRO)
Lead Consultant: Dr. Anil Verma
Scope of Work: Structural and thermal analysis of satellite component materials under simulated space conditions
Contract Period: Jan 2025 to Jun 2025') ON CONFLICT DO NOTHING;

-- Student Fees
INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, student_id, semester_term, fee_status, fee_updated_timestamp, description) VALUES
('FEE-S001-Sem12025-001', 'student-fee', 45000, '2025-01-01', 'CSE', 'S001', 'Sem-1-2025', 'paid', '2025-01-10',
'Fee Category: Tuition
Fee Type: Regular
Notes: B.Tech CSE Year 4, Sem-1-2025 tuition fee') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, student_id, semester_term, fee_status, fee_updated_timestamp, description) VALUES
('FEE-S001-Sem22025-001', 'student-fee', 45000, '2025-07-01', 'CSE', 'S001', 'Sem-2-2025', 'pending', '2025-07-01',
'Fee Category: Tuition
Fee Type: Regular
Notes: B.Tech CSE Year 4, Sem-2-2025 tuition fee') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, student_id, semester_term, fee_status, fee_updated_timestamp, description) VALUES
('FEE-S002-Sem12025-001', 'student-fee', 45000, '2025-01-01', 'CSE', 'S002', 'Sem-1-2025', 'partial', '2025-02-15',
'Fee Category: Tuition
Fee Type: Regular
Notes: B.Tech CSE Year 3, Sem-1-2025 tuition fee — partial payment in progress') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, student_id, semester_term, fee_status, fee_updated_timestamp, description) VALUES
('FEE-S003-Sem12025-001', 'student-fee', 40000, '2025-01-01', 'PHY', 'S003', 'Sem-1-2025', 'paid', '2025-01-08',
'Fee Category: Tuition
Fee Type: Regular
Notes: B.Sc Physics Year 2, Sem-1-2025 tuition fee') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, student_id, semester_term, fee_status, fee_updated_timestamp, description) VALUES
('FEE-S004-Sem12025-001', 'student-fee', 45000, '2025-01-01', 'CSE', 'S004', 'Sem-1-2025', 'partial', '2025-02-20',
'Fee Category: Tuition
Fee Type: Regular
Notes: B.Tech CSE Year 2, Sem-1-2025 tuition fee — installment plan approved') ON CONFLICT DO NOTHING;

-- Fee Structures (total fee per student per semester)
INSERT INTO fee_structures VALUES ('FS-S001-Sem12025', 'S001', 'Sem-1-2025', 'CSE', 45000, '2025-01-01') ON CONFLICT DO NOTHING;
INSERT INTO fee_structures VALUES ('FS-S001-Sem22025', 'S001', 'Sem-2-2025', 'CSE', 45000, '2025-07-01') ON CONFLICT DO NOTHING;
INSERT INTO fee_structures VALUES ('FS-S002-Sem12025', 'S002', 'Sem-1-2025', 'CSE', 45000, '2025-01-01') ON CONFLICT DO NOTHING;
INSERT INTO fee_structures VALUES ('FS-S003-Sem12025', 'S003', 'Sem-1-2025', 'PHY', 40000, '2025-01-01') ON CONFLICT DO NOTHING;
INSERT INTO fee_structures VALUES ('FS-S004-Sem12025', 'S004', 'Sem-1-2025', 'CSE', 45000, '2025-01-01') ON CONFLICT DO NOTHING;

-- Fee Payments
INSERT INTO fee_payments VALUES ('FP-S001-001', 'S001', 'Sem-1-2025', 45000, '2025-01-10', 'online', 'Full payment — cleared in single transaction') ON CONFLICT DO NOTHING;
INSERT INTO fee_payments VALUES ('FP-S002-001', 'S002', 'Sem-1-2025', 20000, '2025-01-15', 'online', 'First installment') ON CONFLICT DO NOTHING;
INSERT INTO fee_payments VALUES ('FP-S002-002', 'S002', 'Sem-1-2025', 10000, '2025-02-15', 'upi', 'Second installment') ON CONFLICT DO NOTHING;
INSERT INTO fee_payments VALUES ('FP-S003-001', 'S003', 'Sem-1-2025', 40000, '2025-01-08', 'online', 'Full payment') ON CONFLICT DO NOTHING;
INSERT INTO fee_payments VALUES ('FP-S004-001', 'S004', 'Sem-1-2025', 15000, '2025-01-20', 'cheque', 'First installment') ON CONFLICT DO NOTHING;
INSERT INTO fee_payments VALUES ('FP-S004-002', 'S004', 'Sem-1-2025', 10000, '2025-02-20', 'upi', 'Second installment') ON CONFLICT DO NOTHING;

-- Project Finance
-- PRJ001: AI-based Student Performance Prediction (budget ₹1,50,000)
INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, project_id, description) VALUES
('PRO-PRJ001-001', 'project-budget', 150000, '2025-01-05', 'CSE', 'PRJ001',
'Expense Category: Budget Allocation
Authorized By: Department Head, CSE
Vendor / Payee: N/A
Purpose: Initial project budget allocation for AI-based student performance prediction system') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, project_id, description) VALUES
('EXP-PRJ001-001', 'expense', 72000, '2025-02-10', 'CSE', 'PRJ001',
'Expense Category: Equipment
Authorized By: Dr. Ramesh Kumar
Vendor / Payee: Dell India Pvt. Ltd.
Purpose: GPU workstation procurement for ML model training') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, project_id, description) VALUES
('EXP-PRJ001-002', 'expense', 18500, '2025-03-01', 'CSE', 'PRJ001',
'Expense Category: Software
Authorized By: Dr. Ramesh Kumar
Vendor / Payee: MathWorks India (MATLAB)
Purpose: Annual academic license for MATLAB and Simulink toolboxes') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, project_id, description) VALUES
('EXP-PRJ001-003', 'expense', 21000, '2025-04-05', 'CSE', 'PRJ001',
'Expense Category: Travel
Authorized By: Dr. Ramesh Kumar
Vendor / Payee: ICML 2025 Conference
Purpose: Conference registration and travel expenses for presenting research paper') ON CONFLICT DO NOTHING;

-- PRJ002: Quantum Simulation for Condensed Matter Physics (budget ₹2,00,000)
INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, project_id, description) VALUES
('PRO-PRJ002-001', 'project-budget', 200000, '2025-01-20', 'PHY', 'PRJ002',
'Expense Category: Budget Allocation
Authorized By: Department Head, PHY
Vendor / Payee: N/A
Purpose: Initial budget allocation for quantum simulation research project') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, project_id, description) VALUES
('EXP-PRJ002-001', 'expense', 95000, '2025-02-25', 'PHY', 'PRJ002',
'Expense Category: Equipment
Authorized By: Dr. Anil Verma
Vendor / Payee: Tektronix India
Purpose: Quantum optics measurement instruments and precision oscilloscopes') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, project_id, description) VALUES
('EXP-PRJ002-002', 'expense', 32000, '2025-03-20', 'PHY', 'PRJ002',
'Expense Category: Software
Authorized By: Dr. Anil Verma
Vendor / Payee: Wolfram Research
Purpose: Mathematica and Wolfram Alpha Pro licenses for simulation work') ON CONFLICT DO NOTHING;

-- PRJ003: NLP Tools for Regional Language Processing (budget ₹1,00,000, completed)
INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, project_id, description) VALUES
('PRO-PRJ003-001', 'project-budget', 100000, '2024-06-01', 'CSE', 'PRJ003',
'Expense Category: Budget Allocation
Authorized By: Department Head, CSE
Vendor / Payee: N/A
Purpose: Initial budget allocation for NLP regional language tools project') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, project_id, description) VALUES
('EXP-PRJ003-001', 'expense', 58000, '2024-08-15', 'CSE', 'PRJ003',
'Expense Category: Personnel
Authorized By: Dr. Ramesh Kumar
Vendor / Payee: Research Assistants (2 positions)
Purpose: Stipend for two research assistants for 6-month project duration') ON CONFLICT DO NOTHING;

INSERT INTO financial_records (record_id, record_type, amount, transaction_date, department_id, project_id, description) VALUES
('EXP-PRJ003-002', 'expense', 28000, '2024-10-01', 'CSE', 'PRJ003',
'Expense Category: Equipment
Authorized By: Dr. Ramesh Kumar
Vendor / Payee: Amazon Web Services India
Purpose: Cloud computing credits for model training and API hosting') ON CONFLICT DO NOTHING;

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
