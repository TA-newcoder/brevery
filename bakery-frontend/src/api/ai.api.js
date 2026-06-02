import api from './axiosInstance'

export const aiApi = {
  chat: (message) => api.post('/ai/chat', { message }),
}
