import api from './axios';

export const authApi = {
  // Register Tenant + Initial TENANT_ADMIN user
  register: async (registerData) => {
    const response = await api.post('/auth/register', registerData);
    return response.data;
  },

  // Login user (optionally with tenantSlug)
  login: async (credentials) => {
    const response = await api.post('/auth/login', credentials);
    return response.data;
  },

  // Logout session
  logout: async () => {
    const response = await api.post('/auth/logout');
    return response.data;
  },

  // Get current authenticated user profile
  getCurrentUser: async () => {
    const response = await api.get('/auth/me');
    return response.data;
  },

  // Get current tenant info (verified via TenantContext)
  getCurrentTenant: async () => {
    const response = await api.get('/tenants/current');
    return response.data;
  },
};
