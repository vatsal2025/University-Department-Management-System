import { useState, useEffect } from 'react';
import { getRegistrations, calculateGpa } from '../../api/api';

export default function GPAView({ studentId, student }) {
  const [gpaData, setGpaData] = useState(null);
  const [registrations, setRegistrations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [gpaRes, regRes] = await Promise.all([
          calculateGpa(studentId),
          getRegistrations(studentId, null),
        ]);
        setGpaData(gpaRes.data);
        setRegistrations(regRes.data || []);
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [studentId]);

  const handlePrint = () => window.print();

  if (loading) return <div className="loading"><div className="spinner"></div></div>;

  const completedRegs = registrations.filter(r => r.registration_status === 'completed');
  const semesterGroups = completedRegs.reduce((acc, r) => {
    acc[r.semester_term] = acc[r.semester_term] || [];
    acc[r.semester_term].push(r);
    return acc;
  }, {});

  return (
    <div>
      <div className="stats-row">
        <div className="stat-card">
          <div className="stat-value">{Number(student?.cgpa || gpaData?.cgpa || 0).toFixed(2)}</div>
          <div className="stat-label">CGPA</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{Number(student?.sgpa || gpaData?.sgpa || 0).toFixed(2)}</div>
          <div className="stat-label">Latest SGPA</div>
        </div>
        <div className="stat-card">
          <div className="stat-value" style={{ color: (gpaData?.backlog_count || 0) > 0 ? '#ef4444' : '#10b981' }}>
            {gpaData?.backlog_count || 0}
          </div>
          <div className="stat-label">Backlogs</div>
        </div>
      </div>

      {gpaData?.message && <div className="alert alert-info">{gpaData.message}</div>}

      {gpaData?.semester_sgpa && Object.keys(gpaData.semester_sgpa).length > 0 && (
        <div className="card">
          <div className="card-title">Semester-wise SGPA</div>
          <div className="table-wrapper">
            <table>
              <thead><tr><th>Semester</th><th>SGPA</th></tr></thead>
              <tbody>
                {Object.entries(gpaData.semester_sgpa).map(([sem, sgpa]) => (
                  <tr key={sem}><td>{sem}</td><td>{Number(sgpa).toFixed(2)}</td></tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {Object.keys(semesterGroups).map(sem => (
        <div className="card" key={sem}>
          <div className="card-title">
            {sem}
            {gpaData?.semester_sgpa?.[sem] && <span style={{ marginLeft: 12, fontWeight: 400, color: '#64748b' }}>SGPA: {Number(gpaData.semester_sgpa[sem]).toFixed(2)}</span>}
          </div>
          <div className="table-wrapper">
            <table>
              <thead><tr><th>Course Code</th><th>Course Name</th><th>Credits</th><th>Grade</th><th>Backlog</th></tr></thead>
              <tbody>
                {semesterGroups[sem].map(r => (
                  <tr key={r.registration_id}>
                    <td>{r.course_code}</td>
                    <td>{r.course_name}</td>
                    <td>{r.credit_hours}</td>
                    <td><strong>{r.grade || '—'}</strong></td>
                    <td>{r.backlog_flag ? <span className="badge badge-danger">F</span> : '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}

      <div style={{ textAlign: 'right', marginTop: 16 }}>
        <button className="btn btn-secondary" onClick={handlePrint}>Download / Print Grade Card</button>
      </div>
    </div>
  );
}
