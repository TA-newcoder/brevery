<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4 class="fw-bold mb-0">Quản lý khuyến mãi</h4>
      <button class="btn btn-bakery btn-sm d-flex align-items-center gap-1" @click="openModal()">
        <PhPlus weight="bold" size="18" /> Thêm coupon
      </button>
    </div>

    <div class="bakery-card">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead><tr>
            <th>Mã</th><th>Loại</th><th>Giá trị</th><th>Tối thiểu</th><th>Hết hạn</th><th>Đã dùng</th><th>Trạng thái</th><th>Hành động</th>
          </tr></thead>
          <tbody>
            <tr v-for="c in coupons" :key="c.couponId" :class="{ 'table-secondary': !c.isActive }">
              <td><code class="fw-bold">{{ c.code }}</code></td>
              <td><span class="badge bg-info text-dark">{{ c.discountType }}</span></td>
              <td class="fw-semibold">{{ c.discountType === 'PERCENT' ? c.discountValue + '%' : formatPrice(c.discountValue) }}</td>
              <td class="small text-sub">{{ c.minOrderAmount ? formatPrice(c.minOrderAmount) : '—' }}</td>
              <td class="small" :class="isExpired(c) ? 'text-danger' : ''">{{ formatDate(c.expiryDate) }}</td>
              <td>{{ c.usedCount }}/{{ c.usageLimit || '∞' }}</td>
              <td>
                <span :class="c.isActive ? 'badge bg-success' : 'badge bg-secondary'">{{ c.isActive ? 'Hoạt động' : 'Tắt' }}</span>
              </td>
              <td>
                <div class="d-flex gap-1">
                  <button class="btn btn-sm btn-bakery-outline" @click="openModal(c)">✏️</button>
                  <button class="btn btn-sm btn-outline-secondary" @click="toggleCoupon(c)">
                    {{ c.isActive ? '🙈' : '👁️' }}
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteCoupon(c)">🗑️</button>
                </div>
              </td>
            </tr>
            <tr v-if="!coupons.length"><td colspan="8" class="text-center text-sub py-4">Chưa có coupon nào</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="bakery-card modal-box">
        <h5 class="fw-bold mb-3">{{ editing ? 'Sửa coupon' : 'Thêm coupon' }}</h5>
        <form @submit.prevent="saveCoupon">
          <div class="row g-3">
            <div class="col-6">
              <label class="form-label small fw-semibold">Mã coupon *</label>
              <input v-model="form.code" class="bakery-input" required placeholder="VD: SALE50" />
            </div>
            <div class="col-6">
              <label class="form-label small fw-semibold">Loại *</label>
              <select v-model="form.discountType" class="bakery-input" required>
                <option value="PERCENT">Phần trăm (%)</option>
                <option value="FIXED">Cố định (VNĐ)</option>
              </select>
            </div>
            <div class="col-6">
              <label class="form-label small fw-semibold">Giá trị giảm *</label>
              <input v-model.number="form.discountValue" type="number" min="0.01" step="0.01" class="bakery-input" required />
            </div>
            <div class="col-6">
              <label class="form-label small fw-semibold">Giảm tối đa (PERCENT)</label>
              <input v-model.number="form.maxDiscount" type="number" min="0" class="bakery-input" />
            </div>
            <div class="col-6">
              <label class="form-label small fw-semibold">Đơn tối thiểu</label>
              <input v-model.number="form.minOrderAmount" type="number" min="0" class="bakery-input" />
            </div>
            <div class="col-6">
              <label class="form-label small fw-semibold">Giới hạn lượt (0 = ∞)</label>
              <input v-model.number="form.usageLimit" type="number" min="0" class="bakery-input" />
            </div>
            <div class="col-12">
              <label class="form-label small fw-semibold">Ngày hết hạn *</label>
              <input v-model="form.expiryDate" type="datetime-local" class="bakery-input" required />
            </div>
          </div>
          <div class="d-flex gap-2 justify-content-end mt-3">
            <button type="button" class="btn btn-bakery-outline btn-sm" @click="showModal = false">Hủy</button>
            <button type="submit" class="btn btn-bakery btn-sm" :disabled="saving">
              {{ saving ? 'Đang lưu...' : 'Lưu' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin.api'
import { PhPlus } from '@phosphor-icons/vue'
import { toast } from 'vue3-toastify'
import dayjs from 'dayjs'

const coupons = ref([])
const showModal = ref(false)
const editing = ref(null)
const saving = ref(false)
const form = ref(emptyForm())

function emptyForm() {
  return { code: '', discountType: 'PERCENT', discountValue: null, maxDiscount: null, minOrderAmount: null, usageLimit: 0, expiryDate: '' }
}

function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }
function formatDate(d) { return dayjs(d).format('DD/MM/YYYY HH:mm') }
function isExpired(c) { return dayjs(c.expiryDate).isBefore(dayjs()) }

async function fetchCoupons() {
  try {
    const { data } = await adminApi.getCoupons()
    coupons.value = data.data || []
  } catch { coupons.value = [] }
}

function openModal(coupon = null) {
  editing.value = coupon
  if (coupon) {
    form.value = {
      code: coupon.code,
      discountType: coupon.discountType,
      discountValue: coupon.discountValue,
      maxDiscount: coupon.maxDiscount,
      minOrderAmount: coupon.minOrderAmount,
      usageLimit: coupon.usageLimit || 0,
      expiryDate: dayjs(coupon.expiryDate).format('YYYY-MM-DDTHH:mm'),
    }
  } else {
    form.value = emptyForm()
  }
  showModal.value = true
}

async function saveCoupon() {
  saving.value = true
  try {
    const payload = { ...form.value, expiryDate: new Date(form.value.expiryDate).toISOString() }
    if (editing.value) {
      await adminApi.updateCoupon(editing.value.couponId, payload)
      toast.success('Cập nhật thành công')
    } else {
      await adminApi.createCoupon(payload)
      toast.success('Thêm coupon thành công')
    }
    showModal.value = false
    await fetchCoupons()
  } catch (e) { toast.error(e.response?.data?.message || 'Lỗi') }
  finally { saving.value = false }
}

async function toggleCoupon(c) {
  try {
    await adminApi.toggleCoupon(c.couponId)
    await fetchCoupons()
  } catch (e) { toast.error(e.response?.data?.message || 'Lỗi') }
}

async function deleteCoupon(c) {
  if (!confirm(`Xóa coupon "${c.code}"?`)) return
  try {
    await adminApi.deleteCoupon(c.couponId)
    await fetchCoupons()
    toast.success('Đã xóa coupon')
  } catch (e) { toast.error(e.response?.data?.message || 'Lỗi') }
}

onMounted(fetchCoupons)
</script>

<style scoped>
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.4); z-index: 1050; display: flex; align-items: center; justify-content: center; }
.modal-box { width: 100%; max-width: 600px; }
</style>
