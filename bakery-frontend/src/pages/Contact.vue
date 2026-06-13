<template>
  <div class="container py-4" style="max-width: 720px">
    <h3 class="fw-bold mb-4 d-flex align-items-center gap-2">
      <PhEnvelope weight="bold" size="28" color="var(--primary)" /> Liên hệ với chúng tôi
    </h3>

    <div class="row g-4">
      <!-- Contact Info -->
      <div class="col-md-5">
        <div class="bakery-card h-100" style="background: var(--bg-muted)">
          <h6 class="fw-bold mb-3">Thông tin liên hệ</h6>
          <div class="mb-3 d-flex align-items-start gap-2">
            <PhMapPin weight="bold" size="20" color="var(--primary)" />
            <div>
              <div class="fw-semibold small">Địa chỉ</div>
              <div class="text-sub small">12 Nguyễn Văn Bảo, P.4, Q.Gò Vấp, TP.HCM</div>
            </div>
          </div>
          <div class="mb-3 d-flex align-items-start gap-2">
            <PhPhone weight="bold" size="20" color="var(--primary)" />
            <div>
              <div class="fw-semibold small">Hotline</div>
              <div class="text-sub small">0705 230 644</div>
            </div>
          </div>
          <div class="mb-3 d-flex align-items-start gap-2">
            <PhEnvelope weight="bold" size="20" color="var(--primary)" />
            <div>
              <div class="fw-semibold small">Email</div>
              <div class="text-sub small">support@brevery.vn</div>
            </div>
          </div>
          <hr />
          <div class="small text-sub">Giờ làm việc: 8:00 - 21:00 (Thứ 2 - Chủ nhật)</div>
        </div>
      </div>

      <!-- Contact Form -->
      <div class="col-md-7">
        <div class="bakery-card">
          <h6 class="fw-bold mb-3">Gửi tin nhắn</h6>
          <div v-if="sent" class="text-center py-4">
            <PhCheckCircle weight="fill" size="56" color="var(--color-success)" />
            <h5 class="fw-bold mt-3">Gửi thành công!</h5>
            <p class="text-sub">Chúng tôi sẽ phản hồi sớm nhất có thể.</p>
            <button class="btn btn-bakery-outline" @click="sent = false">Gửi tin nhắn khác</button>
          </div>
          <form v-else @submit.prevent="submit">
            <div class="row g-3">
              <div class="col-6">
                <label class="form-label small fw-semibold">Họ tên *</label>
                <input v-model="form.name" class="bakery-input" required />
              </div>
              <div class="col-6">
                <label class="form-label small fw-semibold">Email *</label>
                <input v-model="form.email" type="email" class="bakery-input" required />
              </div>
              <div class="col-6">
                <label class="form-label small fw-semibold">Số điện thoại</label>
                <input v-model="form.phone" class="bakery-input" />
              </div>
              <div class="col-6">
                <label class="form-label small fw-semibold">Chủ đề *</label>
                <input v-model="form.subject" class="bakery-input" required />
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">Nội dung *</label>
                <textarea v-model="form.message" class="bakery-input" rows="5" required></textarea>
              </div>
            </div>
            <button type="submit" class="btn btn-bakery mt-3 w-100" :disabled="sending">
              {{ sending ? 'Đang gửi...' : 'Gửi tin nhắn' }}
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { contactApi } from '@/api/product.api'
import { PhEnvelope, PhMapPin, PhPhone, PhCheckCircle } from '@phosphor-icons/vue'
import { toast } from 'vue3-toastify'

const sending = ref(false)
const sent = ref(false)
const form = ref({ name: '', email: '', phone: '', subject: '', message: '' })

async function submit() {
  sending.value = true
  try {
    await contactApi.submit(form.value)
    sent.value = true
    form.value = { name: '', email: '', phone: '', subject: '', message: '' }
  } catch (e) { toast.error(e.response?.data?.message || 'Gửi thất bại') }
  finally { sending.value = false }
}
</script>
