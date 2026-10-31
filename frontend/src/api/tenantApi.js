import api from './axios';

export const tenantApi = {
  // Get currently authenticated tenant profile
  getCurrentTenant: async () => {
    const response = await api.get('/tenant/me');
    return response.data;
  },

  // Update currently authenticated tenant profile (name only)
  updateCurrentTenant: async (data) => {
    const response = await api.put('/tenant/me', data);
    return response.data;
  },
};
