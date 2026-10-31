import api from './axios';

// ── Tasks ───────────────────────────────────────────────────────────────────

export const getProjectTasks = (projectId, params) =>
  api.get(`/projects/${projectId}/tasks`, { params });

export const createTask = (projectId, data) =>
  api.post(`/projects/${projectId}/tasks`, data);

export const getTaskById = (id) =>
  api.get(`/tasks/${id}`);

export const updateTask = (id, data) =>
  api.put(`/tasks/${id}`, data);

export const updateTaskStatus = (id, status) =>
  api.patch(`/tasks/${id}/status`, { status });

export const assignTask = (id, assignedToUserId) =>
  api.patch(`/tasks/${id}/assignee`, { assignedToUserId });

export const deleteTask = (id) =>
  api.delete(`/tasks/${id}`);

export const getMyTasks = (params) =>
  api.get('/tasks/my-tasks', { params });

export const taskApi = {
  getProjectTasks,
  createTask,
  getTaskById,
  updateTask,
  updateTaskStatus,
  assignTask,
  deleteTask,
  getMyTasks,
};

export default taskApi;
