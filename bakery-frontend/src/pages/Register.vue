<template>
  <div class="auth-page d-flex align-items-center justify-content-center min-vh-100">
    <div class="auth-card bakery-card" style="width: 420px">
      <div class="text-center mb-4">
        <span style="font-size: 2.5rem">🍰</span>
        <h3 class="fw-bold mt-2 mb-1">Đăng ký</h3>
        <p class="text-sub">Tạo tài khoản Brevery miễn phí</p>
      </div>
      <form @submit.prevent="handleRegister">
        <div class="mb-3">
          <label class="form-label small fw-semibold">Họ tên</label>
          <input v-model="form.fullName" type="text" class="bakery-input" placeholder="Nguyễn Văn A" required />
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">Email</label>
          <input v-model="form.email" type="email" class="bakery-input" placeholder="email@example.com" required />
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">Mật khẩu</label>
          <input v-model="form.password" type="password" class="bakery-input" placeholder="Tối thiểu 8 ký tự" required minlength="8" />
        </div>
        <div class="mb-4">
          <label class="form-label small fw-semibold">Số điện thoại</label>
          <input v-model="form.phone" type="tel" class="bakery-input" placeholder="0909 123 456" />
        </div>
        <button type="submit" class="btn btn-bakery w-100 py-2" :disabled="authStore.loading">
          {{ authStore.loading ? 'Đang xử lý...' : 'Đăng ký' }}
        </button>
      </form>
      <hr />
      <p class="text-center text-sub mb-0 small">
        Đã có tài khoản? <router-link to="/login" class="text-bakery fw-semibold">Đăng nhập</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const authStore = useAuthStore()
const router = useRouter()
const form = reactive({ fullName: '', email: '', password: '', phone: '' })

async function handleRegister() {
  try {
    await authStore.register(form)
    router.push({ name: 'login' })
  } catch { /* toast already shown */ }
}
</script>

<style scoped>
.auth-page { background: linear-gradient(135deg, var(--bakery-accent-light), var(--bakery-purple-light)); }
.auth-card { padding: 40px 36px; }
</style>
