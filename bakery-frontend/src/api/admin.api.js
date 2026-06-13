import api from './axiosInstance'

export const adminApi = {
  // Products
  createProduct: (formData) => api.post('/admin/products', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  updateProduct: (id, data) => api.put(`/admin/products/${id}`, data),
  toggleProduct: (id) => api.patch(`/admin/products/${id}/toggle`),
  updateStock: (id, data) => api.patch(`/admin/products/${id}/stock`, data),
  addVariant: (id, data) => api.post(`/admin/products/${id}/variants`, data),
  deleteProduct: (id) => api.delete(`/admin/products/${id}`),

  // Orders
  getOrders: (params) => api.get('/admin/orders', { params }),
  updateOrderStatus: (orderId, status) => api.patch(`/admin/orders/${orderId}/status?status=${status}`),
  exportOrders: (params) => api.get('/admin/orders/export', { params, responseType: 'blob' }),

  // Inventory
  getLowStock: (threshold = 10) => api.get('/admin/inventory/low-stock', { params: { threshold } }),
  getReceipts: (params) => api.get('/admin/inventory/receipts', { params }),
  createReceipt: (data) => api.post('/admin/inventory/receipts', data),

  // Reviews
  getReviews: (params) => api.get('/admin/reviews', { params }),
  updateReviewStatus: (reviewId, status) => api.patch(`/admin/reviews/${reviewId}/status?status=${status}`),
  replyToReview: (reviewId, reply) => api.post(`/admin/reviews/${reviewId}/reply?reply=${encodeURIComponent(reply)}`),

  // Users
  getUsers: (params) => api.get('/admin/users', { params }),
  toggleUser: (userId) => api.patch(`/admin/users/${userId}/toggle`),

  // Categories
  getCategories: () => api.get('/admin/categories'),
  createCategory: (data) => api.post('/admin/categories', data),
  updateCategory: (id, data) => api.put(`/admin/categories/${id}`, data),
  toggleCategory: (id) => api.patch(`/admin/categories/${id}/toggle`),
  deleteCategory: (id) => api.delete(`/admin/categories/${id}`),

  // Coupons
  getCoupons: () => api.get('/admin/coupons'),
  createCoupon: (data) => api.post('/admin/coupons', data),
  updateCoupon: (id, data) => api.put(`/admin/coupons/${id}`, data),
  toggleCoupon: (id) => api.patch(`/admin/coupons/${id}/toggle`),
  deleteCoupon: (id) => api.delete(`/admin/coupons/${id}`),

  // Banners
  getBanners: () => api.get('/admin/banners'),
  createBanner: (data) => api.post('/admin/banners', data),
  updateBanner: (id, data) => api.put(`/admin/banners/${id}`, data),
  toggleBanner: (id) => api.patch(`/admin/banners/${id}/toggle`),
  deleteBanner: (id) => api.delete(`/admin/banners/${id}`),

  // Analytics
  getSummary: () => api.get('/admin/analytics/summary'),
  getRevenueChart: (period) => api.get('/admin/analytics/revenue-chart', { params: { period } }),
  getTopProducts: (params) => api.get('/admin/analytics/top-products', { params }),
  getOrderStatusBreakdown: () => api.get('/admin/analytics/order-status-breakdown'),
  getAiInsight: () => api.get('/admin/analytics/insight'),
  triggerAiInsight: () => api.post('/admin/analytics/insight/trigger'),
}

