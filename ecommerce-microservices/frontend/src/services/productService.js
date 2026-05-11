import api from './api';

const productService = {
  getProducts: async (params = {}) => {
    const response = await api.get('/api/products', { params });
    return response.data;
  },

  getProductById: async (id) => {
    const response = await api.get(`/api/products/${id}`);
    return response.data;
  },

  getProductBySku: async (sku) => {
    const response = await api.get(`/api/products/sku/${sku}`);
    return response.data;
  },

  getActiveProducts: async (params = {}) => {
    const response = await api.get('/api/products/active', { params });
    return response.data;
  },

  getFeaturedProducts: async (params = {}) => {
    const response = await api.get('/api/products/featured', { params });
    return response.data;
  },

  searchProducts: async (searchParams) => {
    const response = await api.post('/api/products/search', searchParams);
    return response.data;
  },

  getProductsByCategory: async (categoryId, params = {}) => {
    const response = await api.get(`/api/products/category/${categoryId}`, { params });
    return response.data;
  },

  getProductsByPriceRange: async (minPrice, maxPrice, params = {}) => {
    const response = await api.get('/api/products/price-range', {
      params: { minPrice, maxPrice, ...params }
    });
    return response.data;
  },

  getProductsByBrand: async (brand, params = {}) => {
    const response = await api.get(`/api/products/brand/${brand}`, { params });
    return response.data;
  },

  getTopRatedProducts: async (params = {}) => {
    const response = await api.get('/api/products/top-rated', { params });
    return response.data;
  },

  getLatestProducts: async (params = {}) => {
    const response = await api.get('/api/products/latest', { params });
    return response.data;
  },

  createProduct: async (productData) => {
    const response = await api.post('/api/products', productData);
    return response.data;
  },

  updateProduct: async (id, productData) => {
    const response = await api.put(`/api/products/${id}`, productData);
    return response.data;
  },

  updateProductStatus: async (id, status) => {
    const response = await api.put(`/api/products/${id}/status`, null, {
      params: { status }
    });
    return response.data;
  },

  deleteProduct: async (id) => {
    const response = await api.delete(`/api/products/${id}`);
    return response.data;
  },

  getDistinctBrands: async () => {
    const response = await api.get('/api/products/brands');
    return response.data;
  },

  getActiveProductCount: async () => {
    const response = await api.get('/api/products/count');
    return response.data;
  },

  getProductCountByCategory: async (categoryId) => {
    const response = await api.get(`/api/products/category/${categoryId}/count`);
    return response.data;
  }
};

export default productService;
