import api from './axiosInstance'

export const orderApi = {
  checkout: (data) => api.post('/orders/checkout', data),
  getHistory: (params) => api.get('/orders/history', { params }),
  trackOrder: (code, phone) => api.get('/orders/track', { params: { code, phone } }),
  cancelOrder: (orderId) => api.patch(`/orders/${orderId}/cancel`),
  createReview: (orderId, data) => api.post(`/orders/${orderId}/reviews`, data),
  validateCoupon: (code, orderAmount) => api.get(`/coupons/${code}/validate`, { params: { orderAmount } }),
}
