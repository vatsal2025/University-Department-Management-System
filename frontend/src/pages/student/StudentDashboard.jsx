import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { getStudent, calculateGpa } from '../../api/api';
import CourseRegistration from './CourseRegistration';
import MyCourses from './MyCourses';
import GPAView from './GPAView';
import BacklogTracking from './BacklogTracking';
import FeeStatus from './FeeStatus';

const TABS = ['Dashboard', 'Course Registration', 'My Courses', 'GPA & Grades', 'Program & Backlogs', 'Fee Status'];

export default function StudentDashboard() {
  const { user, logoutUser } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState(0);
  const [student, setStudent] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStudent = async () => {
      try {
        const res = await getStudent(user.student_id);
        setStudent(res.data);
        // Auto-calculate GPA on load
        await calculateGpa(user.student_id);
        const updated = await getStudent(user.student_id);
        setStudent(updated.data);
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    };
    fetchStudent();
  }, [user.student_id]);

  const handleLogout = () => { logoutUser(); navigate('/'); };

  if (loading) return (
    <div className="layout">
      <nav className="navbar"><span className="navbar-brand">UDIIMS</span></nav>
      <div className="loading"><div className="spinner"></div><p style={{marginTop:12}}>Loading dashboard...</p></div>
    </div>
  );

  return (
    <div className="layout">
      <nav className="navbar">
        <span className="navbar-brand">UDIIMS — Student Portal</span>
        <div className="navbar-right">
          <span className="navbar-user">{student?.student_name} ({user.student_id})</span>
          <button className="btn-logout" onClick={handleLogout}>Logout</button>
        </div>
      </nav>

      <div className="tabs">
        {TABS.map((t, i) => (
          <div key={i} className={`tab${activeTab === i ? ' active' : ''}`} onClick={() => setActiveTab(i)}>{t}</div>
        ))}
      </div>

      <div className="content">
        {activeTab === 0 && <Dashboard student={student} />}
        {activeTab === 1 && <CourseRegistration studentId={user.student_id} departmentId={student?.department_id} />}
        {activeTab === 2 && <MyCourses studentId={user.student_id} />}
        {activeTab === 3 && <GPAView studentId={user.student_id} student={student} />}
        {activeTab === 4 && <BacklogTracking studentId={user.student_id} />}
        {activeTab === 5 && <FeeStatus studentId={user.student_id} />}
      </div>
    </div>
  );
}

function Dashboard({ student }) {
  if (!student) return null;
  return (
    <>
      <div className="stats-row">
        <div className="stat-card">
          <div className="stat-value">{Number(student.cgpa || 0).toFixed(2)}</div>
          <div className="stat-label">CGPA</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{Number(student.sgpa || 0).toFixed(2)}</div>
          <div className="stat-label">Current SGPA</div>
        </div>
        <div className="stat-card">
          <div className="stat-value" style={{ color: student.backlog_count > 0 ? '#ef4444' : '#10b981' }}>
            {student.backlog_count || 0}
          </div>
          <div className="stat-label">Backlogs</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{student.semester}</div>
          <div className="stat-label">Current Semester</div>
        </div>
      </div>
      <div className="card">
        <div className="card-title">Student Profile</div>
        <table>
          <tbody>
            <tr><td><strong>Name</strong></td><td>{student.student_name}</td></tr>
            <tr><td><strong>Roll Number</strong></td><td>{student.student_id}</td></tr>
            <tr><td><strong>Program</strong></td><td>{student.program}</td></tr>
            <tr><td><strong>Department</strong></td><td>{student.department_id}</td></tr>
            <tr><td><strong>Semester</strong></td><td>{student.semester}</td></tr>
          </tbody>
        </table>
      </div>
    </>
  );
}
