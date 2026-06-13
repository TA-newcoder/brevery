<template>
  <div class="auth-split">
    <!-- BRAND PANEL -->
    <div class="auth-brand d-none d-lg-flex flex-column justify-content-between">
      <div class="brand-blob blob-a"></div>
      <div class="brand-blob blob-b"></div>
      <div class="brand-blob blob-c"></div>
      <router-link to="/" class="brand-logo position-relative">Brevery</router-link>
      <div class="position-relative">
        <h2 class="fw-bold text-white mb-3">Gia nhập Brevery 🧁</h2>
        <p class="text-white-50 mb-4" style="max-width: 360px;">
          Tạo tài khoản miễn phí để mở khoá ưu đãi thành viên, tích điểm và đặt bánh nhanh hơn.
        </p>
        <ul class="list-unstyled d-flex flex-column gap-3 brand-features">
          <li class="d-flex align-items-center gap-3"><PhGift size="22" weight="duotone" /> Giảm 10% cho đơn đầu tiên</li>
          <li class="d-flex align-items-center gap-3"><PhStar size="22" weight="duotone" /> Tích điểm đổi quà mỗi đơn</li>
          <li class="d-flex align-items-center gap-3"><PhClock size="22" weight="duotone" /> Lưu địa chỉ, đặt lại chỉ 1 chạm</li>
        </ul>
      </div>
      <small class="text-white-50 position-relative">© 2026 Brevery · Bakery &amp; Beverage</small>
    </div>

    <!-- FORM PANEL -->
    <div class="auth-form-panel d-flex align-items-center justify-content-center">
      <div class="auth-card-inner w-100" style="max-width: 440px;">
        <!-- Back Button -->
        <router-link to="/" class="back-btn d-inline-flex align-items-center gap-2 mb-4">
          <PhArrowLeft size="18" weight="bold" />
          <span>Trang chủ</span>
        </router-link>

        <div class="text-center mb-5">
          <router-link to="/" class="d-lg-none d-inline-block mb-3 brand-logo-sm gradient-text">Brevery</router-link>
          <h3 class="fw-bold mb-2">Đăng ký</h3>
          <p class="text-sub mb-0">Tạo tài khoản Brevery miễn phí</p>
        </div>

        <form @submit.prevent="handleRegister">
          <div class="mb-4">
            <label class="form-label small fw-semibold text-main">Họ tên</label>
            <div class="input-icon-wrap">
              <PhUser class="field-icon" size="20" />
              <input v-model="form.fullName" type="text" class="bakery-input has-icon form-control-lg fs-6" placeholder="Nguyễn Văn A" required />
            </div>
          </div>
          <div class="mb-4">
            <label class="form-label small fw-semibold text-main">Email</label>
            <div class="input-icon-wrap">
              <PhEnvelope class="field-icon" size="20" />
              <input v-model="form.email" type="email" class="bakery-input has-icon form-control-lg fs-6" placeholder="email@example.com" required />
            </div>
          </div>
          <div class="mb-4">
            <label class="form-label small fw-semibold text-main">Số điện thoại</label>
            <div class="input-icon-wrap">
              <PhPhone class="field-icon" size="20" />
              <input v-model="form.phone" type="tel" class="bakery-input has-icon form-control-lg fs-6" placeholder="0909 123 456" />
            </div>
          </div>
          <div class="mb-4">
            <label class="form-label small fw-semibold text-main">Mật khẩu</label>
            <div class="input-icon-wrap">
              <PhLock class="field-icon" size="20" />
              <input v-model="form.password" :type="showPassword ? 'text' : 'password'" class="bakery-input has-icon has-toggle form-control-lg fs-6" placeholder="Tối thiểu 8 ký tự" required minlength="8" />
              <button type="button" class="toggle-pass" @click="showPassword = !showPassword" :title="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'">
                <component :is="showPassword ? PhEyeSlash : PhEye" size="20" />
              </button>
            </div>
            <div v-if="form.password" class="pw-meter mt-2">
              <div class="pw-bar"><span :class="`lvl-${strength.level}`" :style="{ transform: `scaleX(${strength.pct / 100})` }"></span></div>
              <small :class="`text-strength lvl-text-${strength.level}`">{{ strength.label }}</small>
            </div>
          </div>
          <button type="submit" class="btn btn-bakery w-100 py-3 auth-submit-btn fw-bold mt-2" :disabled="authStore.loading">
            <span v-if="authStore.loading" class="d-flex align-items-center justify-content-center gap-2">
              <span class="spinner"></span> Đang xử lý...
            </span>
            <span v-else>Đăng ký ngay</span>
          </button>
        </form>
        <div class="auth-divider my-4">
          <span>hoặc</span>
        </div>
        <p class="text-center text-sub mb-0 small">
          Đã có tài khoản? <router-link to="/login" class="text-bakery fw-semibold">Đăng nhập</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { PhUser, PhEnvelope, PhLock, PhPhone, PhEye, PhEyeSlash, PhGift, PhStar, PhClock, PhArrowLeft } from '@phosphor-icons/vue'

const authStore = useAuthStore()
const router = useRouter()
const form = reactive({ fullName: '', email: '', password: '', phone: '' })
const showPassword = ref(false)

const strength = computed(() => {
  const p = form.password || ''
  let score = 0
  if (p.length >= 8) score++
  if (/[A-Z]/.test(p)) score++
  if (/[0-9]/.test(p)) score++
  if (/[^A-Za-z0-9]/.test(p)) score++
  const levels = [
    { level: 0, label: 'Quá yếu', pct: 12 },
    { level: 1, label: 'Yếu', pct: 33 },
    { level: 2, label: 'Trung bình', pct: 60 },
    { level: 3, label: 'Khá mạnh', pct: 82 },
    { level: 4, label: 'Rất mạnh', pct: 100 },
  ]
  return levels[score]
})

async function handleRegister() {
  try {
    await authStore.register(form)
    router.push({ name: 'login' })
  } catch { /* toast already shown */ }
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
.brand-features li {
  color: rgba(255,255,255,.92);
  font-weight: 500;
  transition: transform 0.3s var(--ease-smooth);
}
.brand-features li:hover {
  transform: translateX(4px);
}

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

/* PASSWORD METER */
.pw-meter { display: flex; align-items: center; gap: 10px; }
.pw-bar { flex: 1; height: 6px; background: var(--bg-muted); border-radius: 999px; overflow: hidden; transition: background-color var(--duration-slow) var(--ease-smooth); }
.pw-bar span { display: block; height: 100%; border-radius: 999px; transition: transform .35s var(--ease-spring), background .35s var(--ease-smooth); transform-origin: left; }
.lvl-0, .lvl-1 { background: #E5747B; }
.lvl-2 { background: #E0A93B; }
.lvl-3 { background: #6FA86F; }
.lvl-4 { background: #4A9B5C; }
.text-strength { font-weight: 600; white-space: nowrap; }
.lvl-text-0, .lvl-text-1 { color: #C2616A; }
.lvl-text-2 { color: #B9851F; }
.lvl-text-3, .lvl-text-4 { color: #4A9B5C; }

/* DIVIDER */
.auth-divider {
  display: flex;
  align-items: center;
  gap: 16px;
  color: var(--text-muted);
  font-size: 0.82rem;
}
.auth-divider::before,
.auth-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border-color);
}
</style>
