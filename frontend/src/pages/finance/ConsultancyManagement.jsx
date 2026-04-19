import { useState, useEffect } from 'react';
import { getConsultancy, recordConsultancy } from '../../api/api';

const CONSULT_ID_REGEX = /^CONSULT-[A-Za-z0-9]+-\d+$/;
const EMPTY = { consultancy_id: '', amount: '', transaction_date: new Date().toISOString().split('T')[0], department_id: '', description: '' };

export default function ConsultancyManagement() {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
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

  const handleSubmit = async (e) => {
    e.preventDefault(); setError('');
    if (!CONSULT_ID_REGEX.test(form.consultancy_id)) {
      setError('Consultancy ID must follow the format: CONSULT-<DepartmentName>-<Number> (e.g. CONSULT-CSE-001).');
      return;
    }
    const idDeptSegment = form.consultancy_id.split('-')[1];
    if (idDeptSegment.toUpperCase() !== form.department_id.trim().toUpperCase()) {
      setError(`Consultancy ID department segment "${idDeptSegment}" must match the selected Department ID "${form.department_id}".`);
      return;
    }
    try {
      await recordConsultancy({ ...form, amount: parseFloat(form.amount) });
      setSuccess('Consultancy income recorded.');
      setShowForm(false); setForm(EMPTY); load();
    } catch (e) { setError(e.response?.data?.error || 'Failed.'); }
  };

  const total = records.reduce((s, r) => s + (r.amount || 0), 0);

  return (
    <div>
      <div className="section-header">
        <h3>Consultancy Fund Management — UC-12</h3>
        <button className="btn btn-primary" onClick={() => { setShowForm(!showForm); setError(''); setSuccess(''); }}>
          {showForm ? 'Cancel' : '+ Record Consultancy'}
        </button>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {showForm && (
        <div className="card">
          <div className="card-title">New Consultancy Entry</div>
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label>Consultancy ID *</label>
                <input
                  value={form.consultancy_id}
                  onChange={e => setForm(p => ({ ...p, consultancy_id: e.target.value }))}
                  placeholder="e.g. CONSULT-CSE-001"
                  required
                />
                <small style={{ color: '#6b7280' }}>Format: CONSULT-&lt;DepartmentName&gt;-&lt;Number&gt;</small>
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
                <label>Date</label>
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
                  'Project Title: [official name of the consultancy project]\n' +
                  'Client / Industry Partner: [company or institution name]\n' +
                  'Lead Consultant: [faculty name]\n' +
                  'Scope of Work: [brief description of services rendered]\n' +
                  'Contract Period: [start date] to [end date]'
                }
                style={{ resize: 'vertical' }}
              />
            </div>
            <button type="submit" className="btn btn-primary">Record Consultancy</button>
          </form>
        </div>
      )}

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
                      <td style={{ whiteSpace: 'pre-line', maxWidth: 320 }}>{r.description || '—'}</td>
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
