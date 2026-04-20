import { useState, useEffect } from 'react';
import { getGrants, recordGrant } from '../../api/api';

const EMPTY = { grant_id: '', amount: '', transaction_date: new Date().toISOString().split('T')[0], department_id: '', description: '' };

function nextGrantId(grants) {
  const nums = grants.map(g => parseInt((g.grant_id || '').replace(/\D/g, ''))).filter(n => !isNaN(n));
  const next = nums.length > 0 ? Math.max(...nums) + 1 : 1;
  return 'G' + String(next).padStart(3, '0');
}

export default function GrantManagement() {
  const [grants, setGrants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(false);
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

  const openModal = () => {
    setForm({ ...EMPTY, grant_id: nextGrantId(grants) });
    setError('');
    setModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault(); setError('');
    try {
      await recordGrant({ ...form, amount: parseFloat(form.amount) });
      setSuccess('Grant recorded successfully.');
      setModal(false); setForm(EMPTY); load();
    } catch (e) { setError(e.response?.data?.error || 'Failed.'); }
  };

  const totalGrants = grants.reduce((s, g) => s + (g.amount || 0), 0);

  return (
    <div>
      <div className="section-header">
        <h3>Grant Management — UC-11</h3>
        <button className="btn btn-primary" onClick={openModal}>+ Record Grant</button>
      </div>
      {error && !modal && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

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
                      <td>{g.description || '—'}</td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {modal && (
        <div className="modal-overlay" onClick={() => setModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-title">Record New Grant</div>
            {error && <div className="alert alert-error">{error}</div>}
            <form onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label>Grant ID (auto-generated)</label>
                  <input value={form.grant_id} onChange={e => setForm(p => ({ ...p, grant_id: e.target.value }))} required />
                </div>
                <div className="form-group">
                  <label>Department *</label>
                  <select value={form.department_id} onChange={e => setForm(p => ({ ...p, department_id: e.target.value }))} required>
                    <option value="">— Select —</option>
                    <option value="CSE">CSE</option>
                    <option value="PHY">PHY</option>
                    <option value="ENG">ENG</option>
                    <option value="MATH">MATH</option>
                  </select>
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
                <input value={form.description} onChange={e => setForm(p => ({ ...p, description: e.target.value }))} placeholder="e.g. SERB research grant for AI lab" />
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Record Grant</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
