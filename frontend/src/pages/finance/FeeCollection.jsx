import { useState } from 'react';
import { getStudentFees, updateFeeStatus, createFeeRecord } from '../../api/api';

const STATUS_BADGE = { paid: 'badge-success', pending: 'badge-danger', partial: 'badge-warning', waived: 'badge-info' };
const SEMESTERS = ['Sem-1-2025', 'Sem-2-2025', 'Sem-1-2024', 'Sem-2-2024'];

export default function FeeCollection() {
  const [rollNumber, setRollNumber] = useState('');
  const [studentData, setStudentData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showNewFee, setShowNewFee] = useState(false);
  const [newFee, setNewFee] = useState({ semester_term: 'Sem-1-2025', amount: '', department_id: '' });

  const searchStudent = async (e) => {
    e.preventDefault();
    if (!rollNumber.trim()) { setError('Roll Number is required.'); return; }
    setError(''); setStudentData(null); setLoading(true);
    try {
      const res = await getStudentFees(rollNumber.trim());
      setStudentData(res.data);
    } catch (e) {
      setError(e.response?.data?.error || 'Student record not found.');
    } finally { setLoading(false); }
  };

  const handleFeeUpdate = async (semesterTerm, feeStatus) => {
    setError(''); setSuccess('');
    try {
      await updateFeeStatus(studentData.student_id, semesterTerm, feeStatus);
      setSuccess(`Fee status updated to "${feeStatus}" for ${semesterTerm}.`);
      const res = await getStudentFees(studentData.student_id);
      setStudentData(res.data);
    } catch (e) {
      setError(e.response?.data?.error || 'Update failed.');
    }
  };

  const handleCreateFee = async (e) => {
    e.preventDefault(); setError('');
    try {
      await createFeeRecord({ ...newFee, student_id: studentData.student_id, amount: parseFloat(newFee.amount) });
      setSuccess('Fee record created.');
      setShowNewFee(false);
      const res = await getStudentFees(studentData.student_id);
      setStudentData(res.data);
    } catch (e) {
      setError(e.response?.data?.error || 'Failed to create fee record.');
    }
  };

  return (
    <div>
      <div style={{ marginBottom: 20 }}>
        <h3 style={{ fontSize: '1rem', fontWeight: 600, color: '#1e3a5f', marginBottom: 12 }}>Fee Collection & Modification — UC-13</h3>
        <form onSubmit={searchStudent} style={{ display: 'flex', gap: 12, maxWidth: 420 }}>
          <input type="text" value={rollNumber} onChange={e => setRollNumber(e.target.value)}
            placeholder="Enter Student Roll Number" style={{ flex: 1 }} />
          <button type="submit" className="btn btn-primary" disabled={loading}>Search</button>
        </form>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {loading && <div className="loading"><div className="spinner"></div></div>}

      {studentData && (
        <div>
          <div className="card">
            <div className="card-title">Student: {studentData.student_name} ({studentData.student_id})</div>
            <div className="section-header">
              <span></span>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowNewFee(!showNewFee)}>
                {showNewFee ? 'Cancel' : '+ Add Fee Record'}
              </button>
            </div>

            {showNewFee && (
              <form onSubmit={handleCreateFee} style={{ background: '#f8fafc', padding: 16, borderRadius: 8, marginBottom: 16 }}>
                <div className="form-row-3">
                  <div className="form-group">
                    <label>Semester</label>
                    <select value={newFee.semester_term} onChange={e => setNewFee(p => ({ ...p, semester_term: e.target.value }))}>
                      {SEMESTERS.map(s => <option key={s}>{s}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Amount (₹)</label>
                    <input type="number" value={newFee.amount} onChange={e => setNewFee(p => ({ ...p, amount: e.target.value }))} required min="1" />
                  </div>
                  <div className="form-group">
                    <label>Department</label>
                    <input value={newFee.department_id} onChange={e => setNewFee(p => ({ ...p, department_id: e.target.value }))} placeholder="e.g. CSE" />
                  </div>
                </div>
                <button type="submit" className="btn btn-primary btn-sm">Create Record</button>
              </form>
            )}

            {!studentData.fees?.length ? (
              <div className="empty-state">No fee records found for this student.</div>
            ) : (
              <div className="table-wrapper">
                <table>
                  <thead><tr><th>Semester</th><th>Amount (₹)</th><th>Status</th><th>Last Updated</th><th>Actions</th></tr></thead>
                  <tbody>
                    {studentData.fees.map(f => (
                      <tr key={f.record_id}>
                        <td>{f.semester_term}</td>
                        <td>₹{Number(f.amount).toLocaleString()}</td>
                        <td><span className={`badge ${STATUS_BADGE[f.fee_status] || 'badge-gray'}`}>{f.fee_status}</span></td>
                        <td>{f.fee_updated_timestamp ? new Date(f.fee_updated_timestamp).toLocaleDateString() : '—'}</td>
                        <td>
                          <div className="btn-group">
                            {f.fee_status !== 'paid' && <button className="btn btn-success btn-sm" onClick={() => handleFeeUpdate(f.semester_term, 'paid')}>Mark Paid</button>}
                            {f.fee_status !== 'pending' && <button className="btn btn-warning btn-sm" onClick={() => handleFeeUpdate(f.semester_term, 'pending')}>Mark Due</button>}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
