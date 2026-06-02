<template>
  <div class="container py-4">
    <button class="btn btn-sm text-decoration-none text-dark p-0 mb-3 d-flex align-items-center gap-2" @click="router.back()">
      <PhArrowLeft size="18" weight="bold" /> Quay lại
    </button>
    <h2 class="fw-bold mb-4">🔍 Tra cứu đơn hàng</h2>
    <div class="bakery-card mb-4" style="max-width: 600px; margin: 0 auto">
      <div class="row g-3">
        <div class="col-md-6">
          <label class="form-label small fw-semibold">Mã đơn hàng</label>
          <input v-model="code" class="bakery-input" placeholder="BRV-XXXXXX" />
        </div>
        <div class="col-md-6">
          <label class="form-label small fw-semibold">Số điện thoại</label>
          <input v-model="phone" class="bakery-input" placeholder="0909..." />
        </div>
        <div class="col-12">
          <button class="btn btn-bakery w-100" @click="track" :disabled="loading">{{ loading ? 'Đang tìm...' : 'Tra cứu' }}</button>
        </div>
      </div>
    </div>
    <div v-if="order" class="bakery-card" style="max-width: 600px; margin: 0 auto">
      <div class="d-flex justify-content-between mb-3">
        <h5 class="fw-bold">#{{ order.orderCode }}</h5>
        <OrderStatusBadge :status="order.status" />
      </div>
      <p class="text-sub">{{ order.recipientName }} — {{ order.recipientPhone }}</p>
      <p class="text-sub">{{ order.address }}</p>
      <hr/>
      <div class="d-flex justify-content-between"><span>Tổng:</span><span class="fw-bold text-bakery">{{ formatPrice(order.totalAmount) }}</span></div>
    </div>
    <div v-if="notFound" class="text-center py-4 text-sub">Không tìm thấy đơn hàng</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi } from '@/api/order.api'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import { PhArrowLeft } from '@phosphor-icons/vue'

const router = useRouter()

const code = ref('')
const phone = ref('')
const order = ref(null)
const loading = ref(false)
const notFound = ref(false)

function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }

async function track() {
  loading.value = true; order.value = null; notFound.value = false
  try {
    const { data } = await orderApi.trackOrder(code.value, phone.value)
    order.value = data.data
  } catch { notFound.value = true }
  finally { loading.value = false }
}
</script>
