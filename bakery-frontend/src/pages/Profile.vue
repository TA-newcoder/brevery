<template>
  <div class="profile-page">
    <!-- Header Banner -->
    <div class="profile-header-bg position-relative shadow-sm">
      <div class="container h-100 position-relative">
        <div class="profile-avatar-wrapper position-absolute shadow-lg">
          <template v-if="form.avatarUrl">
            <img :src="form.avatarUrl" alt="avatar" class="profile-avatar" />
          </template>
          <div v-else class="profile-avatar-initials d-flex justify-content-center align-items-center w-100 h-100">
            {{ getInitials(form.fullName) }}
          </div>
          <label class="avatar-edit-overlay cursor-pointer d-flex flex-column justify-content-center align-items-center m-0">
            <PhCamera size="28" weight="fill" color="#fff" />
            <span class="small fw-bold text-white mt-1">Cập nhật</span>
            <input type="file" class="d-none" @change="handleFileUpload" accept="image/*" :disabled="uploading" />
          </label>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="container pb-5" style="padding-top: 90px; max-width: 1000px;">
      
      <!-- Loading State -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-warning" role="status"></div>
        <div class="mt-2 text-muted fw-semibold">Đang tải hồ sơ...</div>
      </div>

      <div v-else>
        <!-- Name & Badge -->
        <div class="d-flex justify-content-between align-items-end mb-4 px-2">
          <div>
            <h2 class="fw-bold mb-2">{{ form.fullName || 'Người dùng' }}</h2>
            <span class="badge badge-caramel px-3 py-2 rounded-pill shadow-sm d-inline-flex align-items-center gap-1">
              <PhShieldCheck size="16" weight="fill" /> {{ profile.role }}
            </span>
          </div>
        </div>

        <div class="row g-4">
          <!-- Cột Trái: Hồ sơ -->
          <div class="col-lg-8">
            <div class="card premium-card border-0 shadow-sm rounded-4 h-100">
              <div class="card-body p-4 p-md-5">
                <h5 class="fw-bold mb-4 d-flex align-items-center gap-2">
                  <PhIdentificationBadge size="24" weight="duotone" color="var(--bakery-primary)" />
                  Thông tin cá nhân
                </h5>
                <form @submit.prevent="saveProfile">
                  <div class="row g-4">
                    <div class="col-md-6">
                      <label class="form-label fw-bold small profile-label">Họ và tên <span class="text-danger">*</span></label>
                      <div class="input-group premium-input-group">
                        <span class="input-group-text"><PhUser size="20" class="input-icon" /></span>
                        <input v-model="form.fullName" class="form-control form-control-lg" placeholder="Nhập họ và tên" required />
                      </div>
                    </div>
                    
                    <div class="col-md-6">
                      <label class="form-label fw-bold small profile-label">Số điện thoại</label>
                      <div class="input-group premium-input-group">
                        <span class="input-group-text"><PhPhone size="20" class="input-icon" /></span>
                        <input v-model="form.phone" class="form-control form-control-lg" placeholder="Nhập số điện thoại" />
                      </div>
                    </div>
                    
                    <div class="col-12">
                      <label class="form-label fw-bold small profile-label">Địa chỉ Email</label>
                      <div class="input-group premium-input-group" :class="{ 'verified-group': profile.isEmailVerified }">
                        <span class="input-group-text"><PhEnvelope size="20" class="input-icon" /></span>
                        <input :value="profile.email" class="form-control form-control-lg disabled-input" disabled />
                        <span class="input-group-text disabled-input border-start-0" :class="profile.isEmailVerified ? 'text-success' : 'text-danger'">
                          <PhCheckCircle v-if="profile.isEmailVerified" size="20" weight="fill" title="Đã xác thực" />
                          <PhWarning v-else size="20" weight="fill" title="Chưa xác thực" />
                        </span>
                      </div>
                      <div v-if="!profile.isEmailVerified" class="form-text text-danger mt-2 fw-semibold d-flex align-items-center gap-1">
                        <PhWarning size="16" /> Email của bạn chưa được xác thực!
                      </div>
                    </div>
                  </div>
                  
                  <div class="mt-5 text-end border-top pt-4">
                    <button type="submit" class="btn btn-bakery btn-lg px-5 rounded-pill fw-bold shadow-sm d-inline-flex align-items-center justify-content-center gap-2" :disabled="saving || uploading" style="min-width: 200px;">
                      <span v-if="saving || uploading" class="spinner-border spinner-border-sm"></span>
                      <PhFloppyDisk v-else size="20" weight="bold" /> 
                      {{ saving ? 'Đang lưu...' : (uploading ? 'Đang tải ảnh...' : 'Lưu thay đổi') }}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>

          <!-- Cột Phải: Mật khẩu -->
          <div class="col-lg-4">
            <div class="card premium-card border-0 shadow-sm rounded-4 h-100">
              <div class="card-body p-4 p-md-5">
                <h5 class="fw-bold mb-4 d-flex align-items-center gap-2">
                  <PhLockKey size="24" weight="duotone" color="var(--bakery-primary)" />
                  Bảo mật
                </h5>
                <form @submit.prevent="handleChangePassword">
                  <div class="mb-4 position-relative">
                    <label class="form-label fw-bold small profile-label">Mật khẩu hiện tại</label>
                    <input v-model="pwForm.currentPassword" :type="showCurrentPw ? 'text' : 'password'" class="form-control form-control-lg premium-input pe-5" placeholder="••••••••" required />
                    <button type="button" class="btn border-0 position-absolute end-0 bottom-0 mb-1 me-1 text-muted pw-toggle" @click="showCurrentPw = !showCurrentPw">
                      <PhEyeSlash v-if="showCurrentPw" size="20" />
                      <PhEye v-else size="20" />
                    </button>
                  </div>
                  <div class="mb-4 position-relative">
                    <label class="form-label fw-bold small profile-label">Mật khẩu mới</label>
                    <input v-model="pwForm.newPassword" :type="showNewPw ? 'text' : 'password'" class="form-control form-control-lg premium-input pe-5" minlength="8" placeholder="Tối thiểu 8 ký tự" required />
                    <button type="button" class="btn border-0 position-absolute end-0 bottom-0 mb-1 me-1 text-muted pw-toggle" @click="showNewPw = !showNewPw">
                      <PhEyeSlash v-if="showNewPw" size="20" />
                      <PhEye v-else size="20" />
                    </button>
                  </div>
                  <div class="mb-5 position-relative">
                    <label class="form-label fw-bold small profile-label">Xác nhận mật khẩu</label>
                    <input v-model="pwForm.confirmPassword" :type="showConfirmPw ? 'text' : 'password'" class="form-control form-control-lg premium-input pe-5" placeholder="••••••••" required />
                    <button type="button" class="btn border-0 position-absolute end-0 bottom-0 mb-1 me-1 text-muted pw-toggle" @click="showConfirmPw = !showConfirmPw">
                      <PhEyeSlash v-if="showConfirmPw" size="20" />
                      <PhEye v-else size="20" />
                    </button>
                  </div>
                  <button type="submit" class="btn btn-outline-primary btn-lg w-100 rounded-pill fw-bold d-inline-flex align-items-center justify-content-center gap-2" :disabled="savingPw">
                    <span v-if="savingPw" class="spinner-border spinner-border-sm"></span>
                    <PhShieldCheck v-else size="20" weight="bold" />
                    {{ savingPw ? 'Đang xử lý...' : 'Đổi mật khẩu' }}
                  </button>
                </form>
              </div>
            </div>
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
import { toast } from 'vue3-toastify'
import { 
  PhUser, PhCamera, PhShieldCheck, PhIdentificationBadge, 
  PhPhone, PhEnvelope, PhCheckCircle, PhWarning, 
  PhFloppyDisk, PhLockKey, PhEye, PhEyeSlash 
} from '@phosphor-icons/vue'

const authStore = useAuthStore()
const loading = ref(true)
const saving = ref(false)
const savingPw = ref(false)

const showCurrentPw = ref(false)
const showNewPw = ref(false)
const showConfirmPw = ref(false)

const profile = ref({})
const form = ref({ fullName: '', phone: '', avatarUrl: '' })
const pwForm = ref({ currentPassword: '', newPassword: '', confirmPassword: '' })

function getInitials(name) {
  if (!name) return 'U'
  const parts = name.trim().split(' ').filter(p => p.length > 0)
  if (parts.length > 1) {
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
  }
  return name.substring(0, 2).toUpperCase()
}

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

const uploading = ref(false)

async function handleFileUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await fetch('http://localhost:8080/api/v1/files/upload', {
      method: 'POST',
      body: formData
    })
    const data = await res.json()
    if (data.data?.url) {
      form.value.avatarUrl = data.data.url
      toast.success('Cập nhật Avatar thành công!')
    } else {
      toast.error('Lỗi tải ảnh')
    }
  } catch (e) {
    toast.error('Lỗi tải ảnh')
    event.target.value = ''
  } finally {
    uploading.value = false
  }
}

async function saveProfile() {
  saving.value = true
  try {
    await authApi.updateProfile(form.value)
    toast.success('Đã lưu thay đổi!')
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

<style scoped>
.profile-page {
  min-height: calc(100vh - 200px);
}

.profile-header-bg {
  height: 220px;
  background: linear-gradient(135deg, var(--bakery-primary) 0%, #FFA07A 100%);
  border-radius: 0 0 2rem 2rem;
  margin-bottom: 2rem;
}

.profile-avatar-wrapper {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  bottom: -75px;
  left: 20px;
  border: 5px solid var(--bg-surface);
  overflow: hidden;
  background: var(--bg-surface);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.profile-avatar-wrapper:hover {
  transform: translateY(-5px);
  box-shadow: 0 1rem 3rem rgba(0,0,0,0.175) !important;
}

.profile-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: filter 0.3s ease, transform 0.3s ease;
}

.profile-avatar-initials {
  background: var(--bakery-primary);
  color: white;
  font-size: 3.5rem;
  font-weight: 800;
  letter-spacing: -1px;
  transition: transform 0.3s ease;
}

.avatar-edit-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.6);
  opacity: 0;
  transition: opacity 0.3s ease;
  backdrop-filter: blur(2px);
}

.profile-avatar-wrapper:hover .avatar-edit-overlay {
  opacity: 1;
}
.profile-avatar-wrapper:hover .profile-avatar,
.profile-avatar-wrapper:hover .profile-avatar-initials {
  transform: scale(1.1);
}

.premium-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-light) !important;
  color: var(--text-main);
  transition: box-shadow 0.3s ease;
}

.premium-card h5 {
  color: var(--text-main);
}

.premium-card:hover {
  box-shadow: 0 0.5rem 1.5rem rgba(0,0,0,0.08) !important;
}

/* Input Group Styling */
.profile-label {
  color: var(--text-main);
  opacity: 0.8;
}

.input-icon {
  color: var(--text-main);
  opacity: 0.6;
}

.premium-input-group .input-group-text {
  background: var(--bg-surface);
  border-right: none;
  border-color: var(--border-color);
  transition: border-color 0.3s ease, color 0.3s ease;
}

.premium-input-group .form-control {
  border-left: none;
  border-color: var(--border-color);
  background: var(--bg-surface);
  color: var(--text-main);
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.premium-input-group .form-control:focus {
  box-shadow: none;
  border-color: var(--bakery-primary);
}

.premium-input-group:focus-within .input-group-text,
.premium-input-group:focus-within .form-control {
  border-color: var(--bakery-primary);
  color: var(--bakery-primary) !important;
}

.premium-input-group:focus-within .input-icon {
  color: var(--bakery-primary);
  opacity: 1;
}

/* For standalone inputs */
.premium-input {
  border-color: var(--border-color);
  border-radius: 0.75rem;
  background: var(--bg-surface);
  color: var(--text-main);
  transition: all 0.3s ease;
}

.premium-input::placeholder,
.premium-input-group .form-control::placeholder {
  color: var(--text-muted);
  opacity: 0.8;
}

.premium-input:focus {
  box-shadow: 0 0 0 4px rgba(200, 80, 42, 0.15);
  border-color: var(--bakery-primary);
}

.disabled-input {
  background-color: var(--bg-muted) !important;
  color: var(--text-main) !important;
  opacity: 1 !important;
}

/* Email Verified Status */
.verified-group .input-group-text,
.verified-group .form-control {
  border-color: #198754 !important;
}

.pw-toggle {
  opacity: 0.6;
  transition: opacity 0.3s ease, color 0.3s ease;
}
.pw-toggle:hover {
  opacity: 1;
  color: var(--bakery-primary) !important;
}
</style>
