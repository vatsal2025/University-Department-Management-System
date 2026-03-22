import { useState, useEffect } from 'react';
import { getFaculty, addFaculty, updateFaculty, deactivateFaculty, forceDeactivateFaculty } from '../../api/api';

const EMPTY = { faculty_id: '', faculty_name: '', designation: '', department_name: '', specialization: '', active_status: true };

export default function FacultyManagement({ departmentId }) {
  const [faculty, setFaculty] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null); // null | 'add' | 'edit'
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const load = async () => {
    setLoading(true);
    try { setFaculty((await getFaculty(departmentId)).data || []); }
    catch { setError('Failed to load faculty.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [departmentId]);

  const openAdd = () => { setForm({ ...EMPTY, department_id: departmentId }); setError(''); setModal('add'); };
  const openEdit = (f) => { setForm({ ...f }); setError(''); setModal('edit'); };

  const handleSave = async () => {
    setError('');
    try {
      if (modal === 'add') await addFaculty(form);
      else await updateFaculty(form.faculty_id, form);
      setSuccess(modal === 'add' ? 'Faculty added.' : 'Faculty updated.');
      setModal(null);
      load();
    } catch (e) {
      setError(e.response?.data?.error || 'Save failed.');
    }
  };

  const handleDeactivate = async (f) => {
    try {
      await deactivateFaculty(f.faculty_id);
      setSuccess('Faculty deactivated.');
      load();
    } catch (e) {
      const msg = e.response?.data?.error || '';
      if (msg.includes('active project')) {
        if (confirm(msg + '\nForce deactivate?')) {
          await forceDeactivateFaculty(f.faculty_id);
          load();
        }
      } else {
        setError(msg);
      }
    }
  };

  return (
    <div>
      <div className="section-header">
        <h3>Faculty Management — UC-06</h3>
        <button className="btn btn-primary" onClick={openAdd}>+ Add Faculty</button>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {loading ? <div className="loading"><div className="spinner"></div></div> : (
        <div className="card">
          <div className="table-wrapper">
            <table>
              <thead><tr><th>ID</th><th>Name</th><th>Designation</th><th>Specialization</th><th>Status</th><th>Actions</th></tr></thead>
              <tbody>
                {faculty.length === 0 ? (
                  <tr><td colSpan={6}><div className="empty-state">No faculty records.</div></td></tr>
                ) : faculty.map(f => (
                  <tr key={f.faculty_id}>
                    <td>{f.faculty_id}</td>
                    <td>{f.faculty_name}</td>
                    <td>{f.designation}</td>
                    <td>{f.specialization || '—'}</td>
                    <td><span className={`badge ${f.active_status ? 'badge-success' : 'badge-gray'}`}>{f.active_status ? 'Active' : 'Inactive'}</span></td>
                    <td>
                      <div className="btn-group">
                        <button className="btn btn-secondary btn-sm" onClick={() => openEdit(f)}>Edit</button>
                        {f.active_status && <button className="btn btn-danger btn-sm" onClick={() => handleDeactivate(f)}>Remove</button>}
                      </div>
                    </td>
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
            <div className="modal-title">{modal === 'add' ? 'Add Faculty' : 'Edit Faculty'}</div>
            {error && <div className="alert alert-error">{error}</div>}
            <div className="form-row">
              <div className="form-group">
                <label>Faculty ID *</label>
                <input value={form.faculty_id} onChange={e => setForm(p => ({ ...p, faculty_id: e.target.value }))} disabled={modal === 'edit'} />
              </div>
              <div className="form-group">
                <label>Name *</label>
                <input value={form.faculty_name} onChange={e => setForm(p => ({ ...p, faculty_name: e.target.value }))} />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Designation *</label>
                <input value={form.designation} onChange={e => setForm(p => ({ ...p, designation: e.target.value }))} />
              </div>
              <div className="form-group">
                <label>Specialization</label>
                <input value={form.specialization || ''} onChange={e => setForm(p => ({ ...p, specialization: e.target.value }))} />
              </div>
            </div>
            <div className="form-group">
              <label>Department Name</label>
              <input value={form.department_name || ''} onChange={e => setForm(p => ({ ...p, department_name: e.target.value }))} />
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
