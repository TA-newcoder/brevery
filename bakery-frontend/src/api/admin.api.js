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
  updateOrderStatus: (orderId, status) => api.patch(`/admin/orders/${orderId}/status`, null, { params: { newStatus: status } }),
  exportOrders: (params) => api.get('/admin/orders/export', { params, responseType: 'blob' }),

  // Users
  getUsers: (params) => api.get('/admin/users', { params }),
  toggleUser: (userId) => api.patch(`/admin/users/${userId}/toggle`),

  // Analytics
  getSummary: () => api.get('/admin/analytics/summary'),
  getRevenueChart: (period) => api.get('/admin/analytics/revenue-chart', { params: { period } }),
  getTopProducts: (params) => api.get('/admin/analytics/top-products', { params }),
  getOrderStatusBreakdown: () => api.get('/admin/analytics/order-status-breakdown'),
  getAiInsight: () => api.get('/admin/analytics/insight'),
  triggerAiInsight: () => api.post('/admin/analytics/insight/trigger'),
}
