import { useState, useEffect } from 'react';
import { getBacklogs } from '../../api/api';

export default function BacklogTracking({ studentId }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getBacklogs(studentId)
      .then(res => setData(res.data))
      .catch(() => setError('Failed to load backlog data.'))
      .finally(() => setLoading(false));
  }, [studentId]);

  if (loading) return <div className="loading"><div className="spinner"></div></div>;
  if (error) return <div className="alert alert-error">{error}</div>;

  const creditsPercent = data?.credits_required > 0
    ? Math.min(100, Math.round((data.credits_earned / data.credits_required) * 100)) : 0;

  return (
    <div>
      {data?.message && <div className="alert alert-info">{data.message}</div>}

      <div className="stats-row">
        <div className="stat-card">
          <div className="stat-value" style={{ color: data?.backlogs?.length > 0 ? '#ef4444' : '#10b981' }}>
            {data?.backlogs?.length || 0}
          </div>
          <div className="stat-label">Pending Backlogs</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{data?.completed?.length || 0}</div>
          <div className="stat-label">Completed Courses</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{data?.credits_earned || 0} / {data?.credits_required || 180}</div>
          <div className="stat-label">Credits Earned</div>
        </div>
      </div>

      <div className="card">
        <div className="card-title">Credit Progress</div>
        <div style={{ background: '#e2e8f0', borderRadius: 8, height: 20, overflow: 'hidden' }}>
          <div style={{ background: creditsPercent >= 100 ? '#10b981' : '#1e3a5f', height: '100%', width: `${creditsPercent}%`, transition: 'width 0.5s' }}></div>
        </div>
        <p style={{ marginTop: 8, color: '#64748b', fontSize: '0.88rem' }}>{creditsPercent}% complete ({data?.credits_earned} of {data?.credits_required} credits)</p>
      </div>

      {data?.backlogs?.length > 0 ? (
        <div className="card">
          <div className="card-title">Pending Backlogs (Grade = F)</div>
          <div className="table-wrapper">
            <table>
              <thead><tr><th>Semester</th><th>Course Code</th><th>Course Name</th><th>Credits</th><th>Grade</th></tr></thead>
              <tbody>
                {data.backlogs.map(r => (
                  <tr key={r.registration_id}>
                    <td>{r.semester_term}</td>
                    <td>{r.course_code}</td>
                    <td>{r.course_name}</td>
                    <td>{r.credit_hours}</td>
                    <td><span className="badge badge-danger">{r.grade}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="alert alert-success">No pending backlogs.</div>
      )}

      {data?.completed?.length > 0 && (
        <div className="card">
          <div className="card-title">Completed Courses</div>
          <div className="table-wrapper">
            <table>
              <thead><tr><th>Semester</th><th>Course Code</th><th>Course Name</th><th>Credits</th><th>Grade</th></tr></thead>
              <tbody>
                {data.completed.map(r => (
                  <tr key={r.registration_id}>
                    <td>{r.semester_term}</td>
                    <td>{r.course_code}</td>
                    <td>{r.course_name}</td>
                    <td>{r.credit_hours}</td>
                    <td>{r.grade || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
