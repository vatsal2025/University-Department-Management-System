// ============================================================
// UDIIMS Full Seed Script — populates ALL 10 departments
// Usage: node seed_all.js
// ============================================================

const URL  = 'https://ouphnfcovroazajlmvij.supabase.co/rest/v1';
const KEY  = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91cGhuZmNvdnJvYXphamxtdmlqIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3NDAxMjUwNSwiZXhwIjoyMDg5NTg4NTA1fQ.QB9RZwbp86Zz8HoyPpxKsL2Q-M_HUp_BwBIFSZ19xfM';

let errors = 0;

async function insert(table, rows, label) {
  const data = Array.isArray(rows) ? rows : [rows];
  if (data.length === 0) return;
  const res = await fetch(`${URL}/${table}`, {
    method: 'POST',
    headers: {
      'apikey': KEY,
      'Authorization': `Bearer ${KEY}`,
      'Content-Type': 'application/json',
      'Prefer': 'resolution=ignore-duplicates'
    },
    body: JSON.stringify(data)
  });
  if (res.status >= 400) {
    const err = await res.text();
    console.error(`  ✗ ${label || table}: ${err.substring(0, 120)}`);
    errors++;
  } else {
    console.log(`  ✓ ${label || table} — ${data.length} row(s)`);
  }
}

// ── Check if a column exists by trying to select it ──────────────────────────
async function columnExists(table, col) {
  const res = await fetch(`${URL}/${table}?select=${col}&limit=1`, {
    headers: { 'apikey': KEY, 'Authorization': `Bearer ${KEY}` }
  });
  return res.status < 400;
}

async function tableExists(table) {
  const res = await fetch(`${URL}/${table}?limit=1`, {
    headers: { 'apikey': KEY, 'Authorization': `Bearer ${KEY}` }
  });
  return res.status < 400;
}

// ─────────────────────────────────────────────────────────────────────────────
// DATA
// ─────────────────────────────────────────────────────────────────────────────

const DEPARTMENTS = [
  { department_id: 'IR',   department_name: 'International Relations', is_technical: false },
  { department_id: 'MECH', department_name: 'Mechanical Engineering',  is_technical: true  },
  { department_id: 'CHME', department_name: 'Chemical Engineering',    is_technical: true  },
  { department_id: 'EE',   department_name: 'Electrical Engineering',  is_technical: true  },
  { department_id: 'CHEM', department_name: 'Chemistry',               is_technical: true  },
  { department_id: 'RM',   department_name: 'Rural Management',        is_technical: false },
];

const FACULTY = [
  // PHY expansions
  { faculty_id: 'F028', faculty_name: 'Dr. Amjad Sharma',      designation: 'Professor',           department_name: 'Physics',                   department_id: 'PHY',  specialization: 'Astrophysics',            active_status: true },
  { faculty_id: 'F029', faculty_name: 'Prof. Rekha Nambiar',   designation: 'Associate Professor',  department_name: 'Physics',                   department_id: 'PHY',  specialization: 'Optics',                  active_status: true },
  // CSE expansion
  { faculty_id: 'F030', faculty_name: 'Dr. Vivek Oberoi',      designation: 'Assistant Professor',  department_name: 'Computer Science & Engineering', department_id: 'CSE', specialization: 'Computer Networks',     active_status: true },
  // ENG
  { faculty_id: 'F004', faculty_name: 'Dr. Naseeb Tiwari',     designation: 'Professor',           department_name: 'English',                   department_id: 'ENG',  specialization: 'Literature & Criticism',  active_status: true },
  { faculty_id: 'F005', faculty_name: 'Prof. Shabana Sharma',  designation: 'Associate Professor',  department_name: 'English',                   department_id: 'ENG',  specialization: 'Linguistics',             active_status: true },
  { faculty_id: 'F006', faculty_name: 'Dr. Konkona Roy',       designation: 'Assistant Professor',  department_name: 'English',                   department_id: 'ENG',  specialization: 'Creative Writing',        active_status: true },
  // MATH
  { faculty_id: 'F007', faculty_name: 'Dr. Amitabh Shrivastava', designation: 'Professor',         department_name: 'Mathematics',               department_id: 'MATH', specialization: 'Pure Mathematics',        active_status: true },
  { faculty_id: 'F008', faculty_name: 'Prof. Madhuri Pillai',  designation: 'Associate Professor',  department_name: 'Mathematics',               department_id: 'MATH', specialization: 'Applied Mathematics',     active_status: true },
  { faculty_id: 'F009', faculty_name: 'Dr. Irrfan Siddiqui',   designation: 'Assistant Professor',  department_name: 'Mathematics',               department_id: 'MATH', specialization: 'Statistics',              active_status: true },
  // IR
  { faculty_id: 'F010', faculty_name: 'Dr. Sanjay Mishra',     designation: 'Professor',           department_name: 'International Relations',    department_id: 'IR',   specialization: 'Geopolitics',             active_status: true },
  { faculty_id: 'F011', faculty_name: 'Prof. Vidya Krishnamurthy', designation: 'Associate Professor', department_name: 'International Relations', department_id: 'IR',  specialization: 'Diplomacy',               active_status: true },
  { faculty_id: 'F012', faculty_name: 'Dr. Tabu Nair',         designation: 'Assistant Professor',  department_name: 'International Relations',    department_id: 'IR',   specialization: 'International Law',       active_status: true },
  // MECH
  { faculty_id: 'F013', faculty_name: 'Dr. Hrithik Malhotra',  designation: 'Professor',           department_name: 'Mechanical Engineering',     department_id: 'MECH', specialization: 'Thermal Engineering',     active_status: true },
  { faculty_id: 'F014', faculty_name: 'Prof. Ajay Trivedi',    designation: 'Associate Professor',  department_name: 'Mechanical Engineering',     department_id: 'MECH', specialization: 'Manufacturing',           active_status: true },
  { faculty_id: 'F015', faculty_name: 'Dr. Sunny Yadav',       designation: 'Assistant Professor',  department_name: 'Mechanical Engineering',     department_id: 'MECH', specialization: 'Fluid Mechanics',         active_status: true },
  // CHME
  { faculty_id: 'F016', faculty_name: 'Dr. Ranbir Bose',       designation: 'Professor',           department_name: 'Chemical Engineering',       department_id: 'CHME', specialization: 'Process Engineering',     active_status: true },
  { faculty_id: 'F017', faculty_name: 'Prof. Deepika Krishnan',designation: 'Associate Professor',  department_name: 'Chemical Engineering',       department_id: 'CHME', specialization: 'Transport Phenomena',     active_status: true },
  { faculty_id: 'F018', faculty_name: 'Dr. Ranveer Menon',     designation: 'Assistant Professor',  department_name: 'Chemical Engineering',       department_id: 'CHME', specialization: 'Reaction Engineering',    active_status: true },
  // EE
  { faculty_id: 'F019', faculty_name: 'Dr. Akshay Tiwari',     designation: 'Professor',           department_name: 'Electrical Engineering',     department_id: 'EE',   specialization: 'Power Electronics',       active_status: true },
  { faculty_id: 'F020', faculty_name: 'Prof. Priyanka Agarwal',designation: 'Associate Professor',  department_name: 'Electrical Engineering',     department_id: 'EE',   specialization: 'Signal Processing',       active_status: true },
  { faculty_id: 'F021', faculty_name: 'Dr. Shahid Verma',      designation: 'Assistant Professor',  department_name: 'Electrical Engineering',     department_id: 'EE',   specialization: 'Control Systems',         active_status: true },
  // CHEM
  { faculty_id: 'F022', faculty_name: 'Dr. Kareena Patel',     designation: 'Professor',           department_name: 'Chemistry',                 department_id: 'CHEM', specialization: 'Organic Chemistry',       active_status: true },
  { faculty_id: 'F023', faculty_name: 'Prof. Shahid Rao',      designation: 'Associate Professor',  department_name: 'Chemistry',                 department_id: 'CHEM', specialization: 'Inorganic Chemistry',     active_status: true },
  { faculty_id: 'F024', faculty_name: 'Dr. Anushka Misra',     designation: 'Assistant Professor',  department_name: 'Chemistry',                 department_id: 'CHEM', specialization: 'Physical Chemistry',      active_status: true },
  // RM
  { faculty_id: 'F025', faculty_name: 'Dr. Nawazuddin Chauhan',designation: 'Professor',           department_name: 'Rural Management',           department_id: 'RM',   specialization: 'Rural Development',       active_status: true },
  { faculty_id: 'F026', faculty_name: 'Prof. Radhika Singh',   designation: 'Associate Professor',  department_name: 'Rural Management',           department_id: 'RM',   specialization: 'Agricultural Economics',  active_status: true },
  { faculty_id: 'F027', faculty_name: 'Dr. Pankaj Tripathi',   designation: 'Assistant Professor',  department_name: 'Rural Management',           department_id: 'RM',   specialization: 'Microfinance',            active_status: true },
];

const SECRETARIES = [
  { secretary_id: 'SEC003', secretary_name: 'Meenakshi Iyer',   department_id: 'ENG',  password: 'sec123' },
  { secretary_id: 'SEC004', secretary_name: 'Ramesh Tiwari',    department_id: 'MATH', password: 'sec123' },
  { secretary_id: 'SEC005', secretary_name: 'Sunita Kapoor',    department_id: 'IR',   password: 'sec123' },
  { secretary_id: 'SEC006', secretary_name: 'Rajesh Mehta',     department_id: 'MECH', password: 'sec123' },
  { secretary_id: 'SEC007', secretary_name: 'Priti Sharma',     department_id: 'CHME', password: 'sec123' },
  { secretary_id: 'SEC008', secretary_name: 'Suresh Nair',      department_id: 'EE',   password: 'sec123' },
  { secretary_id: 'SEC009', secretary_name: 'Kavita Pillai',    department_id: 'CHEM', password: 'sec123' },
  { secretary_id: 'SEC010', secretary_name: 'Dinesh Yadav',     department_id: 'RM',   password: 'sec123' },
];

const STUDENTS = [
  // CSE
  { student_id: 'S004', student_name: 'Neha Joshi',         program: 'B.Tech CSE',                     semester: 2, sgpa: 8.3, cgpa: 8.1, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'CSE' },
  { student_id: 'S005', student_name: 'Ayushmann Verma',    program: 'B.Tech CSE',                     semester: 3, sgpa: 7.9, cgpa: 7.7, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'CSE' },
  { student_id: 'S006', student_name: 'Yami Gupta',         program: 'B.Tech CSE',                     semester: 1, sgpa: 8.7, cgpa: 8.7, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'CSE' },
  // PHY
  { student_id: 'S007', student_name: 'Amitabh Rajan',      program: 'B.Sc Physics',                   semester: 2, sgpa: 8.0, cgpa: 7.9, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'PHY' },
  { student_id: 'S008', student_name: 'Madhuri Iyer',       program: 'B.Sc Physics',                   semester: 3, sgpa: 9.0, cgpa: 8.8, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'PHY' },
  { student_id: 'S009', student_name: 'Deepika Pillai',     program: 'B.Sc Physics',                   semester: 1, sgpa: 7.5, cgpa: 7.5, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'PHY' },
  // ENG
  { student_id: 'S010', student_name: 'Arjun Kapoor',       program: 'B.A. (Hons.) English',           semester: 2, sgpa: 7.8, cgpa: 7.6, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'ENG' },
  { student_id: 'S011', student_name: 'Riya Malhotra',      program: 'B.A. (Hons.) English',           semester: 3, sgpa: 8.5, cgpa: 8.3, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'ENG' },
  { student_id: 'S012', student_name: 'Kabir Sharma',       program: 'B.A. (Hons.) English',           semester: 4, sgpa: 7.2, cgpa: 7.0, backlog_count: 1, dashboard_access: false, password: 'pass123', department_id: 'ENG' },
  { student_id: 'S013', student_name: 'Ananya Singh',       program: 'B.A. (Hons.) English',           semester: 1, sgpa: 9.1, cgpa: 9.1, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'ENG' },
  // MATH
  { student_id: 'S014', student_name: 'Siddharth Roy',      program: 'B.Sc Mathematics',               semester: 2, sgpa: 8.8, cgpa: 8.6, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'MATH' },
  { student_id: 'S015', student_name: 'Pooja Trivedi',      program: 'B.Sc Mathematics',               semester: 3, sgpa: 7.6, cgpa: 7.5, backlog_count: 1, dashboard_access: false, password: 'pass123', department_id: 'MATH' },
  { student_id: 'S016', student_name: 'Varun Pandey',       program: 'B.Sc Mathematics',               semester: 4, sgpa: 9.2, cgpa: 9.0, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'MATH' },
  { student_id: 'S017', student_name: 'Shraddha Mishra',    program: 'B.Sc Mathematics',               semester: 1, sgpa: 8.0, cgpa: 8.0, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'MATH' },
  // IR
  { student_id: 'S018', student_name: 'Kartik Rao',         program: 'B.A. International Relations',   semester: 2, sgpa: 8.2, cgpa: 8.0, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'IR' },
  { student_id: 'S019', student_name: 'Kiara Nair',         program: 'B.A. International Relations',   semester: 3, sgpa: 8.7, cgpa: 8.5, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'IR' },
  { student_id: 'S020', student_name: 'Vicky Tiwari',       program: 'B.A. International Relations',   semester: 4, sgpa: 7.4, cgpa: 7.3, backlog_count: 1, dashboard_access: false, password: 'pass123', department_id: 'IR' },
  { student_id: 'S021', student_name: 'Sara Ansari',        program: 'B.A. International Relations',   semester: 1, sgpa: 8.9, cgpa: 8.9, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'IR' },
  // MECH
  { student_id: 'S022', student_name: 'Hrithik Verma',      program: 'B.Tech Mechanical Engineering',  semester: 2, sgpa: 7.8, cgpa: 7.7, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'MECH' },
  { student_id: 'S023', student_name: 'Rajkumar Yadav',     program: 'B.Tech Mechanical Engineering',  semester: 3, sgpa: 8.4, cgpa: 8.2, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'MECH' },
  { student_id: 'S024', student_name: 'Nawab Siddiqui',     program: 'B.Tech Mechanical Engineering',  semester: 4, sgpa: 7.0, cgpa: 6.9, backlog_count: 2, dashboard_access: false, password: 'pass123', department_id: 'MECH' },
  { student_id: 'S025', student_name: 'Taapsee Bose',       program: 'B.Tech Mechanical Engineering',  semester: 1, sgpa: 8.6, cgpa: 8.6, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'MECH' },
  // CHME
  { student_id: 'S026', student_name: 'Ranveer Singhania',  program: 'B.Tech Chemical Engineering',    semester: 2, sgpa: 8.1, cgpa: 7.9, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'CHME' },
  { student_id: 'S027', student_name: 'Deepa Patel',        program: 'B.Tech Chemical Engineering',    semester: 3, sgpa: 9.0, cgpa: 8.8, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'CHME' },
  { student_id: 'S028', student_name: 'Ranbir Saxena',      program: 'B.Tech Chemical Engineering',    semester: 4, sgpa: 7.3, cgpa: 7.1, backlog_count: 1, dashboard_access: false, password: 'pass123', department_id: 'CHME' },
  { student_id: 'S029', student_name: 'Alia Kapoor',        program: 'B.Tech Chemical Engineering',    semester: 1, sgpa: 8.8, cgpa: 8.8, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'CHME' },
  // EE
  { student_id: 'S030', student_name: 'Shahid Malhotra',    program: 'B.Tech Electrical Engineering',  semester: 2, sgpa: 7.9, cgpa: 7.8, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'EE' },
  { student_id: 'S031', student_name: 'Kareena Mehta',      program: 'B.Tech Electrical Engineering',  semester: 3, sgpa: 8.5, cgpa: 8.4, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'EE' },
  { student_id: 'S032', student_name: 'Vivek Sharma',       program: 'B.Tech Electrical Engineering',  semester: 4, sgpa: 7.1, cgpa: 7.0, backlog_count: 2, dashboard_access: false, password: 'pass123', department_id: 'EE' },
  { student_id: 'S033', student_name: 'Malaika Gupta',      program: 'B.Tech Electrical Engineering',  semester: 1, sgpa: 9.3, cgpa: 9.3, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'EE' },
  // CHEM
  { student_id: 'S034', student_name: 'Akshay Trivedi',     program: 'B.Sc Chemistry',                 semester: 2, sgpa: 8.4, cgpa: 8.2, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'CHEM' },
  { student_id: 'S035', student_name: 'Priyanka Soni',      program: 'B.Sc Chemistry',                 semester: 3, sgpa: 8.9, cgpa: 8.7, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'CHEM' },
  { student_id: 'S036', student_name: 'Katrina Iyer',       program: 'B.Sc Chemistry',                 semester: 4, sgpa: 7.5, cgpa: 7.4, backlog_count: 1, dashboard_access: false, password: 'pass123', department_id: 'CHEM' },
  { student_id: 'S037', student_name: 'Anushka Reddy',      program: 'B.Sc Chemistry',                 semester: 1, sgpa: 9.0, cgpa: 9.0, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'CHEM' },
  // RM
  { student_id: 'S038', student_name: 'Disha Rao',          program: 'B.Sc Rural Management',          semester: 2, sgpa: 8.0, cgpa: 7.8, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'RM' },
  { student_id: 'S039', student_name: 'Tiger Pandey',       program: 'B.Sc Rural Management',          semester: 3, sgpa: 7.7, cgpa: 7.6, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'RM' },
  { student_id: 'S040', student_name: 'Janhvi Singh',       program: 'B.Sc Rural Management',          semester: 4, sgpa: 8.3, cgpa: 8.1, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'RM' },
  { student_id: 'S041', student_name: 'Sanya Malhotra',     program: 'B.Sc Rural Management',          semester: 1, sgpa: 8.6, cgpa: 8.6, backlog_count: 0, dashboard_access: false, password: 'pass123', department_id: 'RM' },
];

// Courses — Sem-1-2025 and Sem-2-2025 (no faculty_id; column added after migration)
const COURSES = [
  // PHY
  { course_code: 'PH102', course_name: 'Electromagnetism',         credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'PHY' },
  { course_code: 'PH103', course_name: 'Optics & Waves',           credit_hours: 3, semester_term: 'Sem-1-2025', department_id: 'PHY' },
  { course_code: 'PH104', course_name: 'Statistical Mechanics',    credit_hours: 3, semester_term: 'Sem-1-2025', department_id: 'PHY' },
  { course_code: 'PH201', course_name: 'Quantum Mechanics',        credit_hours: 4, semester_term: 'Sem-2-2025', department_id: 'PHY' },
  { course_code: 'PH202', course_name: 'Nuclear Physics',          credit_hours: 3, semester_term: 'Sem-2-2025', department_id: 'PHY' },
  // ENG
  { course_code: 'EN101', course_name: 'British Literature',       credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'ENG' },
  { course_code: 'EN102', course_name: 'Grammar & Composition',    credit_hours: 3, semester_term: 'Sem-1-2025', department_id: 'ENG' },
  { course_code: 'EN201', course_name: 'American Literature',      credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'ENG' },
  { course_code: 'EN301', course_name: 'Creative Writing',         credit_hours: 3, semester_term: 'Sem-2-2025', department_id: 'ENG' },
  { course_code: 'EN302', course_name: 'Linguistics',              credit_hours: 4, semester_term: 'Sem-2-2025', department_id: 'ENG' },
  // MATH
  { course_code: 'MA101', course_name: 'Calculus',                 credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'MATH' },
  { course_code: 'MA102', course_name: 'Linear Algebra',           credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'MATH' },
  { course_code: 'MA201', course_name: 'Differential Equations',   credit_hours: 3, semester_term: 'Sem-1-2025', department_id: 'MATH' },
  { course_code: 'MA301', course_name: 'Real Analysis',            credit_hours: 4, semester_term: 'Sem-2-2025', department_id: 'MATH' },
  { course_code: 'MA302', course_name: 'Probability & Statistics', credit_hours: 3, semester_term: 'Sem-2-2025', department_id: 'MATH' },
  // IR
  { course_code: 'IR101', course_name: 'Introduction to IR',            credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'IR' },
  { course_code: 'IR102', course_name: 'Global Politics',               credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'IR' },
  { course_code: 'IR201', course_name: 'Diplomacy & Foreign Policy',    credit_hours: 3, semester_term: 'Sem-1-2025', department_id: 'IR' },
  { course_code: 'IR301', course_name: 'International Law',             credit_hours: 4, semester_term: 'Sem-2-2025', department_id: 'IR' },
  { course_code: 'IR302', course_name: 'Geopolitics & Strategy',        credit_hours: 3, semester_term: 'Sem-2-2025', department_id: 'IR' },
  // MECH
  { course_code: 'ME101', course_name: 'Engineering Mechanics',    credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'MECH' },
  { course_code: 'ME102', course_name: 'Thermodynamics',           credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'MECH' },
  { course_code: 'ME201', course_name: 'Fluid Mechanics',          credit_hours: 3, semester_term: 'Sem-1-2025', department_id: 'MECH' },
  { course_code: 'ME301', course_name: 'Machine Design',           credit_hours: 4, semester_term: 'Sem-2-2025', department_id: 'MECH' },
  { course_code: 'ME302', course_name: 'Manufacturing Processes',  credit_hours: 3, semester_term: 'Sem-2-2025', department_id: 'MECH' },
  // CHME
  { course_code: 'CH101', course_name: 'Process Calculations',     credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'CHME' },
  { course_code: 'CH102', course_name: 'Physical Chemistry',       credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'CHME' },
  { course_code: 'CH201', course_name: 'Heat & Mass Transfer',     credit_hours: 3, semester_term: 'Sem-1-2025', department_id: 'CHME' },
  { course_code: 'CH301', course_name: 'Reaction Engineering',     credit_hours: 4, semester_term: 'Sem-2-2025', department_id: 'CHME' },
  { course_code: 'CH302', course_name: 'Process Control',          credit_hours: 3, semester_term: 'Sem-2-2025', department_id: 'CHME' },
  // EE
  { course_code: 'EE101', course_name: 'Circuit Theory',           credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'EE' },
  { course_code: 'EE102', course_name: 'Signals & Systems',        credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'EE' },
  { course_code: 'EE201', course_name: 'Electrical Machines',      credit_hours: 3, semester_term: 'Sem-1-2025', department_id: 'EE' },
  { course_code: 'EE301', course_name: 'Power Systems',            credit_hours: 4, semester_term: 'Sem-2-2025', department_id: 'EE' },
  { course_code: 'EE302', course_name: 'Control Systems',          credit_hours: 3, semester_term: 'Sem-2-2025', department_id: 'EE' },
  // CHEM
  { course_code: 'CY101', course_name: 'Organic Chemistry',        credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'CHEM' },
  { course_code: 'CY102', course_name: 'Inorganic Chemistry',      credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'CHEM' },
  { course_code: 'CY201', course_name: 'Physical Chemistry',       credit_hours: 3, semester_term: 'Sem-1-2025', department_id: 'CHEM' },
  { course_code: 'CY301', course_name: 'Spectroscopy',             credit_hours: 4, semester_term: 'Sem-2-2025', department_id: 'CHEM' },
  { course_code: 'CY302', course_name: 'Biochemistry',             credit_hours: 3, semester_term: 'Sem-2-2025', department_id: 'CHEM' },
  // RM
  { course_code: 'RM101', course_name: 'Rural Development',        credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'RM' },
  { course_code: 'RM102', course_name: 'Agricultural Economics',   credit_hours: 4, semester_term: 'Sem-1-2025', department_id: 'RM' },
  { course_code: 'RM201', course_name: 'Village Administration',   credit_hours: 3, semester_term: 'Sem-1-2025', department_id: 'RM' },
  { course_code: 'RM301', course_name: 'NGO Management',           credit_hours: 4, semester_term: 'Sem-2-2025', department_id: 'RM' },
  { course_code: 'RM302', course_name: 'Microfinance & SHGs',      credit_hours: 3, semester_term: 'Sem-2-2025', department_id: 'RM' },
];

// Course registrations — completed (Sem-1-2024) + active (Sem-1-2025)
function reg(id, sid, code, name, term, credits, status, backlog, grade) {
  return { registration_id: id, student_id: sid, course_code: code, course_name: name,
           semester_term: term, credit_hours: credits, registration_status: status,
           backlog_flag: backlog, grade };
}
const REGISTRATIONS = [
  // ── PHY ──────────────────────────────────────────────────────
  reg('REG-S007-PH101-2024', 'S007', 'PH101', 'Mechanics',          'Sem-1-2024', 4, 'completed', false, 'B'),
  reg('REG-S007-PH102-2024', 'S007', 'PH102', 'Electromagnetism',   'Sem-1-2024', 4, 'completed', false, 'A-'),
  reg('REG-S007-PH102-2025', 'S007', 'PH103', 'Optics & Waves',     'Sem-1-2025', 3, 'active',    false, null),
  reg('REG-S008-PH101-2024', 'S008', 'PH101', 'Mechanics',          'Sem-1-2024', 4, 'completed', false, 'A'),
  reg('REG-S008-PH102-2024', 'S008', 'PH102', 'Electromagnetism',   'Sem-1-2024', 4, 'completed', false, 'A'),
  reg('REG-S008-PH103-2024', 'S008', 'PH103', 'Optics & Waves',     'Sem-1-2024', 3, 'completed', false, 'A-'),
  reg('REG-S008-PH104-2025', 'S008', 'PH104', 'Statistical Mechanics','Sem-1-2025', 3, 'active',  false, null),
  reg('REG-S009-PH101-2025', 'S009', 'PH101', 'Mechanics',          'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S009-PH102-2025', 'S009', 'PH102', 'Electromagnetism',   'Sem-1-2025', 4, 'active',    false, null),
  // ── ENG ──────────────────────────────────────────────────────
  reg('REG-S010-EN101-2024', 'S010', 'EN101', 'British Literature',    'Sem-1-2024', 4, 'completed', false, 'B'),
  reg('REG-S010-EN102-2024', 'S010', 'EN102', 'Grammar & Composition', 'Sem-1-2024', 3, 'completed', false, 'B+'),
  reg('REG-S010-EN201-2025', 'S010', 'EN201', 'American Literature',   'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S011-EN101-2024', 'S011', 'EN101', 'British Literature',    'Sem-1-2024', 4, 'completed', false, 'A'),
  reg('REG-S011-EN102-2024', 'S011', 'EN102', 'Grammar & Composition', 'Sem-1-2024', 3, 'completed', false, 'A-'),
  reg('REG-S011-EN201-2024', 'S011', 'EN201', 'American Literature',   'Sem-1-2024', 4, 'completed', false, 'B+'),
  reg('REG-S011-EN301-2025', 'S011', 'EN301', 'Creative Writing',      'Sem-1-2025', 3, 'active',    false, null),
  reg('REG-S012-EN101-2024', 'S012', 'EN101', 'British Literature',    'Sem-1-2024', 4, 'completed', false, 'C'),
  reg('REG-S012-EN102-2024', 'S012', 'EN102', 'Grammar & Composition', 'Sem-1-2024', 3, 'completed', true,  'F'),
  reg('REG-S012-EN102-2025', 'S012', 'EN102', 'Grammar & Composition', 'Sem-1-2025', 3, 'active',    true,  null),
  reg('REG-S013-EN101-2025', 'S013', 'EN101', 'British Literature',    'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S013-EN102-2025', 'S013', 'EN102', 'Grammar & Composition', 'Sem-1-2025', 3, 'active',    false, null),
  // ── MATH ─────────────────────────────────────────────────────
  reg('REG-S014-MA101-2024', 'S014', 'MA101', 'Calculus',             'Sem-1-2024', 4, 'completed', false, 'A'),
  reg('REG-S014-MA102-2024', 'S014', 'MA102', 'Linear Algebra',       'Sem-1-2024', 4, 'completed', false, 'A-'),
  reg('REG-S014-MA201-2025', 'S014', 'MA201', 'Differential Equations','Sem-1-2025', 3, 'active',   false, null),
  reg('REG-S015-MA101-2024', 'S015', 'MA101', 'Calculus',             'Sem-1-2024', 4, 'completed', false, 'B'),
  reg('REG-S015-MA102-2024', 'S015', 'MA102', 'Linear Algebra',       'Sem-1-2024', 4, 'completed', true,  'F'),
  reg('REG-S015-MA102-2025', 'S015', 'MA102', 'Linear Algebra',       'Sem-1-2025', 4, 'active',    true,  null),
  reg('REG-S016-MA101-2024', 'S016', 'MA101', 'Calculus',             'Sem-1-2024', 4, 'completed', false, 'A'),
  reg('REG-S016-MA102-2024', 'S016', 'MA102', 'Linear Algebra',       'Sem-1-2024', 4, 'completed', false, 'A'),
  reg('REG-S016-MA201-2024', 'S016', 'MA201', 'Differential Equations','Sem-1-2024', 3, 'completed', false, 'A-'),
  reg('REG-S016-MA301-2025', 'S016', 'MA301', 'Real Analysis',        'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S017-MA101-2025', 'S017', 'MA101', 'Calculus',             'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S017-MA102-2025', 'S017', 'MA102', 'Linear Algebra',       'Sem-1-2025', 4, 'active',    false, null),
  // ── IR ───────────────────────────────────────────────────────
  reg('REG-S018-IR101-2024', 'S018', 'IR101', 'Introduction to IR',     'Sem-1-2024', 4, 'completed', false, 'B+'),
  reg('REG-S018-IR102-2024', 'S018', 'IR102', 'Global Politics',        'Sem-1-2024', 4, 'completed', false, 'B'),
  reg('REG-S018-IR201-2025', 'S018', 'IR201', 'Diplomacy & Foreign Policy','Sem-1-2025', 3, 'active', false, null),
  reg('REG-S019-IR101-2024', 'S019', 'IR101', 'Introduction to IR',     'Sem-1-2024', 4, 'completed', false, 'A'),
  reg('REG-S019-IR102-2024', 'S019', 'IR102', 'Global Politics',        'Sem-1-2024', 4, 'completed', false, 'A-'),
  reg('REG-S019-IR201-2024', 'S019', 'IR201', 'Diplomacy & Foreign Policy','Sem-1-2024', 3, 'completed', false, 'B+'),
  reg('REG-S019-IR301-2025', 'S019', 'IR301', 'International Law',      'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S020-IR101-2024', 'S020', 'IR101', 'Introduction to IR',     'Sem-1-2024', 4, 'completed', false, 'C'),
  reg('REG-S020-IR102-2024', 'S020', 'IR102', 'Global Politics',        'Sem-1-2024', 4, 'completed', true,  'F'),
  reg('REG-S020-IR102-2025', 'S020', 'IR102', 'Global Politics',        'Sem-1-2025', 4, 'active',    true,  null),
  reg('REG-S021-IR101-2025', 'S021', 'IR101', 'Introduction to IR',     'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S021-IR102-2025', 'S021', 'IR102', 'Global Politics',        'Sem-1-2025', 4, 'active',    false, null),
  // ── MECH ─────────────────────────────────────────────────────
  reg('REG-S022-ME101-2024', 'S022', 'ME101', 'Engineering Mechanics', 'Sem-1-2024', 4, 'completed', false, 'B'),
  reg('REG-S022-ME102-2024', 'S022', 'ME102', 'Thermodynamics',        'Sem-1-2024', 4, 'completed', false, 'B-'),
  reg('REG-S022-ME201-2025', 'S022', 'ME201', 'Fluid Mechanics',       'Sem-1-2025', 3, 'active',    false, null),
  reg('REG-S023-ME101-2024', 'S023', 'ME101', 'Engineering Mechanics', 'Sem-1-2024', 4, 'completed', false, 'A-'),
  reg('REG-S023-ME102-2024', 'S023', 'ME102', 'Thermodynamics',        'Sem-1-2024', 4, 'completed', false, 'B+'),
  reg('REG-S023-ME201-2024', 'S023', 'ME201', 'Fluid Mechanics',       'Sem-1-2024', 3, 'completed', false, 'B'),
  reg('REG-S023-ME301-2025', 'S023', 'ME301', 'Machine Design',        'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S024-ME101-2024', 'S024', 'ME101', 'Engineering Mechanics', 'Sem-1-2024', 4, 'completed', false, 'C'),
  reg('REG-S024-ME102-2024', 'S024', 'ME102', 'Thermodynamics',        'Sem-1-2024', 4, 'completed', true,  'F'),
  reg('REG-S024-ME201-2024', 'S024', 'ME201', 'Fluid Mechanics',       'Sem-1-2024', 3, 'completed', true,  'F'),
  reg('REG-S024-ME102-2025', 'S024', 'ME102', 'Thermodynamics',        'Sem-1-2025', 4, 'active',    true,  null),
  reg('REG-S024-ME201-2025', 'S024', 'ME201', 'Fluid Mechanics',       'Sem-1-2025', 3, 'active',    true,  null),
  reg('REG-S025-ME101-2025', 'S025', 'ME101', 'Engineering Mechanics', 'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S025-ME102-2025', 'S025', 'ME102', 'Thermodynamics',        'Sem-1-2025', 4, 'active',    false, null),
  // ── CHME ─────────────────────────────────────────────────────
  reg('REG-S026-CH101-2024', 'S026', 'CH101', 'Process Calculations',  'Sem-1-2024', 4, 'completed', false, 'B+'),
  reg('REG-S026-CH102-2024', 'S026', 'CH102', 'Physical Chemistry',    'Sem-1-2024', 4, 'completed', false, 'B'),
  reg('REG-S026-CH201-2025', 'S026', 'CH201', 'Heat & Mass Transfer',  'Sem-1-2025', 3, 'active',    false, null),
  reg('REG-S027-CH101-2024', 'S027', 'CH101', 'Process Calculations',  'Sem-1-2024', 4, 'completed', false, 'A'),
  reg('REG-S027-CH102-2024', 'S027', 'CH102', 'Physical Chemistry',    'Sem-1-2024', 4, 'completed', false, 'A'),
  reg('REG-S027-CH201-2024', 'S027', 'CH201', 'Heat & Mass Transfer',  'Sem-1-2024', 3, 'completed', false, 'A-'),
  reg('REG-S027-CH301-2025', 'S027', 'CH301', 'Reaction Engineering',  'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S028-CH101-2024', 'S028', 'CH101', 'Process Calculations',  'Sem-1-2024', 4, 'completed', false, 'C'),
  reg('REG-S028-CH102-2024', 'S028', 'CH102', 'Physical Chemistry',    'Sem-1-2024', 4, 'completed', true,  'F'),
  reg('REG-S028-CH102-2025', 'S028', 'CH102', 'Physical Chemistry',    'Sem-1-2025', 4, 'active',    true,  null),
  reg('REG-S029-CH101-2025', 'S029', 'CH101', 'Process Calculations',  'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S029-CH102-2025', 'S029', 'CH102', 'Physical Chemistry',    'Sem-1-2025', 4, 'active',    false, null),
  // ── EE ───────────────────────────────────────────────────────
  reg('REG-S030-EE101-2024', 'S030', 'EE101', 'Circuit Theory',        'Sem-1-2024', 4, 'completed', false, 'B'),
  reg('REG-S030-EE102-2024', 'S030', 'EE102', 'Signals & Systems',     'Sem-1-2024', 4, 'completed', false, 'B-'),
  reg('REG-S030-EE201-2025', 'S030', 'EE201', 'Electrical Machines',   'Sem-1-2025', 3, 'active',    false, null),
  reg('REG-S031-EE101-2024', 'S031', 'EE101', 'Circuit Theory',        'Sem-1-2024', 4, 'completed', false, 'A-'),
  reg('REG-S031-EE102-2024', 'S031', 'EE102', 'Signals & Systems',     'Sem-1-2024', 4, 'completed', false, 'B+'),
  reg('REG-S031-EE201-2024', 'S031', 'EE201', 'Electrical Machines',   'Sem-1-2024', 3, 'completed', false, 'A'),
  reg('REG-S031-EE301-2025', 'S031', 'EE301', 'Power Systems',         'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S032-EE101-2024', 'S032', 'EE101', 'Circuit Theory',        'Sem-1-2024', 4, 'completed', false, 'C'),
  reg('REG-S032-EE102-2024', 'S032', 'EE102', 'Signals & Systems',     'Sem-1-2024', 4, 'completed', true,  'F'),
  reg('REG-S032-EE201-2024', 'S032', 'EE201', 'Electrical Machines',   'Sem-1-2024', 3, 'completed', true,  'F'),
  reg('REG-S032-EE102-2025', 'S032', 'EE102', 'Signals & Systems',     'Sem-1-2025', 4, 'active',    true,  null),
  reg('REG-S033-EE101-2025', 'S033', 'EE101', 'Circuit Theory',        'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S033-EE102-2025', 'S033', 'EE102', 'Signals & Systems',     'Sem-1-2025', 4, 'active',    false, null),
  // ── CHEM ─────────────────────────────────────────────────────
  reg('REG-S034-CY101-2024', 'S034', 'CY101', 'Organic Chemistry',     'Sem-1-2024', 4, 'completed', false, 'A-'),
  reg('REG-S034-CY102-2024', 'S034', 'CY102', 'Inorganic Chemistry',   'Sem-1-2024', 4, 'completed', false, 'B+'),
  reg('REG-S034-CY201-2025', 'S034', 'CY201', 'Physical Chemistry',    'Sem-1-2025', 3, 'active',    false, null),
  reg('REG-S035-CY101-2024', 'S035', 'CY101', 'Organic Chemistry',     'Sem-1-2024', 4, 'completed', false, 'A'),
  reg('REG-S035-CY102-2024', 'S035', 'CY102', 'Inorganic Chemistry',   'Sem-1-2024', 4, 'completed', false, 'A'),
  reg('REG-S035-CY201-2024', 'S035', 'CY201', 'Physical Chemistry',    'Sem-1-2024', 3, 'completed', false, 'A-'),
  reg('REG-S035-CY301-2025', 'S035', 'CY301', 'Spectroscopy',          'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S036-CY101-2024', 'S036', 'CY101', 'Organic Chemistry',     'Sem-1-2024', 4, 'completed', false, 'B'),
  reg('REG-S036-CY102-2024', 'S036', 'CY102', 'Inorganic Chemistry',   'Sem-1-2024', 4, 'completed', true,  'F'),
  reg('REG-S036-CY102-2025', 'S036', 'CY102', 'Inorganic Chemistry',   'Sem-1-2025', 4, 'active',    true,  null),
  reg('REG-S037-CY101-2025', 'S037', 'CY101', 'Organic Chemistry',     'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S037-CY102-2025', 'S037', 'CY102', 'Inorganic Chemistry',   'Sem-1-2025', 4, 'active',    false, null),
  // ── RM ───────────────────────────────────────────────────────
  reg('REG-S038-RM101-2024', 'S038', 'RM101', 'Rural Development',     'Sem-1-2024', 4, 'completed', false, 'B+'),
  reg('REG-S038-RM102-2024', 'S038', 'RM102', 'Agricultural Economics','Sem-1-2024', 4, 'completed', false, 'B'),
  reg('REG-S038-RM201-2025', 'S038', 'RM201', 'Village Administration','Sem-1-2025', 3, 'active',    false, null),
  reg('REG-S039-RM101-2024', 'S039', 'RM101', 'Rural Development',     'Sem-1-2024', 4, 'completed', false, 'B'),
  reg('REG-S039-RM102-2024', 'S039', 'RM102', 'Agricultural Economics','Sem-1-2024', 4, 'completed', false, 'B-'),
  reg('REG-S039-RM201-2024', 'S039', 'RM201', 'Village Administration','Sem-1-2024', 3, 'completed', false, 'B+'),
  reg('REG-S039-RM301-2025', 'S039', 'RM301', 'NGO Management',        'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S040-RM101-2024', 'S040', 'RM101', 'Rural Development',     'Sem-1-2024', 4, 'completed', false, 'A'),
  reg('REG-S040-RM102-2024', 'S040', 'RM102', 'Agricultural Economics','Sem-1-2024', 4, 'completed', false, 'A-'),
  reg('REG-S040-RM201-2024', 'S040', 'RM201', 'Village Administration','Sem-1-2024', 3, 'completed', false, 'B+'),
  reg('REG-S040-RM301-2025', 'S040', 'RM301', 'NGO Management',        'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S040-RM302-2025', 'S040', 'RM302', 'Microfinance & SHGs',   'Sem-1-2025', 3, 'active',    false, null),
  reg('REG-S041-RM101-2025', 'S041', 'RM101', 'Rural Development',     'Sem-1-2025', 4, 'active',    false, null),
  reg('REG-S041-RM102-2025', 'S041', 'RM102', 'Agricultural Economics','Sem-1-2025', 4, 'active',    false, null),
];

// Fee for technical depts: 55000, semi-tech (PHY, CHEM): 45000, non-tech: 30000
function feeRec(sid, dept, sem, amount, status) {
  return {
    record_id: `FEE-${sid}-${sem.replace(/-/g,'')}-001`,
    record_type: 'student-fee',
    amount,
    transaction_date: '2025-01-01T00:00:00Z',
    department_id: dept,
    student_id: sid,
    semester_term: sem,
    fee_status: status,
    fee_updated_timestamp: '2025-01-15T00:00:00Z'
  };
}
const FINANCIAL_RECORDS = [
  // Fees for new students — Sem-1-2025
  feeRec('S004', 'CSE',  'Sem-1-2025', 50000, 'paid'),
  feeRec('S005', 'CSE',  'Sem-1-2025', 50000, 'partial'),
  feeRec('S006', 'CSE',  'Sem-1-2025', 50000, 'pending'),
  feeRec('S007', 'PHY',  'Sem-1-2025', 45000, 'paid'),
  feeRec('S008', 'PHY',  'Sem-1-2025', 45000, 'paid'),
  feeRec('S009', 'PHY',  'Sem-1-2025', 45000, 'pending'),
  feeRec('S010', 'ENG',  'Sem-1-2025', 30000, 'paid'),
  feeRec('S011', 'ENG',  'Sem-1-2025', 30000, 'paid'),
  feeRec('S012', 'ENG',  'Sem-1-2025', 30000, 'partial'),
  feeRec('S013', 'ENG',  'Sem-1-2025', 30000, 'pending'),
  feeRec('S014', 'MATH', 'Sem-1-2025', 32000, 'paid'),
  feeRec('S015', 'MATH', 'Sem-1-2025', 32000, 'partial'),
  feeRec('S016', 'MATH', 'Sem-1-2025', 32000, 'paid'),
  feeRec('S017', 'MATH', 'Sem-1-2025', 32000, 'pending'),
  feeRec('S018', 'IR',   'Sem-1-2025', 30000, 'paid'),
  feeRec('S019', 'IR',   'Sem-1-2025', 30000, 'paid'),
  feeRec('S020', 'IR',   'Sem-1-2025', 30000, 'partial'),
  feeRec('S021', 'IR',   'Sem-1-2025', 30000, 'pending'),
  feeRec('S022', 'MECH', 'Sem-1-2025', 55000, 'partial'),
  feeRec('S023', 'MECH', 'Sem-1-2025', 55000, 'paid'),
  feeRec('S024', 'MECH', 'Sem-1-2025', 55000, 'pending'),
  feeRec('S025', 'MECH', 'Sem-1-2025', 55000, 'pending'),
  feeRec('S026', 'CHME', 'Sem-1-2025', 55000, 'paid'),
  feeRec('S027', 'CHME', 'Sem-1-2025', 55000, 'paid'),
  feeRec('S028', 'CHME', 'Sem-1-2025', 55000, 'partial'),
  feeRec('S029', 'CHME', 'Sem-1-2025', 55000, 'pending'),
  feeRec('S030', 'EE',   'Sem-1-2025', 55000, 'partial'),
  feeRec('S031', 'EE',   'Sem-1-2025', 55000, 'paid'),
  feeRec('S032', 'EE',   'Sem-1-2025', 55000, 'pending'),
  feeRec('S033', 'EE',   'Sem-1-2025', 55000, 'paid'),
  feeRec('S034', 'CHEM', 'Sem-1-2025', 42000, 'paid'),
  feeRec('S035', 'CHEM', 'Sem-1-2025', 42000, 'paid'),
  feeRec('S036', 'CHEM', 'Sem-1-2025', 42000, 'partial'),
  feeRec('S037', 'CHEM', 'Sem-1-2025', 42000, 'pending'),
  feeRec('S038', 'RM',   'Sem-1-2025', 28000, 'paid'),
  feeRec('S039', 'RM',   'Sem-1-2025', 28000, 'partial'),
  feeRec('S040', 'RM',   'Sem-1-2025', 28000, 'paid'),
  feeRec('S041', 'RM',   'Sem-1-2025', 28000, 'pending'),
  // Grants per new dept
  { record_id: 'GR-GRANT-PHY-2025', record_type: 'grant', amount: 400000, transaction_date: '2025-01-20T00:00:00Z', department_id: 'PHY', grant_id: 'GRANT-PHY-2025' },
  { record_id: 'GR-GRANT-ENG-2025', record_type: 'grant', amount: 150000, transaction_date: '2025-01-22T00:00:00Z', department_id: 'ENG', grant_id: 'GRANT-ENG-2025' },
  { record_id: 'GR-GRANT-MATH-2025',record_type: 'grant', amount: 180000, transaction_date: '2025-01-25T00:00:00Z', department_id: 'MATH',grant_id: 'GRANT-MATH-2025' },
  { record_id: 'GR-GRANT-IR-2025',  record_type: 'grant', amount: 120000, transaction_date: '2025-01-28T00:00:00Z', department_id: 'IR',  grant_id: 'GRANT-IR-2025' },
  { record_id: 'GR-GRANT-MECH-2025',record_type: 'grant', amount: 600000, transaction_date: '2025-02-01T00:00:00Z', department_id: 'MECH',grant_id: 'GRANT-MECH-2025' },
  { record_id: 'GR-GRANT-CHME-2025',record_type: 'grant', amount: 550000, transaction_date: '2025-02-05T00:00:00Z', department_id: 'CHME',grant_id: 'GRANT-CHME-2025' },
  { record_id: 'GR-GRANT-EE-2025',  record_type: 'grant', amount: 700000, transaction_date: '2025-02-08T00:00:00Z', department_id: 'EE',  grant_id: 'GRANT-EE-2025' },
  { record_id: 'GR-GRANT-CHEM-2025',record_type: 'grant', amount: 300000, transaction_date: '2025-02-10T00:00:00Z', department_id: 'CHEM',grant_id: 'GRANT-CHEM-2025' },
  { record_id: 'GR-GRANT-RM-2025',  record_type: 'grant', amount: 100000, transaction_date: '2025-02-12T00:00:00Z', department_id: 'RM',  grant_id: 'GRANT-RM-2025' },
  // Consultancy per new dept
  { record_id: 'CON-PHY-CON001',  record_type: 'consultancy', amount: 60000,  transaction_date: '2025-03-01T00:00:00Z', department_id: 'PHY',  consultancy_id: 'PHY-CON001',  description: 'Lab testing services for ISRO' },
  { record_id: 'CON-ENG-CON001',  record_type: 'consultancy', amount: 25000,  transaction_date: '2025-03-05T00:00:00Z', department_id: 'ENG',  consultancy_id: 'ENG-CON001',  description: 'Content writing for state textbooks' },
  { record_id: 'CON-MATH-CON001', record_type: 'consultancy', amount: 40000,  transaction_date: '2025-03-08T00:00:00Z', department_id: 'MATH', consultancy_id: 'MATH-CON001', description: 'Data analytics for state agriculture dept' },
  { record_id: 'CON-IR-CON001',   record_type: 'consultancy', amount: 30000,  transaction_date: '2025-03-10T00:00:00Z', department_id: 'IR',   consultancy_id: 'IR-CON001',   description: 'Policy research for MEA' },
  { record_id: 'CON-MECH-CON001', record_type: 'consultancy', amount: 120000, transaction_date: '2025-03-12T00:00:00Z', department_id: 'MECH', consultancy_id: 'MECH-CON001', description: 'Design consultation for Tata Steel' },
  { record_id: 'CON-CHME-CON001', record_type: 'consultancy', amount: 90000,  transaction_date: '2025-03-15T00:00:00Z', department_id: 'CHME', consultancy_id: 'CHME-CON001', description: 'Process audit for Reliance Industries' },
  { record_id: 'CON-EE-CON001',   record_type: 'consultancy', amount: 150000, transaction_date: '2025-03-18T00:00:00Z', department_id: 'EE',   consultancy_id: 'EE-CON001',   description: 'Grid modernisation study for NTPC' },
  { record_id: 'CON-CHEM-CON001', record_type: 'consultancy', amount: 50000,  transaction_date: '2025-03-20T00:00:00Z', department_id: 'CHEM', consultancy_id: 'CHEM-CON001', description: 'Water testing lab services for municipality' },
  { record_id: 'CON-RM-CON001',   record_type: 'consultancy', amount: 20000,  transaction_date: '2025-03-22T00:00:00Z', department_id: 'RM',   consultancy_id: 'RM-CON001',   description: 'SHG capacity building for NABARD' },
];

const PROJECTS = [
  { project_id: 'PRJ002', project_title: 'AI Chatbot for Student Counselling',   faculty_id: 'F001', project_budget: 200000, project_status: 'active', department_id: 'CSE',  abstract: 'NLP-based chatbot for student mental health support.', publication_link: '' },
  { project_id: 'PRJ003', project_title: 'Digital Archive of Indian Literature',  faculty_id: 'F004', project_budget:  80000, project_status: 'active', department_id: 'ENG',  abstract: 'Digitising rare regional language manuscripts.', publication_link: '' },
  { project_id: 'PRJ004', project_title: 'Mathematical Modelling of Crop Yield',  faculty_id: 'F007', project_budget: 120000, project_status: 'active', department_id: 'MATH', abstract: 'Differential equation-based crop productivity model.', publication_link: '' },
  { project_id: 'PRJ005', project_title: 'India-ASEAN Diplomatic Relations Study',faculty_id: 'F010', project_budget:  70000, project_status: 'active', department_id: 'IR',   abstract: 'Analysing bilateral trade and diplomatic trends.', publication_link: '' },
  { project_id: 'PRJ006', project_title: 'IoT-enabled Smart Manufacturing Cell',  faculty_id: 'F013', project_budget: 400000, project_status: 'active', department_id: 'MECH', abstract: 'Automated production monitoring using sensor networks.', publication_link: '' },
  { project_id: 'PRJ007', project_title: 'Carbon Capture Process Optimisation',   faculty_id: 'F016', project_budget: 350000, project_status: 'active', department_id: 'CHME', abstract: 'Solvent-based CO2 capture for industrial plants.', publication_link: '' },
  { project_id: 'PRJ008', project_title: 'Smart Grid Energy Management System',   faculty_id: 'F019', project_budget: 500000, project_status: 'active', department_id: 'EE',   abstract: 'AI-based load forecasting and grid optimisation.', publication_link: '' },
  { project_id: 'PRJ009', project_title: 'Green Synthesis of Nanoparticles',       faculty_id: 'F022', project_budget: 180000, project_status: 'active', department_id: 'CHEM', abstract: 'Eco-friendly synthesis of silver nanoparticles for antimicrobial use.', publication_link: '' },
  { project_id: 'PRJ010', project_title: 'Rural Micro-enterprise Development',     faculty_id: 'F025', project_budget: 150000, project_status: 'active', department_id: 'RM',   abstract: 'Empowering SHGs through technology-enabled microfinance.', publication_link: '' },
  { project_id: 'PRJ011', project_title: 'Gravitational Wave Signal Analysis',     faculty_id: 'F028', project_budget: 250000, project_status: 'active', department_id: 'PHY',  abstract: 'ML-based signal processing for LIGO data streams.', publication_link: '' },
  { project_id: 'PRJ012', project_title: 'English Language Lab Development',       faculty_id: 'F005', project_budget:  60000, project_status: 'active', department_id: 'ENG',  abstract: 'Building interactive spoken English curriculum.', publication_link: '' },
  { project_id: 'PRJ013', project_title: 'Renewable Energy Grid Simulation',       faculty_id: 'F020', project_budget: 280000, project_status: 'active', department_id: 'EE',   abstract: 'Simulation of solar-wind hybrid microgrid.', publication_link: '' },
];

// Inventory — 3-4 items per dept (lab items for technical depts, no lab_incharge_id since column not yet in DB)
const INVENTORY = [
  // PHY
  { item_id: 'INV-PHY-001', item_name: 'Spectrometer',        category: 'Optical Instrument', quantity: 4,  location: 'Optics Lab',         condition: 'good', assigned_department: 'PHY', is_lab_item: true  },
  { item_id: 'INV-PHY-002', item_name: 'Laser Source',         category: 'Electronics',        quantity: 3,  location: 'Optics Lab',         condition: 'good', assigned_department: 'PHY', is_lab_item: true  },
  { item_id: 'INV-PHY-003', item_name: 'Reference Books',      category: 'Books',              quantity: 50, location: 'PHY Library',        condition: 'good', assigned_department: 'PHY', is_lab_item: false },
  { item_id: 'INV-PHY-004', item_name: 'Digital Oscilloscope', category: 'Electronics',        quantity: 6,  location: 'Physics Lab',        condition: 'good', assigned_department: 'PHY', is_lab_item: true  },
  // ENG
  { item_id: 'INV-ENG-001', item_name: 'Desks & Chairs (set)', category: 'Furniture',          quantity: 60, location: 'Classrooms',         condition: 'good', assigned_department: 'ENG', is_lab_item: false },
  { item_id: 'INV-ENG-002', item_name: 'Audio Recorder',       category: 'Electronics',        quantity: 5,  location: 'Language Lab',       condition: 'good', assigned_department: 'ENG', is_lab_item: false },
  { item_id: 'INV-ENG-003', item_name: 'Smart TV',             category: 'Electronics',        quantity: 3,  location: 'Seminar Room',       condition: 'good', assigned_department: 'ENG', is_lab_item: false },
  // MATH
  { item_id: 'INV-MATH-001', item_name: 'Graph Plotters',      category: 'Computer',           quantity: 8,  location: 'Math Computing Lab', condition: 'good', assigned_department: 'MATH',is_lab_item: false },
  { item_id: 'INV-MATH-002', item_name: 'MATLAB Licenses',     category: 'Software',           quantity: 20, location: 'Math Computing Lab', condition: 'good', assigned_department: 'MATH',is_lab_item: false },
  { item_id: 'INV-MATH-003', item_name: 'Reference Books',     category: 'Books',              quantity: 80, location: 'MATH Library',       condition: 'good', assigned_department: 'MATH',is_lab_item: false },
  // IR
  { item_id: 'INV-IR-001', item_name: 'Conference Table',      category: 'Furniture',          quantity: 2,  location: 'Seminar Hall',       condition: 'good', assigned_department: 'IR',  is_lab_item: false },
  { item_id: 'INV-IR-002', item_name: 'Projector Screen',      category: 'Electronics',        quantity: 3,  location: 'Classrooms',         condition: 'good', assigned_department: 'IR',  is_lab_item: false },
  { item_id: 'INV-IR-003', item_name: 'Globe & Atlas Set',     category: 'Stationery',         quantity: 10, location: 'Classrooms',         condition: 'good', assigned_department: 'IR',  is_lab_item: false },
  // MECH
  { item_id: 'INV-MECH-001', item_name: 'CNC Milling Machine',  category: 'Machinery',          quantity: 2, location: 'Workshop Lab',       condition: 'good', assigned_department: 'MECH',is_lab_item: true  },
  { item_id: 'INV-MECH-002', item_name: '3D Printer',           category: 'Electronics',        quantity: 3, location: 'Design Lab',         condition: 'good', assigned_department: 'MECH',is_lab_item: true  },
  { item_id: 'INV-MECH-003', item_name: 'Universal Testing Machine', category: 'Machinery',    quantity: 1, location: 'Materials Lab',      condition: 'good', assigned_department: 'MECH',is_lab_item: true  },
  { item_id: 'INV-MECH-004', item_name: 'Engineering Manuals',  category: 'Books',              quantity: 40,location: 'MECH Library',       condition: 'good', assigned_department: 'MECH',is_lab_item: false },
  // CHME
  { item_id: 'INV-CHME-001', item_name: 'Distillation Column',  category: 'Lab Equipment',      quantity: 2, location: 'Process Lab',        condition: 'good', assigned_department: 'CHME',is_lab_item: true  },
  { item_id: 'INV-CHME-002', item_name: 'Heat Exchanger Unit',  category: 'Lab Equipment',      quantity: 2, location: 'Heat Transfer Lab',  condition: 'good', assigned_department: 'CHME',is_lab_item: true  },
  { item_id: 'INV-CHME-003', item_name: 'Gas Chromatograph',    category: 'Lab Equipment',      quantity: 1, location: 'Analytical Lab',     condition: 'good', assigned_department: 'CHME',is_lab_item: true  },
  { item_id: 'INV-CHME-004', item_name: 'Safety Equipment Set', category: 'Safety',             quantity: 30,location: 'Process Lab',        condition: 'new',  assigned_department: 'CHME',is_lab_item: false },
  // EE
  { item_id: 'INV-EE-001', item_name: 'Power Electronics Kit',  category: 'Lab Equipment',      quantity: 10,location: 'Power Lab',          condition: 'good', assigned_department: 'EE',  is_lab_item: true  },
  { item_id: 'INV-EE-002', item_name: 'Digital Signal Processor',category: 'Electronics',       quantity: 8, location: 'Signal Processing Lab',condition: 'good',assigned_department: 'EE', is_lab_item: true  },
  { item_id: 'INV-EE-003', item_name: 'High-voltage Trainer',   category: 'Lab Equipment',      quantity: 2, location: 'Power Lab',          condition: 'good', assigned_department: 'EE',  is_lab_item: true  },
  { item_id: 'INV-EE-004', item_name: 'Multimeters',            category: 'Electronics',        quantity: 25,location: 'Basic Electrical Lab',condition: 'good', assigned_department: 'EE',  is_lab_item: false },
  // CHEM
  { item_id: 'INV-CHEM-001', item_name: 'UV-Vis Spectrophotometer', category: 'Lab Equipment', quantity: 2, location: 'Analytical Lab',    condition: 'good', assigned_department: 'CHEM', is_lab_item: true  },
  { item_id: 'INV-CHEM-002', item_name: 'Fume Hood',            category: 'Lab Equipment',      quantity: 4, location: 'Organic Chem Lab',  condition: 'good', assigned_department: 'CHEM', is_lab_item: true  },
  { item_id: 'INV-CHEM-003', item_name: 'Rotary Evaporator',    category: 'Lab Equipment',      quantity: 2, location: 'Organic Chem Lab',  condition: 'good', assigned_department: 'CHEM', is_lab_item: true  },
  { item_id: 'INV-CHEM-004', item_name: 'Lab Glassware Set',    category: 'Lab Equipment',      quantity: 10,location: 'All Labs',           condition: 'new',  assigned_department: 'CHEM', is_lab_item: false },
  // RM
  { item_id: 'INV-RM-001', item_name: 'GIS Mapping Software',  category: 'Software',            quantity: 10,location: 'Computer Lab',      condition: 'good', assigned_department: 'RM',   is_lab_item: false },
  { item_id: 'INV-RM-002', item_name: 'Field Survey Kits',     category: 'Survey Equipment',    quantity: 8, location: 'Field Lab',         condition: 'good', assigned_department: 'RM',   is_lab_item: false },
  { item_id: 'INV-RM-003', item_name: 'Projector',             category: 'Electronics',         quantity: 2, location: 'Seminar Room',      condition: 'good', assigned_department: 'RM',   is_lab_item: false },
];

// Finance Officers
const FINANCE_OFFICERS = [
  { officer_id: 'FIN001', officer_name: 'Sunita Reddy',   password: 'fin123' },
  { officer_id: 'FIN002', officer_name: 'Arvind Mishra',  password: 'fin123' },
];

// ─────────────────────────────────────────────────────────────────────────────
// MAIN
// ─────────────────────────────────────────────────────────────────────────────

async function main() {
  console.log('\n🌱 UDIIMS Full Department Seed\n' + '='.repeat(50));

  // Check if new tables exist (migration might not have been run yet)
  const hasNewTables = await tableExists('fee_structures');
  if (!hasNewTables) {
    console.warn('\n⚠️  New tables (fee_structures, fee_payments, etc.) not found in DB.');
    console.warn('   Run migration.sql in Supabase SQL Editor first, then re-run this script.\n');
  }

  console.log('\n📦 Departments');
  await insert('departments', DEPARTMENTS, 'New departments (6)');

  console.log('\n👩‍🏫 Faculty');
  await insert('faculty', FACULTY, `Faculty (${FACULTY.length})`);

  console.log('\n🗂️  Secretaries');
  await insert('department_secretaries', SECRETARIES, `Secretaries (${SECRETARIES.length})`);

  console.log('\n🎓 Students');
  await insert('students', STUDENTS, `Students (${STUDENTS.length})`);

  console.log('\n📚 Courses');
  await insert('courses', COURSES, `Courses (${COURSES.length})`);

  console.log('\n📋 Course Registrations');
  await insert('course_registrations', REGISTRATIONS, `Registrations (${REGISTRATIONS.length})`);

  console.log('\n💰 Financial Records');
  // PostgREST requires all objects in a batch to have identical keys — normalise every record
  const normalised = FINANCIAL_RECORDS.map(r => ({
    record_id:             r.record_id,
    record_type:           r.record_type,
    amount:                r.amount,
    transaction_date:      r.transaction_date,
    department_id:         r.department_id       ?? null,
    student_id:            r.student_id          ?? null,
    semester_term:         r.semester_term       ?? null,
    fee_status:            r.fee_status          ?? null,
    fee_updated_timestamp: r.fee_updated_timestamp ?? null,
    grant_id:              r.grant_id            ?? null,
    consultancy_id:        r.consultancy_id      ?? null,
    project_id:            r.project_id          ?? null,
    description:           r.description         ?? null,
  }));
  // Split into fee records and non-fee records (safer batch grouping)
  const feeRecords    = normalised.filter(r => r.record_type === 'student-fee');
  const nonFeeRecords = normalised.filter(r => r.record_type !== 'student-fee');
  await insert('financial_records', feeRecords,    `Fee records (${feeRecords.length})`);
  await insert('financial_records', nonFeeRecords, `Grant/consultancy records (${nonFeeRecords.length})`);

  console.log('\n🔬 Projects');
  await insert('projects', PROJECTS, `Projects (${PROJECTS.length})`);

  console.log('\n🏭 Inventory');
  await insert('inventory', INVENTORY, `Inventory (${INVENTORY.length})`);

  console.log('\n🏦 Finance Officers');
  await insert('finance_officers', FINANCE_OFFICERS, `Finance Officers`);

  if (hasNewTables) {
    console.log('\n💳 Fee Structures & Payments');
    const feeStructures = STUDENTS.map(s => {
      const techDepts = ['CSE', 'PHY', 'MECH', 'CHME', 'EE', 'CHEM'];
      const nonTechFee = { ENG: 30000, MATH: 32000, IR: 30000, RM: 28000 };
      const totalFee = nonTechFee[s.department_id] ||
                       (s.department_id === 'PHY' || s.department_id === 'CHEM' ? 42000 : 50000);
      return {
        structure_id: `FS-${s.student_id}-Sem12025`,
        student_id: s.student_id,
        semester_term: 'Sem-1-2025',
        department_id: s.department_id,
        total_fee: totalFee
      };
    });
    await insert('fee_structures', feeStructures, `Fee structures (${feeStructures.length})`);

    const feePayments = [];
    STUDENTS.forEach(s => {
      const fr = FINANCIAL_RECORDS.find(r => r.student_id === s.student_id && r.record_type === 'student-fee');
      if (!fr) return;
      const total = fr.amount;
      if (fr.fee_status === 'paid') {
        feePayments.push({ payment_id: `FP-${s.student_id}-001`, student_id: s.student_id, semester_term: 'Sem-1-2025', amount: total, payment_date: '2025-01-15T00:00:00Z', payment_method: 'online', notes: 'Full payment' });
      } else if (fr.fee_status === 'partial') {
        const first = Math.round(total * 0.5);
        feePayments.push({ payment_id: `FP-${s.student_id}-001`, student_id: s.student_id, semester_term: 'Sem-1-2025', amount: first, payment_date: '2025-01-15T00:00:00Z', payment_method: 'online', notes: 'First installment' });
        feePayments.push({ payment_id: `FP-${s.student_id}-002`, student_id: s.student_id, semester_term: 'Sem-1-2025', amount: Math.round(total * 0.25), payment_date: '2025-02-15T00:00:00Z', payment_method: 'upi', notes: 'Second installment' });
      }
    });
    await insert('fee_payments', feePayments, `Fee payments (${feePayments.length})`);

    console.log('\n🏛️  Department Fund Sources & Usage');
    const allDepts = ['CSE','PHY','ENG','MATH','IR','MECH','CHME','EE','CHEM','RM'];
    const fundSources = [];
    const fundUsage   = [];
    const srcAmounts  = { CSE:500000, PHY:400000, ENG:150000, MATH:180000, IR:120000, MECH:600000, CHME:550000, EE:700000, CHEM:300000, RM:100000 };
    const conAmounts  = { CSE:75000,  PHY:60000,  ENG:25000,  MATH:40000,  IR:30000,  MECH:120000, CHME:90000,  EE:150000, CHEM:50000,  RM:20000  };
    allDepts.forEach(d => {
      fundSources.push({ source_id: `DFS-${d}-GRANT-2025`, department_id: d, source_name: 'Government Grant',    amount: srcAmounts[d], received_date: '2025-01-20T00:00:00Z', description: `Annual govt funding for ${d}` });
      fundSources.push({ source_id: `DFS-${d}-CON-2025`,   department_id: d, source_name: 'Consultancy Revenue', amount: conAmounts[d], received_date: '2025-03-01T00:00:00Z', description: `Consultancy income` });
      const labExp = Math.round(srcAmounts[d] * 0.2);
      const maint  = Math.round(srcAmounts[d] * 0.07);
      fundUsage.push({ usage_id: `DFU-${d}-LAB-2025`,   department_id: d, amount: labExp, usage_date: '2025-02-10T00:00:00Z', purpose: 'lab_equipment', description: 'Lab equipment procurement' });
      fundUsage.push({ usage_id: `DFU-${d}-MAINT-2025`, department_id: d, amount: maint,  usage_date: '2025-03-15T00:00:00Z', purpose: 'maintenance',   description: 'Annual maintenance of labs & classrooms' });
    });
    await insert('department_fund_sources', fundSources, `Fund sources (${fundSources.length})`);
    await insert('department_fund_usage',   fundUsage,   `Fund usage   (${fundUsage.length})`);
  }

  console.log('\n' + '='.repeat(50));
  if (errors === 0) {
    console.log('✅  All done — no errors!');
  } else {
    console.log(`⚠️  Completed with ${errors} error(s). Check logs above.`);
  }
}

main().catch(console.error);
