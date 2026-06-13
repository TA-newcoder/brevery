<template>
  <div class="admin-settings">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h5 class="fw-bold mb-0" style="color: var(--text-main)">Cài đặt hệ thống</h5>
    </div>

    <div class="row g-4">
      <!-- CÀI ĐẶT CỬA HÀNG -->
      <div class="col-lg-8">
        <div class="bakery-card">
          <div class="d-flex align-items-center gap-2 mb-4">
            <PhStorefront size="24" weight="fill" color="var(--primary)" />
            <h6 class="fw-bold mb-0" style="color: var(--text-main)">Thông tin cửa hàng</h6>
          </div>
          
          <form @submit.prevent="saveStoreInfo">
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label text-sub fw-semibold small">Tên cửa hàng</label>
                <input type="text" class="form-control bakery-input" v-model="storeInfo.name" />
              </div>
              <div class="col-md-6">
                <label class="form-label text-sub fw-semibold small">Số điện thoại liên hệ</label>
                <input type="text" class="form-control bakery-input" v-model="storeInfo.phone" />
              </div>
              <div class="col-12">
                <label class="form-label text-sub fw-semibold small">Địa chỉ chính</label>
                <input type="text" class="form-control bakery-input" v-model="storeInfo.address" />
              </div>
              <div class="col-md-6">
                <label class="form-label text-sub fw-semibold small">Email hỗ trợ</label>
                <input type="email" class="form-control bakery-input" v-model="storeInfo.email" />
              </div>
              <div class="col-md-6">
                <label class="form-label text-sub fw-semibold small">Giờ hoạt động</label>
                <input type="text" class="form-control bakery-input" v-model="storeInfo.hours" placeholder="VD: 08:00 - 22:00" />
              </div>
              <div class="col-12 mt-4 text-end">
                <button type="submit" class="btn btn-primary px-4 fw-semibold d-flex align-items-center gap-2 ms-auto" :disabled="isSavingStore">
                  <span v-if="isSavingStore" class="spinner-border spinner-border-sm" role="status"></span>
                  Lưu thay đổi
                </button>
              </div>
            </div>
          </form>
        </div>
        
        <!-- CÀI ĐẶT PHÍ GIAO HÀNG (Tương lai) -->
        <div class="bakery-card mt-4">
          <div class="d-flex align-items-center gap-2 mb-4">
            <PhTruck size="24" weight="fill" color="var(--primary)" />
            <h6 class="fw-bold mb-0" style="color: var(--text-main)">Cấu hình giao hàng & Thanh toán</h6>
          </div>
          <p class="text-sub small mb-0">Tính năng này đang được phát triển và sẽ được cập nhật trong phiên bản tới.</p>
        </div>
      </div>

      <!-- ĐỔI MẬT KHẨU ADMIN -->
      <div class="col-lg-4">
        <div class="bakery-card">
          <div class="d-flex align-items-center gap-2 mb-4">
            <PhShieldCheck size="24" weight="fill" color="var(--primary)" />
            <h6 class="fw-bold mb-0" style="color: var(--text-main)">Bảo mật tài khoản</h6>
          </div>

          <form @submit.prevent="changePassword">
            <div class="mb-3">
              <label class="form-label text-sub fw-semibold small">Mật khẩu hiện tại</label>
              <input type="password" class="form-control bakery-input" v-model="pwdForm.oldPassword" required />
            </div>
            <div class="mb-3">
              <label class="form-label text-sub fw-semibold small">Mật khẩu mới</label>
              <input type="password" class="form-control bakery-input" v-model="pwdForm.newPassword" required />
            </div>
            <div class="mb-4">
              <label class="form-label text-sub fw-semibold small">Xác nhận mật khẩu mới</label>
              <input type="password" class="form-control bakery-input" v-model="pwdForm.confirmPassword" required />
            </div>
            
            <button type="submit" class="btn btn-outline-primary w-100 fw-semibold d-flex align-items-center justify-content-center gap-2" :disabled="isChangingPwd">
              <span v-if="isChangingPwd" class="spinner-border spinner-border-sm" role="status"></span>
              Đổi mật khẩu
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { toast } from 'vue3-toastify'
import { PhStorefront, PhShieldCheck, PhTruck } from '@phosphor-icons/vue'
import api from '@/api/axiosInstance' // Dùng chung axios instance cho việc đổi pass

const isSavingStore = ref(false)
const isChangingPwd = ref(false)

const storeInfo = ref({
  name: 'Brevery Bakery & Beverage',
  phone: '0909 000 111',
  address: '123 Đường Bánh Ngọt, Phường Mứt, Quận Dâu',
  email: 'support@brevery.vn',
  hours: '08:00 - 22:30'
})

const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(() => {
  // TODO: Fetch store info if there's a backend endpoint for it later.
})

const saveStoreInfo = async () => {
  isSavingStore.value = true
  // Mock API Call
  setTimeout(() => {
    toast.success('Đã cập nhật thông tin cửa hàng thành công!')
    isSavingStore.value = false
  }, 1000)
}

const changePassword = async () => {
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    toast.error('Mật khẩu xác nhận không khớp!')
    return
  }
  
  if (pwdForm.value.newPassword.length < 6) {
    toast.error('Mật khẩu mới phải có ít nhất 6 ký tự.')
    return
  }

  isChangingPwd.value = true
  try {
    // Backend chưa có endpoint /api/v1/users/me/password, đây là code chuẩn bị sẵn.
    // Nếu bạn có backend AuthController hỗ trợ đổi mật khẩu, có thể gọi thực tế.
    // await api.put('/users/me/password', {
    //   oldPassword: pwdForm.value.oldPassword,
    //   newPassword: pwdForm.value.newPassword
    // })
    
    // Mock success for now
    setTimeout(() => {
      toast.success('Đổi mật khẩu thành công!')
      pwdForm.value.oldPassword = ''
      pwdForm.value.newPassword = ''
      pwdForm.value.confirmPassword = ''
      isChangingPwd.value = false
    }, 1000)

  } catch (error) {
    toast.error(error.response?.data?.message || 'Có lỗi xảy ra khi đổi mật khẩu.')
    isChangingPwd.value = false
  }
}
</script>
