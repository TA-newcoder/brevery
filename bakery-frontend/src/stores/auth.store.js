import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth.api'
import { toast } from 'vue3-toastify'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const accessToken = ref(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => !!accessToken.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const userName = computed(() => user.value?.fullName || '')

  async function login(credentials) {
    loading.value = true
    try {
      const { data } = await authApi.login(credentials)
      accessToken.value = data.data.accessToken
      user.value = {
        userId: data.data.userId,
        email: data.data.email,
        fullName: data.data.fullName,
        role: data.data.role,
        avatarUrl: data.data.avatarUrl,
      }
      toast.success('Đăng nhập thành công!')
      return data
    } catch (err) {
      const msg = err.response?.data?.message || 'Đăng nhập thất bại'
      toast.error(msg)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function register(formData) {
    loading.value = true
    try {
      const { data } = await authApi.register(formData)
      toast.success(data.message || 'Đăng ký thành công! Kiểm tra email để xác thực.')
      return data
    } catch (err) {
      const msg = err.response?.data?.message || 'Đăng ký thất bại'
      toast.error(msg)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function refreshToken() {
    try {
      const { data } = await authApi.refresh()
      accessToken.value = data.data.accessToken
      user.value = {
        userId: data.data.userId,
        email: data.data.email,
        fullName: data.data.fullName,
        role: data.data.role,
        avatarUrl: data.data.avatarUrl,
      }
      return data.data.accessToken
    } catch (err) {
      logout()
      throw err
    }
  }

  async function fetchMe() {
    try {
      await refreshToken()
    } catch {
      // Not logged in — silent
    }
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // ignore
    }
    accessToken.value = null
    user.value = null
  }

  return {
    user, accessToken, loading,
    isAuthenticated, isAdmin, userName,
    login, register, refreshToken, fetchMe, logout,
  }
})
