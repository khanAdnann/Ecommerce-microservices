import api from './api';

const cartService = {
  getCart: async () => {
    const response = await api.get('/api/cart');
    return response.data;
  },

  addItem: async (itemData) => {
    const response = await api.post('/api/cart/items', itemData);
    return response.data;
  },

  updateItem: async (itemId, itemData) => {
    const response = await api.put(`/api/cart/items/${itemId}`, itemData);
    return response.data;
  },

  removeItem: async (itemId) => {
    const response = await api.delete(`/api/cart/items/${itemId}`);
    return response.data;
  },

  clearCart: async () => {
    const response = await api.delete('/api/cart');
    return response.data;
  }
};

export default cartService;
