import { useState, useEffect } from 'react';
import { getAvailableCourses, getRegistrations, registerCourses, dropCourse } from '../../api/api';

export default function CourseRegistration({ studentId, departmentId }) {
  const [semesterTerm, setSemesterTerm] = useState('Sem-1-2025');
  const [available, setAvailable] = useState([]);
  const [registrations, setRegistrations] = useState([]);
  const [selected, setSelected] = useState([]);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const [avail, regs] = await Promise.all([
        getAvailableCourses(semesterTerm, departmentId),
        getRegistrations(studentId, null),
      ]);
      setAvailable(avail.data || []);
      setRegistrations(regs.data || []);
    } catch (e) {
      setError('Failed to load courses.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [semesterTerm]);

  const isRegistered = (code) => registrations.some(r => r.course_code === code && r.semester_term === semesterTerm && r.registration_status === 'active');
  const isCompleted = (code) => registrations.some(r => r.course_code === code && r.registration_status === 'completed');

  const toggle = (code) => {
    setSelected(prev => prev.includes(code) ? prev.filter(c => c !== code) : [...prev, code]);
  };

  const handleRegister = async () => {
    if (selected.length === 0) { setError('Select at least one course.'); return; }
    setLoading(true); setError(''); setResult(null);
    try {
      const res = await registerCourses(studentId, semesterTerm, selected);
      setResult(res.data);
      setSelected([]);
      await load();
    } catch (e) {
      setError(e.response?.data?.error || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleDrop = async (courseCode) => {
    if (!confirm(`Drop ${courseCode} from ${semesterTerm}?`)) return;
    try {
      await dropCourse(studentId, courseCode, semesterTerm);
      await load();
    } catch (e) {
      setError(e.response?.data?.error || 'Failed to drop course.');
    }
  };

  return (
    <div>
      <div className="card">
        <div className="card-title">Course Registration — UC-02</div>
        <div className="form-group" style={{ maxWidth: 280 }}>
          <label>Semester Term</label>
          <select value={semesterTerm} onChange={e => setSemesterTerm(e.target.value)}>
            <option>Sem-1-2025</option>
            <option>Sem-2-2025</option>
            <option>Sem-1-2024</option>
            <option>Sem-2-2024</option>
          </select>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {result && (
        <div className="alert alert-success">
          Registered: {result.registered?.join(', ') || 'None'}.
          {result.failed?.length > 0 && ` Failed: ${result.failed.join(', ')}`}
        </div>
      )}

      <div className="card">
        <div className="section-header">
          <h3>Available Courses</h3>
          {selected.length > 0 && (
            <button className="btn btn-primary" onClick={handleRegister} disabled={loading}>
              Register {selected.length} Course(s)
            </button>
          )}
        </div>
        {loading ? <div className="loading"><div className="spinner"></div></div> : (
          available.length === 0 ? <div className="empty-state">No courses available for this term.</div> : (
            <div className="table-wrapper">
              <table>
                <thead><tr><th>Select</th><th>Code</th><th>Course Name</th><th>Credits</th><th>Status</th></tr></thead>
                <tbody>
                  {available.map(c => {
                    const registered = isRegistered(c.course_code);
                    const completed = isCompleted(c.course_code);
                    return (
                      <tr key={c.course_code}>
                        <td>
                          <input type="checkbox" checked={selected.includes(c.course_code)}
                            onChange={() => toggle(c.course_code)}
                            disabled={registered || completed} />
                        </td>
                        <td>{c.course_code}</td>
                        <td>{c.course_name}</td>
                        <td>{c.credit_hours}</td>
                        <td>
                          {completed ? <span className="badge badge-success">Completed</span>
                            : registered ? <span className="badge badge-info">Registered</span>
                              : <span className="badge badge-gray">Available</span>}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )
        )}
      </div>

      <div className="card">
        <div className="card-title">My Registrations (All Semesters)</div>
        {registrations.length === 0 ? <div className="empty-state">No registrations found.</div> : (
          <div className="table-wrapper">
            <table>
              <thead><tr><th>Semester</th><th>Code</th><th>Course</th><th>Credits</th><th>Status</th><th>Grade</th><th>Backlog</th><th>Action</th></tr></thead>
              <tbody>
                {registrations.map(r => (
                  <tr key={r.registration_id}>
                    <td>{r.semester_term}</td>
                    <td>{r.course_code}</td>
                    <td>{r.course_name}</td>
                    <td>{r.credit_hours}</td>
                    <td>
                      <span className={`badge ${r.registration_status === 'completed' ? 'badge-success' : r.registration_status === 'dropped' ? 'badge-gray' : 'badge-info'}`}>
                        {r.registration_status}
                      </span>
                    </td>
                    <td>{r.grade || '—'}</td>
                    <td>{r.backlog_flag ? <span className="badge badge-danger">Yes</span> : '—'}</td>
                    <td>
                      {r.registration_status === 'active' && r.semester_term === semesterTerm && (
                        <button className="btn btn-danger btn-sm" onClick={() => handleDrop(r.course_code)}>Drop</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
