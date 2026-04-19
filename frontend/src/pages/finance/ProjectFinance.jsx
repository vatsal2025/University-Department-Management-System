import { useState, useEffect } from 'react';
import { getFinanceProjects, getProjectFinance, recordProjectFinance } from '../../api/api';

const EMPTY = { project_id: '', record_type: 'expense', amount: '', transaction_date: new Date().toISOString().split('T')[0], department_id: '', description: '' };

export default function ProjectFinance() {
  const [projects, setProjects] = useState([]);
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [budgetWarning, setBudgetWarning] = useState(false);
  const [deptFilter, setDeptFilter] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const [proj, recs] = await Promise.all([
        getFinanceProjects(deptFilter || undefined),
        getProjectFinance(deptFilter || undefined),
      ]);
      setProjects(proj.data || []);
      setRecords(recs.data || []);
    } catch { setError('Failed to load.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [deptFilter]);

  const handleSubmit = async (e, confirmed = false) => {
    if (e) e.preventDefault();
    setError(''); setBudgetWarning(false);
    try {
      const body = { ...form, amount: parseFloat(form.amount), confirmed };
      await recordProjectFinance(body);
      setSuccess('Project finance entry recorded.');
      setShowForm(false); setForm(EMPTY); load();
    } catch (err) {
      if (err.response?.status === 409 && err.response?.data?.requires_confirmation) {
        setBudgetWarning(true);
        setError(err.response.data.error.replace('BUDGET_WARNING: ', ''));
      } else {
        setError(err.response?.data?.error || 'Failed.');
      }
    }
  };

  return (
    <div>
      <div className="section-header">
        <h3>Project Financial Management — UC-14</h3>
        <button className="btn btn-primary" onClick={() => { setShowForm(!showForm); setError(''); setBudgetWarning(false); }}>
          {showForm ? 'Cancel' : '+ Record Entry'}
        </button>
      </div>
      {error && (
        <div className={`alert ${budgetWarning ? 'alert-warning' : 'alert-error'}`}>
          {error}
          {budgetWarning && (
            <div style={{ marginTop: 10 }}>
              <button className="btn btn-warning btn-sm" onClick={() => handleSubmit(null, true)}>Confirm Entry</button>
              <button className="btn btn-secondary btn-sm" style={{ marginLeft: 8 }} onClick={() => { setError(''); setBudgetWarning(false); }}>Cancel</button>
            </div>
          )}
        </div>
      )}
      {success && <div className="alert alert-success">{success}</div>}

      {showForm && (
        <div className="card">
          <div className="card-title">New Project Finance Entry</div>
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label>Project ID *</label>
                <select value={form.project_id} onChange={e => {
                  const p = projects.find(pr => pr.project_id === e.target.value);
                  setForm(prev => ({ ...prev, project_id: e.target.value, department_id: p?.department_id || '' }));
                }} required>
                  <option value="">— Select Project —</option>
                  {projects.map(p => <option key={p.project_id} value={p.project_id}>{p.project_title} ({p.project_id})</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Record Type</label>
                <select value={form.record_type} onChange={e => setForm(p => ({ ...p, record_type: e.target.value }))}>
                  <option value="expense">Expense</option>
                  <option value="project-budget">Budget Allocation</option>
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
              <textarea
                value={form.description}
                onChange={e => setForm(p => ({ ...p, description: e.target.value }))}
                rows={4}
                placeholder={
                  'Expense Category: [Equipment / Travel / Personnel / Software / Other]\n' +
                  'Authorized By: [approving authority name]\n' +
                  'Vendor / Payee: [supplier or recipient name]\n' +
                  'Purpose: [brief description of what the funds are used for]'
                }
                style={{ resize: 'vertical' }}
              />
            </div>
            <button type="submit" className="btn btn-primary">Record Entry</button>
          </form>
        </div>
      )}

      <div style={{ marginBottom: 16, maxWidth: 280 }}>
        <select value={deptFilter} onChange={e => setDeptFilter(e.target.value)}>
          <option value="">All Departments</option>
          <option value="CSE">CSE</option>
          <option value="PHY">PHY</option>
          <option value="ENG">ENG</option>
          <option value="MATH">MATH</option>
        </select>
      </div>

      <div className="card">
        <div className="card-title">Projects Overview</div>
        <div className="table-wrapper">
          <table>
            <thead><tr><th>Project ID</th><th>Title</th><th>Budget (₹)</th><th>Status</th></tr></thead>
            <tbody>
              {projects.length === 0 ? <tr><td colSpan={4}><div className="empty-state">No projects.</div></td></tr>
                : projects.map(p => (
                  <tr key={p.project_id}>
                    <td>{p.project_id}</td>
                    <td>{p.project_title}</td>
                    <td>₹{Number(p.project_budget).toLocaleString()}</td>
                    <td><span className={`badge ${p.project_status === 'active' ? 'badge-info' : 'badge-gray'}`}>{p.project_status}</span></td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      </div>

      {loading ? <div className="loading"><div className="spinner"></div></div> : (
        <div className="card">
          <div className="card-title">Financial Transactions</div>
          <div className="table-wrapper">
            <table>
              <thead><tr><th>Date</th><th>Project</th><th>Type</th><th>Amount (₹)</th><th>Description</th></tr></thead>
              <tbody>
                {records.length === 0 ? <tr><td colSpan={5}><div className="empty-state">No records.</div></td></tr>
                  : records.map(r => (
                    <tr key={r.record_id}>
                      <td>{r.transaction_date ? new Date(r.transaction_date).toLocaleDateString() : '—'}</td>
                      <td>{r.project_id || '—'}</td>
                      <td><span className={`badge ${r.record_type === 'project-budget' ? 'badge-success' : 'badge-warning'}`}>{r.record_type}</span></td>
                      <td>₹{Number(r.amount).toLocaleString()}</td>
                      <td>{r.description || '—'}</td>
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
