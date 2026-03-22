import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import GrantManagement from './GrantManagement';
import ConsultancyManagement from './ConsultancyManagement';
import FeeCollection from './FeeCollection';
import ProjectFinance from './ProjectFinance';

const TABS = ['Grant Management', 'Consultancy Funds', 'Fee Collection', 'Project Finance'];

export default function FinanceDashboard() {
  const { user, logoutUser } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState(0);

  return (
    <div className="layout">
      <nav className="navbar">
        <span className="navbar-brand">UDIIMS — Finance Section</span>
        <div className="navbar-right">
          <span className="navbar-user">{user.officer_name} ({user.officer_id})</span>
          <button className="btn-logout" onClick={() => { logoutUser(); navigate('/'); }}>Logout</button>
        </div>
      </nav>
      <div className="tabs">
        {TABS.map((t, i) => (
          <div key={i} className={`tab${activeTab === i ? ' active' : ''}`} onClick={() => setActiveTab(i)}>{t}</div>
        ))}
      </div>
      <div className="content">
        {activeTab === 0 && <GrantManagement />}
        {activeTab === 1 && <ConsultancyManagement />}
        {activeTab === 2 && <FeeCollection />}
        {activeTab === 3 && <ProjectFinance />}
      </div>
    </div>
  );
}
