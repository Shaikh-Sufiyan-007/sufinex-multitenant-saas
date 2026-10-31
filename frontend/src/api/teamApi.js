import api from './axios';

// ── Teams ──────────────────────────────────────────────────────────────────

export const getTeams = (params) =>
  api.get('/teams', { params });

export const getTeamById = (id) =>
  api.get(`/teams/${id}`);

export const createTeam = (data) =>
  api.post('/teams', data);

export const updateTeam = (id, data) =>
  api.put(`/teams/${id}`, data);

export const deleteTeam = (id) =>
  api.delete(`/teams/${id}`);

// ── Team Members ────────────────────────────────────────────────────────────

export const getTeamMembers = (teamId) =>
  api.get(`/teams/${teamId}/members`);

export const addTeamMember = (teamId, data) =>
  api.post(`/teams/${teamId}/members`, data);

export const removeTeamMember = (teamId, userId) =>
  api.delete(`/teams/${teamId}/members/${userId}`);

export const teamApi = {
  getTeams,
  getTeamById,
  createTeam,
  updateTeam,
  deleteTeam,
  getTeamMembers,
  addTeamMember,
  removeTeamMember,
};

export default teamApi;
