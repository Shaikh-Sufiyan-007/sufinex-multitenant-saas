import api from './axios';

export const dashboardApi = {
  // Fetch role-tailored dashboard statistics
  getDashboardStats: async () => {
    const response = await api.get('/dashboard/stats');
    return response.data;
  },
};
