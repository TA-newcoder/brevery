import api from './axiosInstance'

export const productApi = {
  getProducts: (params) => api.get('/products', { params }),
  getProductDetail: (id) => api.get(`/products/${id}`),
  getReviews: (id, params) => api.get(`/products/${id}/reviews`, { params }),
  submitReview: (data) => api.post('/reviews', data),
  getFrequentlyBought: (id, limit = 4) => api.get(`/products/recommendations/frequent/${id}`, { params: { limit } }),
  getPersonalized: (limit = 8) => api.get('/products/recommendations/personal', { params: { limit } }),
  getTopSelling: (limit = 8) => api.get('/products/recommendations/top', { params: { limit } }),
  getCategories: () => api.get('/categories'),
  getBanners: () => api.get('/banners'),
}

export const contactApi = {
  submit: (data) => api.post('/contact', data),
}

