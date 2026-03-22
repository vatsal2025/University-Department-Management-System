import { useState, useEffect } from 'react';
import { getProjects, addProject, updateProject, getFaculty } from '../../api/api';

const EMPTY = { project_id: '', project_title: '', faculty_id: '', project_budget: '', project_status: 'active', abstract: '', publication_link: '' };

export default function ProjectManagement({ departmentId }) {
  const [projects, setProjects] = useState([]);
  const [faculty, setFaculty] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const [proj, fac] = await Promise.all([
        getProjects(departmentId),
        getFaculty(departmentId),
      ]);
      setProjects(proj.data || []);
      setFaculty((fac.data || []).filter(f => f.active_status));
    } catch { setError('Failed to load.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [departmentId]);

  const handleSave = async () => {
    setError('');
    try {
      const body = { ...form, department_id: departmentId, project_budget: parseFloat(form.project_budget) || 0 };
      if (modal === 'add') await addProject(body);
      else await updateProject(form.project_id, body);
      setSuccess(modal === 'add' ? 'Project added.' : 'Project updated.');
      setModal(null); load();
    } catch (e) { setError(e.response?.data?.error || 'Save failed.'); }
  };

  return (
    <div>
      <div className="section-header">
        <h3>Project Management — UC-07</h3>
        <button className="btn btn-primary" onClick={() => { setForm({ ...EMPTY }); setError(''); setModal('add'); }}>+ Add Project</button>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {loading ? <div className="loading"><div className="spinner"></div></div> : (
        <div className="card">
          <div className="table-wrapper">
            <table>
              <thead><tr><th>ID</th><th>Title</th><th>Supervisor</th><th>Budget (₹)</th><th>Status</th><th>Publication</th><th>Actions</th></tr></thead>
              <tbody>
                {projects.length === 0 ? <tr><td colSpan={7}><div className="empty-state">No projects.</div></td></tr>
                  : projects.map(p => (
                    <tr key={p.project_id}>
                      <td>{p.project_id}</td>
                      <td>{p.project_title}</td>
                      <td>{p.faculty_id || '—'}</td>
                      <td>₹{Number(p.project_budget).toLocaleString()}</td>
                      <td><span className={`badge ${p.project_status === 'active' ? 'badge-info' : 'badge-gray'}`}>{p.project_status}</span></td>
                      <td>{p.publication_link ? <a href={p.publication_link} target="_blank" rel="noopener noreferrer">Open</a> : '—'}</td>
                      <td><button className="btn btn-secondary btn-sm" onClick={() => { setForm({ ...p }); setError(''); setModal('edit'); }}>Edit</button></td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {modal && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-title">{modal === 'add' ? 'Add Project' : 'Edit Project'}</div>
            {error && <div className="alert alert-error">{error}</div>}
            <div className="form-row">
              <div className="form-group">
                <label>Project ID *</label>
                <input value={form.project_id} onChange={e => setForm(p => ({ ...p, project_id: e.target.value }))} disabled={modal === 'edit'} />
              </div>
              <div className="form-group">
                <label>Status</label>
                <select value={form.project_status} onChange={e => setForm(p => ({ ...p, project_status: e.target.value }))}>
                  <option>active</option><option>completed</option><option>inactive</option>
                </select>
              </div>
            </div>
            <div className="form-group">
              <label>Project Title *</label>
              <input value={form.project_title} onChange={e => setForm(p => ({ ...p, project_title: e.target.value }))} />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Supervising Faculty</label>
                <select value={form.faculty_id || ''} onChange={e => setForm(p => ({ ...p, faculty_id: e.target.value }))}>
                  <option value="">— Select Faculty —</option>
                  {faculty.map(f => <option key={f.faculty_id} value={f.faculty_id}>{f.faculty_name} ({f.faculty_id})</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Budget (₹)</label>
                <input type="number" value={form.project_budget} onChange={e => setForm(p => ({ ...p, project_budget: e.target.value }))} />
              </div>
            </div>
            <div className="form-group">
              <label>Abstract</label>
              <textarea value={form.abstract || ''} onChange={e => setForm(p => ({ ...p, abstract: e.target.value }))} />
            </div>
            <div className="form-group">
              <label>Publication Link</label>
              <input value={form.publication_link || ''} onChange={e => setForm(p => ({ ...p, publication_link: e.target.value }))} placeholder="https://..." />
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSave}>Save</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
