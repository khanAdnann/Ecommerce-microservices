import api from './api';

const orderService = {
  getUserOrders: async (params = {}) => {
    const response = await api.get('/api/orders/my', { params });
    return response.data;
  },

  getOrderById: async (id) => {
    const response = await api.get(`/api/orders/${id}`);
    return response.data;
  },

  createOrder: async (orderData) => {
    const response = await api.post('/api/orders', orderData);
    return response.data;
  },

  updateOrderStatus: async (id, statusData) => {
    const response = await api.put(`/api/orders/${id}/status`, statusData);
    return response.data;
  },

  cancelOrder: async (id, reason) => {
    const response = await api.post(`/api/orders/${id}/cancel`, null, {
      params: { reason }
    });
    return response.data;
  },

  getAllOrders: async (params = {}) => {
    const response = await api.get('/api/orders', { params });
    return response.data;
  },

  searchOrders: async (searchParams) => {
    const response = await api.post('/api/orders/search', searchParams);
    return response.data;
  }
};

export default orderService;
