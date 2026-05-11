import api from './api';

const authService = {
  login: async (credentials) => {
    const response = await api.post('/api/users/login', credentials);
    return response.data;
  },

  register: async (userData) => {
    const response = await api.post('/api/users/register', userData);
    return response.data;
  },

  refreshToken: async (tokenData) => {
    const response = await api.post('/api/users/refresh', tokenData);
    return response.data;
  },

  getCurrentUser: async () => {
    const response = await api.get('/api/users/me');
    return response.data;
  },

  forgotPassword: async (email) => {
    const response = await api.post('/api/users/forgot-password', { email });
    return response.data;
  },

  resetPassword: async (token, newPassword) => {
    const response = await api.post('/api/users/reset-password', { token, newPassword });
    return response.data;
  },

  verifyEmail: async (token) => {
    const response = await api.post('/api/users/verify-email', { token });
    return response.data;
  },

  changePassword: async (passwordData) => {
    const response = await api.post('/api/users/change-password', passwordData);
    return response.data;
  },

  updateProfile: async (profileData) => {
    const response = await api.put('/api/users/profile', profileData);
    return response.data;
  },
};

export default authService;
