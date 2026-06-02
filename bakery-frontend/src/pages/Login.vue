<template>
  <div class="auth-page d-flex align-items-center justify-content-center min-vh-100">
    <div class="auth-card bakery-card" style="width: 420px">
      <div class="text-center mb-4">
        <span style="font-size: 2.5rem">🍰</span>
        <h3 class="fw-bold mt-2 mb-1">Đăng nhập</h3>
        <p class="text-sub">Chào mừng bạn trở lại Brevery</p>
      </div>
      <form @submit.prevent="handleLogin">
        <div class="mb-3">
          <label class="form-label small fw-semibold">Email</label>
          <input v-model="form.email" type="email" class="bakery-input" placeholder="email@example.com" required />
        </div>
        <div class="mb-4">
          <label class="form-label small fw-semibold">Mật khẩu</label>
          <input v-model="form.password" type="password" class="bakery-input" placeholder="••••••••" required />
        </div>
        <button type="submit" class="btn btn-bakery w-100 py-2" :disabled="authStore.loading">
          {{ authStore.loading ? 'Đang xử lý...' : 'Đăng nhập' }}
        </button>
      </form>
      <div class="text-center mt-3">
        <router-link to="/forgot-password" class="text-bakery small">Quên mật khẩu?</router-link>
      </div>
      <hr />
      <p class="text-center text-sub mb-0 small">
        Chưa có tài khoản? <router-link to="/register" class="text-bakery fw-semibold">Đăng ký ngay</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()
const route = useRoute()
const form = reactive({ email: '', password: '' })

async function handleLogin() {
  try {
    await authStore.login(form)
    await cartStore.fetchCart()
    const redirect = route.query.redirect || (authStore.isAdmin ? '/admin' : '/')
    router.push(redirect)
  } catch { /* toast already shown */ }
}
</script>

<style scoped>
.auth-page { background: linear-gradient(135deg, var(--bakery-primary-light), var(--bakery-accent-light)); }
.auth-card { padding: 40px 36px; }
</style>
