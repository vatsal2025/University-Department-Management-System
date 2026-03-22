import { useState, useEffect } from 'react';
import { getDepartmentAccounts } from '../../api/api';

export default function DepartmentAccounts({ departmentId }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getDepartmentAccounts(departmentId)
      .then(res => setData(res.data))
      .catch(() => setError('Failed to load accounts.'))
      .finally(() => setLoading(false));
  }, [departmentId]);

  if (loading) return <div className="loading"><div className="spinner"></div></div>;
  if (error) return <div className="alert alert-error">{error}</div>;

  return (
    <div>
      <div className="read-only-note">Read-only view. Financial entries are managed by Finance Officers.</div>
      {data?.warning && <div className="warning-banner">{data.warning}</div>}
      {data?.message && <div className="alert alert-info">{data.message}</div>}

      <div className="stats-row">
        <div className="stat-card">
          <div className="stat-value">₹{Number(data?.budget_allocation || 0).toLocaleString()}</div>
          <div className="stat-label">Total Grants</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">₹{Number(data?.total_income || 0).toLocaleString()}</div>
          <div className="stat-label">Consultancy Income</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">₹{Number(data?.total_expenses || 0).toLocaleString()}</div>
          <div className="stat-label">Total Expenses</div>
        </div>
        <div className="stat-card">
          <div className="stat-value" style={{ color: (data?.remaining_balance || 0) < 0 ? '#ef4444' : '#10b981' }}>
            ₹{Number(data?.remaining_balance || 0).toLocaleString()}
          </div>
          <div className="stat-label">Remaining Balance</div>
        </div>
      </div>

      <div className="card">
        <div className="card-title">Financial Records — UC-09</div>
        <div className="table-wrapper">
          <table>
            <thead><tr><th>Date</th><th>Type</th><th>Amount (₹)</th><th>Description</th></tr></thead>
            <tbody>
              {!data?.records?.length ? (
                <tr><td colSpan={4}><div className="empty-state">No financial records.</div></td></tr>
              ) : data.records.map(r => (
                <tr key={r.record_id}>
                  <td>{r.transaction_date ? new Date(r.transaction_date).toLocaleDateString() : '—'}</td>
                  <td><span className={`badge ${r.record_type === 'grant' ? 'badge-success' : r.record_type === 'consultancy' ? 'badge-info' : r.record_type.includes('expense') ? 'badge-danger' : 'badge-warning'}`}>{r.record_type}</span></td>
                  <td>₹{Number(r.amount).toLocaleString()}</td>
                  <td>{r.description || r.grant_id || r.consultancy_id || r.project_id || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
