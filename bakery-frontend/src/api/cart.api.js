import api from './axiosInstance'

export const cartApi = {
  getCart: () => api.get('/cart'),
  addToCart: (data) => api.post('/cart/add', data),
  updateCartItem: (cartItemId, data) => api.put(`/cart/${cartItemId}`, data),
  removeCartItem: (cartItemId) => api.delete(`/cart/${cartItemId}`),
  clearCart: () => api.delete('/cart/clear'),
}
