import api from './axios';

// ── Projects ────────────────────────────────────────────────────────────────

export const getProjects = (params) =>
  api.get('/projects', { params });

export const getProjectById = (id) =>
  api.get(`/projects/${id}`);

export const createProject = (data) =>
  api.post('/projects', data);

export const updateProject = (id, data) =>
  api.put(`/projects/${id}`, data);

export const updateProjectStatus = (id, status) =>
  api.patch(`/projects/${id}/status`, { status });

export const archiveProject = (id) =>
  api.delete(`/projects/${id}`);

// ── Project Members ─────────────────────────────────────────────────────────

export const getProjectMembers = (projectId) =>
  api.get(`/projects/${projectId}/members`);

export const addProjectMember = (projectId, data) =>
  api.post(`/projects/${projectId}/members`, data);

export const removeProjectMember = (projectId, userId) =>
  api.delete(`/projects/${projectId}/members/${userId}`);

// ── Project Dashboard ───────────────────────────────────────────────────────

export const getProjectDashboard = (projectId) =>
  api.get(`/projects/${projectId}/dashboard`);

export const projectApi = {
  getProjects,
  getProjectById,
  createProject,
  updateProject,
  updateProjectStatus,
  archiveProject,
  getProjectMembers,
  addProjectMember,
  removeProjectMember,
  getProjectDashboard,
};

export default projectApi;
