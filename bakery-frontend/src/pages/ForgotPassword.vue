<template>
  <div class="auth-split">
    <!-- BRAND PANEL -->
    <div class="auth-brand d-none d-lg-flex flex-column justify-content-between">
      <div class="brand-blob blob-a"></div>
      <div class="brand-blob blob-b"></div>
      <router-link to="/" class="brand-logo position-relative">Brevery</router-link>
      <div class="position-relative">
        <h2 class="fw-bold text-white mb-3">Đừng lo, chúng tôi giúp 🔑</h2>
        <p class="text-white-50 mb-4" style="max-width: 360px;">
          Nhập email đã đăng ký, chúng tôi sẽ gửi cho bạn liên kết an toàn để đặt lại mật khẩu.
        </p>
        <ul class="list-unstyled d-flex flex-column gap-3 brand-features">
          <li class="d-flex align-items-center gap-3"><PhEnvelopeSimple size="22" weight="duotone" /> Link gửi tới email trong giây lát</li>
          <li class="d-flex align-items-center gap-3"><PhShieldCheck size="22" weight="duotone" /> Bảo mật, hết hạn sau 15 phút</li>
        </ul>
      </div>
      <small class="text-white-50 position-relative">© 2026 Brevery · Bakery &amp; Beverage</small>
    </div>

    <!-- FORM PANEL -->
    <div class="auth-form-panel d-flex align-items-center justify-content-center">
      <div class="auth-card-inner w-100" style="max-width: 420px;">
        <router-link to="/" class="d-lg-none d-inline-block mb-4 brand-logo-sm gradient-text">Brevery</router-link>
        <h3 class="fw-bold mb-1">Quên mật khẩu</h3>
        <p class="text-sub mb-4">Nhập email để nhận link đặt lại mật khẩu</p>

        <form @submit.prevent="handleSubmit">
          <div class="mb-4">
            <label class="form-label small fw-semibold">Email</label>
            <div class="input-icon-wrap">
              <PhEnvelope class="field-icon" size="18" />
              <input v-model="email" type="email" class="bakery-input has-icon" placeholder="email@example.com" required />
            </div>
          </div>
          <button type="submit" class="btn btn-bakery w-100 py-2" :disabled="loading">
            {{ loading ? 'Đang gửi...' : 'Gửi link đặt lại' }}
          </button>
        </form>

        <div v-if="sent" class="alert mt-3 d-flex align-items-center gap-2" style="background: var(--accent-sage); color: var(--accent-sage-text); border-radius: var(--radius-input); border: none">
          <PhCheckCircle size="20" weight="fill" /> Đã gửi email! Kiểm tra hộp thư của bạn.
        </div>
        <hr class="my-4" />
        <p class="text-center text-sub mb-0 small">
          <router-link to="/login" class="text-bakery fw-semibold d-inline-flex align-items-center gap-1">
            <PhArrowLeft size="16" /> Quay lại đăng nhập
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { authApi } from '@/api/auth.api'
import { toast } from 'vue3-toastify'
import { PhEnvelope, PhEnvelopeSimple, PhShieldCheck, PhCheckCircle, PhArrowLeft } from '@phosphor-icons/vue'

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
</style>
