<template>
  <div class="auth-page d-flex align-items-center justify-content-center min-vh-100">
    <div class="auth-card bakery-card" style="width: 420px">
      <div class="text-center mb-4">
        <span style="font-size: 2.5rem">🔑</span>
        <h3 class="fw-bold mt-2 mb-1">Quên mật khẩu</h3>
        <p class="text-sub">Nhập email để nhận link đặt lại mật khẩu</p>
      </div>
      <form @submit.prevent="handleSubmit">
        <div class="mb-4">
          <label class="form-label small fw-semibold">Email</label>
          <input v-model="email" type="email" class="bakery-input" placeholder="email@example.com" required />
        </div>
        <button type="submit" class="btn btn-bakery w-100 py-2" :disabled="loading">
          {{ loading ? 'Đang gửi...' : 'Gửi link đặt lại' }}
        </button>
      </form>
      <div v-if="sent" class="alert mt-3 text-center" style="background: var(--bakery-green-light); color: #4A9B5C; border-radius: var(--radius-btn); border: none">
        ✅ Đã gửi email! Kiểm tra hộp thư của bạn.
      </div>
      <hr />
      <p class="text-center text-sub mb-0 small">
        <router-link to="/login" class="text-bakery fw-semibold">← Quay lại đăng nhập</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { authApi } from '@/api/auth.api'
import { toast } from 'vue3-toastify'

const email = ref('')
const loading = ref(false)
const sent = ref(false)

async function handleSubmit() {
  loading.value = true
  try {
    await authApi.forgotPassword({ email: email.value })
    sent.value = true
  } catch {
    toast.info('Nếu email tồn tại, bạn sẽ nhận được link đặt lại mật khẩu.')
    sent.value = true
  } finally { loading.value = false }
}
</script>

<style scoped>
.auth-page { background: linear-gradient(135deg, var(--bakery-purple-light), var(--bakery-primary-light)); }
.auth-card { padding: 40px 36px; }
</style>
