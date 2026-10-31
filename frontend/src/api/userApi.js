import api from './axios';

export const userApi = {
  // TENANT_ADMIN: List users inside own tenant (paginated & searchable)
  getTenantUsers: async (params = {}) => {
    const response = await api.get('/users', { params });
    return response.data;
  },

  // TENANT_ADMIN: Create a new user in own tenant
  createUser: async (data) => {
    const response = await api.post('/users', data);
    return response.data;
  },

  // TENANT_ADMIN: Update a user inside own tenant
  updateUser: async (id, data) => {
    const response = await api.put(`/users/${id}`, data);
    return response.data;
  },

  // TENANT_ADMIN: Activate or deactivate a user
  updateUserStatus: async (id, status) => {
    const response = await api.patch(`/users/${id}/status`, { status });
    return response.data;
  },

  // ANY USER: View own profile
  getMyProfile: async () => {
    const response = await api.get('/users/me');
    return response.data;
  },

  // ANY USER: Update own profile (name)
  updateMyProfile: async (data) => {
    const response = await api.put('/users/me', data);
    return response.data;
  },

  // ANY USER: Change password
  changePassword: async (data) => {
    const response = await api.put('/users/me/password', data);
    return response.data;
  },
};
