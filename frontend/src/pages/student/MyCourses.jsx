import { useState, useEffect } from 'react';
import { getRegistrations } from '../../api/api';

const SEMESTERS = ['All', 'Sem-2-2025', 'Sem-1-2025', 'Sem-2-2024', 'Sem-1-2024'];

const parseTerm = (t) => {
  const parts = t.split('-'); // ['Sem', '1', '2025']
  return { year: parseInt(parts[2]), sem: parseInt(parts[1]) };
};

export default function MyCourses({ studentId }) {
  const [registrations, setRegistrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [semFilter, setSemFilter] = useState('All');

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const res = await getRegistrations(studentId, null);
        setRegistrations(res.data || []);
      } catch {
        setError('Failed to load course history.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [studentId]);

  const filtered = semFilter === 'All'
    ? registrations
    : registrations.filter(r => r.semester_term === semFilter);

  // Group by semester_term
  const grouped = filtered.reduce((acc, r) => {
    const sem = r.semester_term || 'Unknown';
    if (!acc[sem]) acc[sem] = [];
    acc[sem].push(r);
    return acc;
  }, {});

  const semesterOrder = Object.keys(grouped).sort((a, b) => {
    const pa = parseTerm(a), pb = parseTerm(b);
    if (pb.year !== pa.year) return pb.year - pa.year;
    return pb.sem - pa.sem;
  });

  return (
    <div>
      <div className="section-header">
        <h3>My Courses — UC-02</h3>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div style={{ marginBottom: 16, maxWidth: 280 }}>
        <select value={semFilter} onChange={e => setSemFilter(e.target.value)}>
          {SEMESTERS.map(s => <option key={s}>{s}</option>)}
        </select>
      </div>

      {loading ? <div className="loading"><div className="spinner"></div></div> : (
        registrations.length === 0 ? (
          <div className="card"><div className="empty-state">No course registrations found.</div></div>
        ) : semesterOrder.length === 0 ? (
          <div className="card"><div className="empty-state">No courses for the selected semester.</div></div>
        ) : (
          semesterOrder.map(sem => (
            <div className="card" key={sem}>
              <div className="card-title">{sem}</div>
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr><th>Code</th><th>Course</th><th>Credits</th><th>Status</th><th>Grade</th><th>Backlog</th></tr>
                  </thead>
                  <tbody>
                    {grouped[sem].map(r => (
                      <tr key={r.registration_id}>
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
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ))
        )
      )}
    </div>
  );
}
