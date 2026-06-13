<template>
  <div class="container py-4" style="max-width: 720px">
    <h3 class="fw-bold mb-4 d-flex align-items-center gap-2">
      <PhUser weight="bold" size="28" color="var(--primary)" /> Thông tin cá nhân
    </h3>

    <div v-if="loading" class="text-center py-5"><div class="spinner-border text-warning"></div></div>

    <div v-else>
      <!-- Profile Form -->
      <div class="bakery-card mb-4">
        <h6 class="fw-bold mb-3">Cập nhật hồ sơ</h6>
        <form @submit.prevent="saveProfile">
          <div class="mb-3">
            <label class="form-label small fw-semibold">Email</label>
            <input :value="profile.email" class="bakery-input" disabled />
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">Họ và tên *</label>
            <input v-model="form.fullName" class="bakery-input" required />
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">Số điện thoại</label>
            <input v-model="form.phone" class="bakery-input" />
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">Avatar URL</label>
            <input v-model="form.avatarUrl" class="bakery-input" placeholder="https://..." />
          </div>
          <div class="d-flex gap-2">
            <button type="submit" class="btn btn-bakery" :disabled="saving">
              {{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}
            </button>
          </div>
        </form>
      </div>

      <!-- Change Password -->
      <div class="bakery-card">
        <h6 class="fw-bold mb-3">Đổi mật khẩu</h6>
        <form @submit.prevent="handleChangePassword">
          <div class="mb-3">
            <label class="form-label small fw-semibold">Mật khẩu hiện tại</label>
            <input v-model="pwForm.currentPassword" type="password" class="bakery-input" required />
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">Mật khẩu mới (tối thiểu 8 ký tự)</label>
            <input v-model="pwForm.newPassword" type="password" class="bakery-input" minlength="8" required />
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">Xác nhận mật khẩu mới</label>
            <input v-model="pwForm.confirmPassword" type="password" class="bakery-input" required />
          </div>
          <button type="submit" class="btn btn-bakery-outline" :disabled="savingPw">
            {{ savingPw ? 'Đang xử lý...' : 'Đổi mật khẩu' }}
          </button>
        </form>
      </div>

      <!-- Account Info -->
      <div class="bakery-card mt-4" style="background: var(--bg-muted)">
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <span class="small text-sub">Vai trò: </span>
            <span class="badge-caramel">{{ profile.role }}</span>
          </div>
          <div>
            <span class="small text-sub">Email xác thực: </span>
            <span :class="profile.isEmailVerified ? 'text-success' : 'text-danger'" class="fw-semibold">
              {{ profile.isEmailVerified ? '✓ Đã xác thực' : '✗ Chưa xác thực' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { authApi } from '@/api/auth.api'
import { useAuthStore } from '@/stores/auth.store'
import { PhUser } from '@phosphor-icons/vue'
import { toast } from 'vue3-toastify'

const authStore = useAuthStore()
const loading = ref(true)
const saving = ref(false)
const savingPw = ref(false)

const profile = ref({})
const form = ref({ fullName: '', phone: '', avatarUrl: '' })
const pwForm = ref({ currentPassword: '', newPassword: '', confirmPassword: '' })

async function fetchProfile() {
  loading.value = true
  try {
    const { data } = await authApi.getProfile()
    profile.value = data.data
    form.value = {
      fullName: data.data.fullName || '',
      phone: data.data.phone || '',
      avatarUrl: data.data.avatarUrl || '',
    }
  } catch { toast.error('Không thể tải thông tin') }
  finally { loading.value = false }
}

async function saveProfile() {
  saving.value = true
  try {
    await authApi.updateProfile(form.value)
    toast.success('Cập nhật thành công!')
    // Update store
    if (authStore.user) {
      authStore.user.fullName = form.value.fullName
      authStore.user.avatarUrl = form.value.avatarUrl
    }
    await fetchProfile()
  } catch (e) { toast.error(e.response?.data?.message || 'Cập nhật thất bại') }
  finally { saving.value = false }
}

async function handleChangePassword() {
  if (pwForm.value.newPassword !== pwForm.value.confirmPassword) {
    toast.error('Mật khẩu mới không khớp!')
    return
  }
  savingPw.value = true
  try {
    await authApi.changePassword({
      currentPassword: pwForm.value.currentPassword,
      newPassword: pwForm.value.newPassword,
    })
    toast.success('Đổi mật khẩu thành công!')
    pwForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e) { toast.error(e.response?.data?.message || 'Đổi mật khẩu thất bại') }
  finally { savingPw.value = false }
}

onMounted(fetchProfile)
</script>
