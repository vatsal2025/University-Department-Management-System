import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import FacultyManagement from './FacultyManagement';
import ProjectManagement from './ProjectManagement';
import InventoryManagement from './InventoryManagement';
import DepartmentAccounts from './DepartmentAccounts';
import CourseOfferings from './CourseOfferings';

const TABS = ['Faculty', 'Projects', 'Inventory', 'Dept. Accounts', 'Course Offerings'];

export default function SecretaryDashboard() {
  const { user, logoutUser } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState(0);
  const deptId = user.department_id;

  return (
    <div className="layout">
      <nav className="navbar">
        <span className="navbar-brand">UDIIMS — Department Portal</span>
        <div className="navbar-right">
          <span className="navbar-user">{user.secretary_name} | {deptId}</span>
          <button className="btn-logout" onClick={() => { logoutUser(); navigate('/'); }}>Logout</button>
        </div>
      </nav>
      <div className="tabs">
        {TABS.map((t, i) => (
          <div key={i} className={`tab${activeTab === i ? ' active' : ''}`} onClick={() => setActiveTab(i)}>{t}</div>
        ))}
      </div>
      <div className="content">
        {activeTab === 0 && <FacultyManagement departmentId={deptId} />}
        {activeTab === 1 && <ProjectManagement departmentId={deptId} />}
        {activeTab === 2 && <InventoryManagement departmentId={deptId} isTechnical={true} />}
        {activeTab === 3 && <DepartmentAccounts departmentId={deptId} />}
        {activeTab === 4 && <CourseOfferings departmentId={deptId} />}
      </div>
    </div>
  );
}
