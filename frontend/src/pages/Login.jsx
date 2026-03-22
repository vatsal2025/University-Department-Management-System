import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { login } from '../api/api';

const ROLES = [
  { key: 'student', label: 'Student' },
  { key: 'secretary', label: 'Dept. Secretary' },
  { key: 'finance_officer', label: 'Finance Officer' },
];

export default function Login() {
  const [role, setRole] = useState('student');
  const [id, setId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { loginUser } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!id.trim()) { setError('ID is required.'); return; }
    setError(''); setLoading(true);
    try {
      const res = await login(role, id.trim(), password);
      loginUser(res.data);
      navigate(`/${res.data.role}`);
    } catch (err) {
      const msg = err.response?.data?.error || 'Login failed. Please try again.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-box">
        <div className="login-title">UDIIMS</div>
        <div className="login-subtitle">University Department Information Management System</div>

        <div className="role-tabs">
          {ROLES.map(r => (
            <div key={r.key} className={`role-tab${role === r.key ? ' active' : ''}`}
              onClick={() => { setRole(r.key); setError(''); setId(''); setPassword(''); }}>
              {r.label}
            </div>
          ))}
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>{role === 'student' ? 'Roll Number' : role === 'secretary' ? 'Secretary ID' : 'Officer ID'}</label>
            <input type="text" value={id} onChange={e => setId(e.target.value)}
              placeholder={role === 'student' ? 'e.g. S001' : role === 'secretary' ? 'e.g. SEC001' : 'e.g. FIN001'}
              autoFocus />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)}
              placeholder="Enter password" />
          </div>
          <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <div style={{ marginTop: 24, padding: 14, background: '#f8fafc', borderRadius: 8, fontSize: '0.8rem', color: '#64748b' }}>
          <strong>Demo credentials:</strong><br />
          Student: S001 / pass123<br />
          Secretary: SEC001 / sec123 (CSE dept)<br />
          Finance: FIN001 / fin123
        </div>
      </div>
    </div>
  );
}
