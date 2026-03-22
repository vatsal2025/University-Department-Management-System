import { useState, useEffect } from 'react';
import { getCourseOfferings, addCourseOffering, updateCourseOffering, removeCourseOffering } from '../../api/api';

const EMPTY = { course_code: '', course_name: '', credit_hours: 3, semester_term: 'Sem-1-2025' };

export default function CourseOfferings({ departmentId }) {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [semFilter, setSemFilter] = useState('Sem-1-2025');
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const load = async () => {
    setLoading(true);
    try { setCourses((await getCourseOfferings(departmentId, semFilter)).data || []); }
    catch { setError('Failed to load courses.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [departmentId, semFilter]);

  const handleSave = async () => {
    setError('');
    try {
      const body = { ...form, department_id: departmentId, credit_hours: parseInt(form.credit_hours) };
      if (modal === 'add') await addCourseOffering(body);
      else await updateCourseOffering(form.course_code, form.semester_term, departmentId, { course_name: form.course_name, credit_hours: body.credit_hours });
      setSuccess(modal === 'add' ? 'Course added.' : 'Course updated.');
      setModal(null); load();
    } catch (e) { setError(e.response?.data?.error || 'Save failed.'); }
  };

  const handleRemove = async (c) => {
    try {
      await removeCourseOffering(c.course_code, c.semester_term, departmentId);
      setSuccess('Course removed.');
      load();
    } catch (e) {
      const msg = e.response?.data?.error || '';
      if (msg.includes('enrolled')) {
        if (confirm(msg)) {
          // Force remove would require a different endpoint; for now just show message
          setError('Cannot remove: students are currently enrolled.');
        }
      } else {
        setError(msg);
      }
    }
  };

  return (
    <div>
      <div className="section-header">
        <h3>Course Offerings — UC-10</h3>
        <button className="btn btn-primary" onClick={() => { setForm({ ...EMPTY, semester_term: semFilter }); setError(''); setModal('add'); }}>+ Add Course</button>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div style={{ marginBottom: 16, maxWidth: 280 }}>
        <select value={semFilter} onChange={e => setSemFilter(e.target.value)}>
          <option>Sem-1-2025</option><option>Sem-2-2025</option><option>Sem-1-2024</option><option>Sem-2-2024</option>
        </select>
      </div>

      {loading ? <div className="loading"><div className="spinner"></div></div> : (
        <div className="card">
          <div className="table-wrapper">
            <table>
              <thead><tr><th>Code</th><th>Course Name</th><th>Credits</th><th>Semester</th><th>Actions</th></tr></thead>
              <tbody>
                {courses.length === 0 ? <tr><td colSpan={5}><div className="empty-state">No offerings for this term.</div></td></tr>
                  : courses.map(c => (
                    <tr key={`${c.course_code}-${c.semester_term}`}>
                      <td>{c.course_code}</td>
                      <td>{c.course_name}</td>
                      <td>{c.credit_hours}</td>
                      <td>{c.semester_term}</td>
                      <td>
                        <div className="btn-group">
                          <button className="btn btn-secondary btn-sm" onClick={() => { setForm({ ...c }); setError(''); setModal('edit'); }}>Edit</button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleRemove(c)}>Remove</button>
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
            <div className="modal-title">{modal === 'add' ? 'Add Course Offering' : 'Edit Course'}</div>
            {error && <div className="alert alert-error">{error}</div>}
            <div className="form-row">
              <div className="form-group">
                <label>Course Code *</label>
                <input value={form.course_code} onChange={e => setForm(p => ({ ...p, course_code: e.target.value }))} disabled={modal === 'edit'} />
              </div>
              <div className="form-group">
                <label>Credit Hours *</label>
                <input type="number" value={form.credit_hours} onChange={e => setForm(p => ({ ...p, credit_hours: e.target.value }))} min="1" />
              </div>
            </div>
            <div className="form-group">
              <label>Course Name *</label>
              <input value={form.course_name} onChange={e => setForm(p => ({ ...p, course_name: e.target.value }))} />
            </div>
            <div className="form-group">
              <label>Semester Term *</label>
              <select value={form.semester_term} onChange={e => setForm(p => ({ ...p, semester_term: e.target.value }))} disabled={modal === 'edit'}>
                <option>Sem-1-2025</option><option>Sem-2-2025</option><option>Sem-1-2024</option><option>Sem-2-2024</option>
              </select>
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
