<template>
  <div class="auth-split">
    <!-- BRAND PANEL -->
    <div class="auth-brand d-none d-lg-flex flex-column justify-content-between">
      <div class="brand-blob blob-a"></div>
      <div class="brand-blob blob-b"></div>
      <div class="brand-blob blob-c"></div>
      <router-link to="/" class="brand-logo position-relative">Brevery</router-link>
      <div class="position-relative">
        <h2 class="fw-bold text-white mb-3">Tạo mật khẩu mới 🔒</h2>
        <p class="text-white-50 mb-4" style="max-width: 360px;">
          Bảo mật hơn với mật khẩu mới. Hãy chắc chắn bạn không sử dụng lại mật khẩu cũ nhé.
        </p>
      </div>
      <small class="text-white-50 position-relative">© 2026 Brevery · Bakery &amp; Beverage</small>
    </div>

    <!-- FORM PANEL -->
    <div class="auth-form-panel d-flex align-items-center justify-content-center">
      <div class="auth-card-inner w-100" style="max-width: 420px;">
        <!-- Back Button -->
        <router-link to="/login" class="back-btn d-inline-flex align-items-center gap-2 mb-4">
          <PhArrowLeft size="18" weight="bold" />
          <span>Quay lại đăng nhập</span>
        </router-link>

        <div class="text-center mb-5">
          <router-link to="/" class="d-lg-none d-inline-block mb-3 brand-logo-sm gradient-text">Brevery</router-link>
          <h3 class="fw-bold mb-2">Mật khẩu mới</h3>
          <p class="text-sub mb-0">Thiết lập lại mật khẩu cho tài khoản của bạn</p>
        </div>

        <form v-if="!success" @submit.prevent="handleSubmit">
          <div class="mb-4">
            <label class="form-label small fw-semibold text-main">Mật khẩu mới</label>
            <div class="input-icon-wrap">
              <PhLockKey class="field-icon" size="20" />
              <input v-model="newPassword" :type="showNewPassword ? 'text' : 'password'" class="bakery-input has-icon has-toggle form-control-lg fs-6" placeholder="Nhập mật khẩu mới" required minlength="6" />
              <button type="button" class="toggle-pass" @click="showNewPassword = !showNewPassword" :title="showNewPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'">
                <component :is="showNewPassword ? PhEyeSlash : PhEye" size="20" />
              </button>
            </div>
          </div>
          
          <div class="mb-4">
            <label class="form-label small fw-semibold text-main">Xác nhận mật khẩu</label>
            <div class="input-icon-wrap">
              <PhLockKey class="field-icon" size="20" />
              <input v-model="confirmPassword" :type="showConfirmPassword ? 'text' : 'password'" class="bakery-input has-icon has-toggle form-control-lg fs-6" placeholder="Nhập lại mật khẩu" required />
              <button type="button" class="toggle-pass" @click="showConfirmPassword = !showConfirmPassword" :title="showConfirmPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'">
                <component :is="showConfirmPassword ? PhEyeSlash : PhEye" size="20" />
              </button>
            </div>
            <small v-if="errorMsg" class="text-danger mt-1 d-block">{{ errorMsg }}</small>
          </div>
          
          <button type="submit" class="btn btn-bakery w-100 py-3 auth-submit-btn fw-bold mt-2" :disabled="loading">
            <span v-if="loading" class="d-flex align-items-center justify-content-center gap-2">
              <span class="spinner"></span> Đang xử lý...
            </span>
            <span v-else>Xác nhận đặt lại</span>
          </button>
        </form>

        <div v-else class="text-center">
          <div class="success-alert mb-4 d-inline-flex align-items-center gap-2">
            <PhCheckCircle size="24" weight="fill" /> Mật khẩu đã được đặt lại thành công!
          </div>
          <router-link to="/login" class="btn btn-bakery w-100 py-3 fw-bold">
            Đến trang đăng nhập
          </router-link>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authApi } from '@/api/auth.api'
import { toast } from 'vue3-toastify'
import { PhLockKey, PhCheckCircle, PhArrowLeft, PhEye, PhEyeSlash } from '@phosphor-icons/vue'

const route = useRoute()
const router = useRouter()

const token = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const success = ref(false)
const errorMsg = ref('')
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

onMounted(() => {
  token.value = route.query.token
  if (!token.value) {
    toast.error('Đường dẫn không hợp lệ hoặc thiếu Token!')
    router.push('/login')
  }
})

async function handleSubmit() {
  errorMsg.value = ''
  
  if (newPassword.value !== confirmPassword.value) {
    errorMsg.value = 'Mật khẩu xác nhận không khớp!'
    return
  }
  
  loading.value = true
  try {
    await authApi.resetPassword({ 
      token: token.value, 
      newPassword: newPassword.value 
    })
    success.value = true
    toast.success('Đặt lại mật khẩu thành công!')
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Link đã hết hạn hoặc không hợp lệ.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-split {
  display: flex;
  min-height: 100vh;
  background: var(--bg-app);
  transition: background-color var(--duration-slow) var(--ease-smooth);
}

/* BRAND PANEL */
.auth-brand {
  flex: 0 0 44%;
  background: var(--gradient-primary);
  padding: 48px 56px;
  position: relative;
  overflow: hidden;
}
.brand-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(50px);
  animation: blob-float 12s ease-in-out infinite;
}
.blob-a { width: 280px; height: 280px; background: rgba(255,255,255,.12); top: -60px; right: -40px; animation-delay: 0s; }
.blob-b { width: 220px; height: 220px; background: rgba(255,200,140,.18); bottom: -40px; left: -30px; animation-delay: -4s; }
.blob-c { width: 180px; height: 180px; background: rgba(255,255,255,.08); top: 50%; left: 40%; animation-delay: -8s; }

@keyframes blob-float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(15px, -20px) scale(1.05); }
  66% { transform: translate(-10px, 15px) scale(0.95); }
}

.brand-logo {
  font-family: 'Playfair Display', serif;
  font-size: 2rem; font-weight: 800; letter-spacing: 2px; color: #fff;
  text-decoration: none;
  transition: opacity 0.3s var(--ease-smooth);
}
.brand-logo:hover { opacity: 0.85; }
.brand-logo-sm { font-family: 'Playfair Display', serif; font-size: 1.8rem; font-weight: 800; letter-spacing: 1px; }

/* FORM PANEL */
.auth-form-panel {
  flex: 1;
  padding: 40px 24px;
  transition: background-color var(--duration-slow) var(--ease-smooth);
}
.auth-card-inner {
  background: var(--bg-surface);
  padding: 48px 40px;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-glass);
  border: 1px solid var(--border-light);
  transition: background-color var(--duration-slow) var(--ease-smooth),
              box-shadow var(--duration-slow) var(--ease-smooth),
              border-color var(--duration-slow) var(--ease-smooth);
}

/* BACK BUTTON */
.back-btn {
  color: var(--text-sub);
  font-size: 0.88rem;
  font-weight: 600;
  text-decoration: none;
  padding: 6px 14px 6px 10px;
  border-radius: var(--radius-btn);
  border: 1px solid var(--border-color);
  background: var(--bg-surface);
  transition: all 0.3s var(--ease-smooth);
}
.back-btn:hover {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light);
  transform: translateX(-2px);
}

/* INPUT */
.input-icon-wrap { position: relative; }
.field-icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: var(--text-sub); pointer-events: none; transition: color 0.25s var(--ease-smooth); }
.bakery-input.has-icon { padding-left: 42px; }
.bakery-input.has-toggle { padding-right: 44px; }
.toggle-pass {
  position: absolute; right: 10px; top: 50%; transform: translateY(-50%);
  background: transparent; border: none; color: var(--text-sub);
  padding: 4px; cursor: pointer; display: flex; transition: color .25s var(--ease-smooth);
}
.toggle-pass:hover { color: var(--primary); }

/* SUBMIT BUTTON */
.auth-submit-btn {
  position: relative;
  font-size: 1rem;
  letter-spacing: 0.02em;
}
.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* SUCCESS ALERT */
.success-alert {
  background: var(--accent-sage);
  color: var(--accent-sage-text);
  border-radius: var(--radius-input);
  border: none;
  padding: 12px 16px;
  font-weight: 500;
  font-size: 1.1rem;
}
</style>
