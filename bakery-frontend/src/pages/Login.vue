<template>
  <div class="auth-split">
    <!-- BRAND PANEL -->
    <div class="auth-brand d-none d-lg-flex flex-column justify-content-between">
      <div class="brand-blob blob-a"></div>
      <div class="brand-blob blob-b"></div>
      <router-link to="/" class="brand-logo position-relative">Brevery</router-link>
      <div class="position-relative">
        <h2 class="fw-bold text-white mb-3">Chào mừng trở lại 🍰</h2>
        <p class="text-white-50 mb-4" style="max-width: 360px;">
          Đăng nhập để theo dõi đơn hàng, lưu sản phẩm yêu thích và nhận ưu đãi độc quyền mỗi tuần.
        </p>
        <ul class="list-unstyled d-flex flex-column gap-3 brand-features">
          <li class="d-flex align-items-center gap-3"><PhMoped size="22" weight="duotone" /> Giao hàng trong 30 phút</li>
          <li class="d-flex align-items-center gap-3"><PhLeaf size="22" weight="duotone" /> Nguyên liệu tươi mỗi sáng</li>
          <li class="d-flex align-items-center gap-3"><PhShieldCheck size="22" weight="duotone" /> Hoàn tiền 100% nếu chưa hài lòng</li>
        </ul>
      </div>
      <small class="text-white-50 position-relative">© 2026 Brevery · Bakery &amp; Beverage</small>
    </div>

    <!-- FORM PANEL -->
    <div class="auth-form-panel d-flex align-items-center justify-content-center">
      <div class="auth-card-inner w-100" style="max-width: 420px;">
        <router-link to="/" class="d-lg-none d-inline-block mb-4 brand-logo-sm gradient-text">Brevery</router-link>
        <h3 class="fw-bold mb-1">Đăng nhập</h3>
        <p class="text-sub mb-4">Nhập thông tin để tiếp tục</p>

        <form @submit.prevent="handleLogin">
          <div class="mb-3">
            <label class="form-label small fw-semibold">Email</label>
            <div class="input-icon-wrap">
              <PhEnvelope class="field-icon" size="18" />
              <input v-model="form.email" type="email" class="bakery-input has-icon" placeholder="email@example.com" required />
            </div>
          </div>
          <div class="mb-4">
            <label class="form-label small fw-semibold">Mật khẩu</label>
            <div class="input-icon-wrap">
              <PhLock class="field-icon" size="18" />
              <input v-model="form.password" :type="showPassword ? 'text' : 'password'" class="bakery-input has-icon has-toggle" placeholder="••••••••" required />
              <button type="button" class="toggle-pass" @click="showPassword = !showPassword" :title="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'">
                <component :is="showPassword ? PhEyeSlash : PhEye" size="18" />
              </button>
            </div>
          </div>
          <button type="submit" class="btn btn-bakery w-100 py-2" :disabled="authStore.loading">
            {{ authStore.loading ? 'Đang xử lý...' : 'Đăng nhập' }}
          </button>
        </form>

        <div class="text-center mt-3">
          <router-link to="/forgot-password" class="text-bakery small fw-semibold">Quên mật khẩu?</router-link>
        </div>
        <hr class="my-4" />
        <p class="text-center text-sub mb-0 small">
          Chưa có tài khoản? <router-link to="/register" class="text-bakery fw-semibold">Đăng ký ngay</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'
import { PhEnvelope, PhLock, PhEye, PhEyeSlash, PhMoped, PhLeaf, PhShieldCheck } from '@phosphor-icons/vue'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()
const route = useRoute()
const form = reactive({ email: '', password: '' })
const showPassword = ref(false)

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
.brand-logo {
  font-family: 'Playfair Display', serif;
  font-size: 2rem; font-weight: 800; letter-spacing: 2px; color: #fff;
}
.brand-logo-sm { font-family: 'Playfair Display', serif; font-size: 1.8rem; font-weight: 800; letter-spacing: 1px; }
.brand-features li { color: rgba(255,255,255,.92); font-weight: 500; }

.auth-form-panel { flex: 1; padding: 40px 24px; }
.auth-card-inner {
  background: #fff;
  padding: 40px 36px;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-soft);
}
@media (max-width: 991px) {
  .auth-card-inner { box-shadow: var(--shadow-card); }
}

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
</style>
