import { useState, useEffect } from 'react';
import { getConsultancy, recordConsultancy } from '../../api/api';

const EMPTY = { consultancy_id: '', amount: '', transaction_date: new Date().toISOString().split('T')[0], department_id: '', description: '' };

function nextConsultancyId(records) {
  const nums = records.map(r => parseInt((r.consultancy_id || '').replace(/\D/g, ''))).filter(n => !isNaN(n));
  const next = nums.length > 0 ? Math.max(...nums) + 1 : 1;
  return 'CON' + String(next).padStart(3, '0');
}

export default function ConsultancyManagement() {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(false);
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [deptFilter, setDeptFilter] = useState('');

  const load = async () => {
    setLoading(true);
    try { setRecords((await getConsultancy(deptFilter || undefined)).data || []); }
    catch { setError('Failed to load consultancy records.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [deptFilter]);

  const openModal = () => {
    setForm({ ...EMPTY, consultancy_id: nextConsultancyId(records) });
    setError('');
    setModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault(); setError('');
    try {
      await recordConsultancy({ ...form, amount: parseFloat(form.amount) });
      setSuccess('Consultancy income recorded.');
      setModal(false); setForm(EMPTY); load();
    } catch (e) { setError(e.response?.data?.error || 'Failed.'); }
  };

  const total = records.reduce((s, r) => s + (r.amount || 0), 0);

  return (
    <div>
      <div className="section-header">
        <h3>Consultancy Fund Management — UC-12</h3>
        <button className="btn btn-primary" onClick={openModal}>+ Record Consultancy</button>
      </div>
      {error && !modal && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="stats-row">
        <div className="stat-card">
          <div className="stat-value">{records.length}</div>
          <div className="stat-label">Total Records</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">₹{Number(total).toLocaleString()}</div>
          <div className="stat-label">Total Revenue</div>
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
              <thead><tr><th>Consultancy ID</th><th>Department</th><th>Amount (₹)</th><th>Date</th><th>Description</th></tr></thead>
              <tbody>
                {records.length === 0 ? <tr><td colSpan={5}><div className="empty-state">No consultancy records.</div></td></tr>
                  : records.map(r => (
                    <tr key={r.record_id}>
                      <td>{r.consultancy_id}</td>
                      <td>{r.department_id}</td>
                      <td>₹{Number(r.amount).toLocaleString()}</td>
                      <td>{r.transaction_date ? new Date(r.transaction_date).toLocaleDateString() : '—'}</td>
                      <td>{r.description || '—'}</td>
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
            <div className="modal-title">Record Consultancy Income</div>
            {error && <div className="alert alert-error">{error}</div>}
            <form onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label>Consultancy ID (auto-generated)</label>
                  <input value={form.consultancy_id} onChange={e => setForm(p => ({ ...p, consultancy_id: e.target.value }))} required />
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
                  <label>Date</label>
                  <input type="date" value={form.transaction_date} onChange={e => setForm(p => ({ ...p, transaction_date: e.target.value }))} />
                </div>
              </div>
              <div className="form-group">
                <label>Description</label>
                <input value={form.description} onChange={e => setForm(p => ({ ...p, description: e.target.value }))} placeholder="e.g. Industry project with XYZ Ltd." />
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Record Consultancy</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
