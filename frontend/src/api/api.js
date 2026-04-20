import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

// Auth
export const login = (role, id, password) =>
  api.post('/auth/login', { role, id, password });

// Student API (UC-01 to UC-05)
export const getStudent = (studentId) => api.get(`/students/${studentId}`);
export const getAvailableCourses = (semesterTerm, departmentId) =>
  api.get('/students/courses/available', { params: { semesterTerm, departmentId } });
export const getRegistrations = (studentId, semesterTerm) =>
  api.get(`/students/${studentId}/registrations`, { params: { semesterTerm } });
export const registerCourses = (studentId, semester_term, course_codes) =>
  api.post(`/students/${studentId}/registrations`, { semester_term, course_codes });
export const dropCourse = (studentId, courseCode, semesterTerm) =>
  api.delete(`/students/${studentId}/registrations/${courseCode}`, { params: { semesterTerm } });
export const calculateGpa = (studentId) => api.post(`/students/${studentId}/gpa/calculate`);
export const getGpa = (studentId) => api.get(`/students/${studentId}/gpa`);
export const getBacklogs = (studentId) => api.get(`/students/${studentId}/backlogs`);
export const getFees = (studentId) => api.get(`/students/${studentId}/fees`);

// Secretary API (UC-06 to UC-10)
export const getFaculty = (departmentId) => api.get('/secretary/faculty', { params: { departmentId } });
export const addFaculty = (body) => api.post('/secretary/faculty', body);
export const updateFaculty = (facultyId, body) => api.put(`/secretary/faculty/${facultyId}`, body);
export const deactivateFaculty = (facultyId) => api.delete(`/secretary/faculty/${facultyId}`);
export const forceDeactivateFaculty = (facultyId) => api.delete(`/secretary/faculty/${facultyId}/force`);

export const getProjects = (departmentId) => api.get('/secretary/projects', { params: { departmentId } });
export const addProject = (body) => api.post('/secretary/projects', body);
export const updateProject = (projectId, body) => api.put(`/secretary/projects/${projectId}`, body);

export const getInventory = (departmentId) => api.get('/secretary/inventory', { params: { departmentId } });
export const addInventoryItem = (body, departmentId, isTechnical) =>
  api.post('/secretary/inventory', body, { params: { departmentId, isTechnical } });
export const updateInventoryItem = (itemId, body) => api.put(`/secretary/inventory/${itemId}`, body);
export const disposeInventoryItem = (itemId) => api.delete(`/secretary/inventory/${itemId}`);

export const getDepartmentAccounts = (departmentId) =>
  api.get('/secretary/accounts', { params: { departmentId } });

export const getCourseOfferings = (departmentId, semesterTerm) =>
  api.get('/secretary/courses', { params: { departmentId, semesterTerm } });
export const addCourseOffering = (body) => api.post('/secretary/courses', body);
export const updateCourseOffering = (courseCode, semesterTerm, departmentId, body) =>
  api.put(`/secretary/courses/${courseCode}`, body, { params: { semesterTerm, departmentId } });
export const removeCourseOffering = (courseCode, semesterTerm, departmentId) =>
  api.delete(`/secretary/courses/${courseCode}`, { params: { semesterTerm, departmentId } });

// Finance API (UC-11 to UC-14)
export const getGrants = (departmentId) => api.get('/finance/grants', { params: { departmentId } });
export const recordGrant = (body) => api.post('/finance/grants', body);
export const getConsultancy = (departmentId) => api.get('/finance/consultancy', { params: { departmentId } });
export const recordConsultancy = (body) => api.post('/finance/consultancy', body);
export const getStudentFees = (studentId) => api.get(`/finance/fees/${studentId}`);
export const updateFeeStatus = (studentId, semester_term, fee_status) =>
  api.patch(`/finance/fees/${studentId}`, { semester_term, fee_status });
export const createFeeRecord = (body) => api.post('/finance/fees', body);
export const getProjectFinance = (departmentId) => api.get('/finance/project-finance', { params: { departmentId } });
export const recordProjectFinance = (body) => api.post('/finance/project-finance', body);
export const getFinanceProjects = (departmentId) => api.get('/finance/projects', { params: { departmentId } });

export default api;
