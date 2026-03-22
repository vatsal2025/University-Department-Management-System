import { useState, useEffect } from 'react';
import { getFees } from '../../api/api';

const STATUS_BADGE = {
  paid: 'badge-success', pending: 'badge-danger', partial: 'badge-warning', waived: 'badge-info',
};

export default function FeeStatus({ studentId }) {
  const [fees, setFees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getFees(studentId)
      .then(res => setFees(Array.isArray(res.data) ? res.data : []))
      .catch(err => setError(err.response?.data?.error || 'No fee records available.'))
      .finally(() => setLoading(false));
  }, [studentId]);

  if (loading) return <div className="loading"><div className="spinner"></div></div>;

  return (
    <div>
      <div className="read-only-note">This is a read-only view. Fee modifications can only be done by Finance Officers.</div>
      {error ? <div className="alert alert-info">{error}</div> : (
        fees.length === 0 ? <div className="empty-state">No fee records available.</div> : (
          <div className="card">
            <div className="card-title">Fee Status — UC-05</div>
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr><th>Semester</th><th>Amount (₹)</th><th>Status</th><th>Last Updated</th></tr>
                </thead>
                <tbody>
                  {fees.map(f => (
                    <tr key={f.record_id}>
                      <td>{f.semester_term}</td>
                      <td>₹{Number(f.amount).toLocaleString()}</td>
                      <td><span className={`badge ${STATUS_BADGE[f.fee_status] || 'badge-gray'}`}>{f.fee_status}</span></td>
                      <td>{f.fee_updated_timestamp ? new Date(f.fee_updated_timestamp).toLocaleDateString() : '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )
      )}
    </div>
  );
}
