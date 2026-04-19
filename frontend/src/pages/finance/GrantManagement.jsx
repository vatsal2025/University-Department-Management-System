import { useState, useEffect } from 'react';
import { getGrants, recordGrant } from '../../api/api';

const GRANT_ID_REGEX = /^GRANT-[A-Za-z0-9]+-\d+$/;
const EMPTY = { grant_id: '', amount: '', transaction_date: new Date().toISOString().split('T')[0], department_id: '', description: '' };

export default function GrantManagement() {
  const [grants, setGrants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [deptFilter, setDeptFilter] = useState('');

  const load = async () => {
    setLoading(true);
    try { setGrants((await getGrants(deptFilter || undefined)).data || []); }
    catch { setError('Failed to load grants.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [deptFilter]);

  const handleSubmit = async (e) => {
    e.preventDefault(); setError('');
    if (!GRANT_ID_REGEX.test(form.grant_id)) {
      setError('Grant ID must follow the format: GRANT-<DepartmentName>-<Number> (e.g. GRANT-CSE-001).');
      return;
    }
    const idDeptSegment = form.grant_id.split('-')[1];
    if (idDeptSegment.toUpperCase() !== form.department_id.trim().toUpperCase()) {
      setError(`Grant ID department segment "${idDeptSegment}" must match the selected Department ID "${form.department_id}".`);
      return;
    }
    try {
      await recordGrant({ ...form, amount: parseFloat(form.amount) });
      setSuccess('Grant recorded successfully.');
      setShowForm(false); setForm(EMPTY); load();
    } catch (e) { setError(e.response?.data?.error || 'Failed.'); }
  };

  const totalGrants = grants.reduce((s, g) => s + (g.amount || 0), 0);

  return (
    <div>
      <div className="section-header">
        <h3>Grant Management — UC-11</h3>
        <button className="btn btn-primary" onClick={() => { setShowForm(!showForm); setError(''); setSuccess(''); }}>
          {showForm ? 'Cancel' : '+ Record Grant'}
        </button>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {showForm && (
        <div className="card">
          <div className="card-title">New Grant Entry</div>
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label>Grant ID *</label>
                <input
                  value={form.grant_id}
                  onChange={e => setForm(p => ({ ...p, grant_id: e.target.value }))}
                  placeholder="e.g. GRANT-CSE-001"
                  required
                />
                <small style={{ color: '#6b7280' }}>Format: GRANT-&lt;DepartmentName&gt;-&lt;Number&gt;</small>
              </div>
              <div className="form-group">
                <label>Department ID *</label>
                <input value={form.department_id} onChange={e => setForm(p => ({ ...p, department_id: e.target.value }))} placeholder="e.g. CSE" required />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Amount (₹) *</label>
                <input type="number" value={form.amount} onChange={e => setForm(p => ({ ...p, amount: e.target.value }))} required min="1" />
              </div>
              <div className="form-group">
                <label>Transaction Date *</label>
                <input type="date" value={form.transaction_date} onChange={e => setForm(p => ({ ...p, transaction_date: e.target.value }))} />
              </div>
            </div>
            <div className="form-group">
              <label>Description</label>
              <textarea
                value={form.description}
                onChange={e => setForm(p => ({ ...p, description: e.target.value }))}
                rows={5}
                placeholder={
                  'Grant Title: [official title of the grant]\n' +
                  'Funding Agency: [DST / UGC / SERB / Industry / etc.]\n' +
                  'Principal Investigator: [faculty name]\n' +
                  'Allocated To: [faculty / lab / unit / department]\n' +
                  'Objectives: [brief purpose of the grant]\n' +
                  'Grant Period: [start date] to [end date]'
                }
                style={{ resize: 'vertical' }}
              />
            </div>
            <button type="submit" className="btn btn-primary">Record Grant</button>
          </form>
        </div>
      )}

      <div className="stats-row">
        <div className="stat-card">
          <div className="stat-value">{grants.length}</div>
          <div className="stat-label">Total Grants</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">₹{Number(totalGrants).toLocaleString()}</div>
          <div className="stat-label">Total Amount</div>
        </div>
      </div>

      <div style={{ marginBottom: 16, maxWidth: 280 }}>
        <select value={deptFilter} onChange={e => setDeptFilter(e.target.value)}>
          <option value="">All Departments</option>
          <option value="CSE">CSE</option>
          <option value="PHY">PHY</option>
          <option value="ENG">ENG</option>
          <option value="MATH">MATH</option>
        </select>
      </div>

      {loading ? <div className="loading"><div className="spinner"></div></div> : (
        <div className="card">
          <div className="table-wrapper">
            <table>
              <thead><tr><th>Grant ID</th><th>Department</th><th>Amount (₹)</th><th>Date</th><th>Description</th></tr></thead>
              <tbody>
                {grants.length === 0 ? <tr><td colSpan={5}><div className="empty-state">No grants recorded.</div></td></tr>
                  : grants.map(g => (
                    <tr key={g.record_id}>
                      <td>{g.grant_id}</td>
                      <td>{g.department_id}</td>
                      <td>₹{Number(g.amount).toLocaleString()}</td>
                      <td>{g.transaction_date ? new Date(g.transaction_date).toLocaleDateString() : '—'}</td>
                      <td style={{ whiteSpace: 'pre-line', maxWidth: 320 }}>{g.description || '—'}</td>
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
