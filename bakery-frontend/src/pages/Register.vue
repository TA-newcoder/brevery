<template>
  <div class="auth-split">
    <!-- BRAND PANEL -->
    <div class="auth-brand d-none d-lg-flex flex-column justify-content-between">
      <div class="brand-blob blob-a"></div>
      <div class="brand-blob blob-b"></div>
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
        <router-link to="/" class="d-lg-none d-inline-block mb-4 brand-logo-sm gradient-text">Brevery</router-link>
        <h3 class="fw-bold mb-1">Đăng ký</h3>
        <p class="text-sub mb-4">Tạo tài khoản Brevery miễn phí</p>

        <form @submit.prevent="handleRegister">
          <div class="mb-3">
            <label class="form-label small fw-semibold">Họ tên</label>
            <div class="input-icon-wrap">
              <PhUser class="field-icon" size="18" />
              <input v-model="form.fullName" type="text" class="bakery-input has-icon" placeholder="Nguyễn Văn A" required />
            </div>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">Email</label>
            <div class="input-icon-wrap">
              <PhEnvelope class="field-icon" size="18" />
              <input v-model="form.email" type="email" class="bakery-input has-icon" placeholder="email@example.com" required />
            </div>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">Mật khẩu</label>
            <div class="input-icon-wrap">
              <PhLock class="field-icon" size="18" />
              <input v-model="form.password" :type="showPassword ? 'text' : 'password'" class="bakery-input has-icon has-toggle" placeholder="Tối thiểu 8 ký tự" required minlength="8" />
              <button type="button" class="toggle-pass" @click="showPassword = !showPassword" :title="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'">
                <component :is="showPassword ? PhEyeSlash : PhEye" size="18" />
              </button>
            </div>
            <div v-if="form.password" class="pw-meter mt-2">
              <div class="pw-bar"><span :class="`lvl-${strength.level}`" :style="{ width: strength.pct + '%' }"></span></div>
              <small :class="`text-strength lvl-text-${strength.level}`">{{ strength.label }}</small>
            </div>
          </div>
          <div class="mb-4">
            <label class="form-label small fw-semibold">Số điện thoại</label>
            <div class="input-icon-wrap">
              <PhPhone class="field-icon" size="18" />
              <input v-model="form.phone" type="tel" class="bakery-input has-icon" placeholder="0909 123 456" />
            </div>
          </div>
          <button type="submit" class="btn btn-bakery w-100 py-2" :disabled="authStore.loading">
            {{ authStore.loading ? 'Đang xử lý...' : 'Đăng ký' }}
          </button>
        </form>
        <hr class="my-4" />
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
import { PhUser, PhEnvelope, PhLock, PhPhone, PhEye, PhEyeSlash, PhGift, PhStar, PhClock } from '@phosphor-icons/vue'

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
.auth-split { display: flex; min-height: 100vh; background: var(--bg-app); }
.auth-brand {
  flex: 0 0 44%;
  background: var(--gradient-primary);
  padding: 48px 56px;
  position: relative;
  overflow: hidden;
}
.brand-blob { position: absolute; border-radius: 50%; filter: blur(40px); }
.blob-a { width: 280px; height: 280px; background: rgba(255,255,255,.12); top: -60px; right: -40px; }
.blob-b { width: 220px; height: 220px; background: rgba(255,200,140,.18); bottom: -40px; left: -30px; }
.brand-logo { font-family: 'Playfair Display', serif; font-size: 2rem; font-weight: 800; letter-spacing: 2px; color: #fff; }
.brand-logo-sm { font-family: 'Playfair Display', serif; font-size: 1.8rem; font-weight: 800; letter-spacing: 1px; }
.brand-features li { color: rgba(255,255,255,.92); font-weight: 500; }

.auth-form-panel { flex: 1; padding: 40px 24px; }
.auth-card-inner { background: #fff; padding: 40px 36px; border-radius: var(--radius-card); box-shadow: var(--shadow-soft); }

.input-icon-wrap { position: relative; }
.field-icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: var(--text-sub); pointer-events: none; }
.bakery-input.has-icon { padding-left: 42px; }
.bakery-input.has-toggle { padding-right: 44px; }
.toggle-pass {
  position: absolute; right: 10px; top: 50%; transform: translateY(-50%);
  background: transparent; border: none; color: var(--text-sub);
  padding: 4px; cursor: pointer; display: flex; transition: color .15s;
}
.toggle-pass:hover { color: var(--primary); }

.pw-meter { display: flex; align-items: center; gap: 10px; }
.pw-bar { flex: 1; height: 6px; background: #EFE4DC; border-radius: 999px; overflow: hidden; }
.pw-bar span { display: block; height: 100%; border-radius: 999px; transition: width .3s ease, background .3s ease; }
.lvl-0, .lvl-1 { background: #E5747B; }
.lvl-2 { background: #E0A93B; }
.lvl-3 { background: #6FA86F; }
.lvl-4 { background: #4A9B5C; }
.text-strength { font-weight: 600; white-space: nowrap; }
.lvl-text-0, .lvl-text-1 { color: #C2616A; }
.lvl-text-2 { color: #B9851F; }
.lvl-text-3, .lvl-text-4 { color: #4A9B5C; }
</style>
