-- ============================================================
-- UDIIMS Migration — run this ONCE in Supabase SQL Editor
-- Dashboard → SQL Editor → New query → Paste → Run
-- ============================================================

-- 1. Add faculty_id to courses (course-faculty visibility)
ALTER TABLE courses ADD COLUMN IF NOT EXISTS faculty_id TEXT REFERENCES faculty(faculty_id);

-- 2. Add lab_incharge_id to inventory (lab incharge display)
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS lab_incharge_id TEXT REFERENCES faculty(faculty_id);

-- 3. Fee Structures — total fee definition per student per semester
CREATE TABLE IF NOT EXISTS fee_structures (
    structure_id  TEXT PRIMARY KEY,
    student_id    TEXT NOT NULL REFERENCES students(student_id),
    semester_term TEXT NOT NULL,
    department_id TEXT REFERENCES departments(department_id),
    total_fee     FLOAT NOT NULL CHECK (total_fee > 0),
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (student_id, semester_term)
);

-- 4. Fee Payments — individual installment records
CREATE TABLE IF NOT EXISTS fee_payments (
    payment_id     TEXT PRIMARY KEY,
    student_id     TEXT NOT NULL REFERENCES students(student_id),
    semester_term  TEXT NOT NULL,
    amount         FLOAT NOT NULL CHECK (amount > 0),
    payment_date   TIMESTAMPTZ DEFAULT NOW(),
    payment_method TEXT CHECK (payment_method IN ('cash','online','cheque','dd','upi')) DEFAULT 'online',
    notes          TEXT
);

-- 5. Department Fund Sources — incoming money
CREATE TABLE IF NOT EXISTS department_fund_sources (
    source_id     TEXT PRIMARY KEY,
    department_id TEXT NOT NULL REFERENCES departments(department_id),
    source_name   TEXT NOT NULL,
    amount        FLOAT NOT NULL CHECK (amount > 0),
    received_date TIMESTAMPTZ DEFAULT NOW(),
    description   TEXT
);

-- 6. Department Fund Usage — expenditures
CREATE TABLE IF NOT EXISTS department_fund_usage (
    usage_id      TEXT PRIMARY KEY,
    department_id TEXT NOT NULL REFERENCES departments(department_id),
    amount        FLOAT NOT NULL CHECK (amount > 0),
    usage_date    TIMESTAMPTZ DEFAULT NOW(),
    purpose       TEXT NOT NULL CHECK (purpose IN ('lab_equipment','maintenance','events','salaries','research','other')),
    description   TEXT
);

-- Done. Now run seed_all.js to populate data.
