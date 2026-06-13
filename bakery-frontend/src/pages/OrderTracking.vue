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
      <div class="d-flex justify-content-between align-items-center border-bottom pb-3 mb-3">
        <div>
          <h5 class="fw-bold text-main mb-1">#{{ order.orderCode }}</h5>
          <span class="small text-sub">{{ formatDate(order.createdAt) }}</span>
        </div>
        <OrderStatusBadge :status="order.status" />
      </div>

      <div class="mb-4">
        <h6 class="fw-bold mb-3">Thông tin nhận hàng</h6>
        <p class="mb-1 text-main"><span class="fw-semibold">Người nhận:</span> {{ order.receiverName || order.recipientName }}</p>
        <p class="mb-1 text-main"><span class="fw-semibold">Số điện thoại:</span> {{ order.receiverPhone || order.recipientPhone }}</p>
        <p class="mb-1 text-main"><span class="fw-semibold">Địa chỉ:</span> {{ order.shippingAddress || order.address }}</p>
      </div>

      <div class="mb-4">
        <h6 class="fw-bold mb-3">Sản phẩm đã đặt</h6>
        <div v-for="(item, index) in order.orderDetails" :key="item.orderDetailId">
          <div class="d-flex align-items-center py-2">
            <img :src="item.primaryImageUrl || '/assets/images/placeholder.png'" class="rounded me-3 object-fit-cover border" style="width: 56px; height: 56px" />
            <div class="flex-grow-1">
              <div class="fw-semibold text-main mb-1">{{ item.productName }}</div>
              <div class="small text-sub">Size: {{ item.productSize }} x {{ item.quantity }}</div>
            </div>
            <div class="text-end fw-semibold text-main">
              {{ formatPrice(item.price) }}
            </div>
          </div>
          <hr v-if="index < order.orderDetails.length - 1" class="my-1 border-light" />
        </div>
      </div>

      <div class="p-3 bg-light rounded-3">
        <div class="d-flex justify-content-between mb-2 small text-sub"><span>Tạm tính:</span><span>{{ formatPrice(order.subTotal) }}</span></div>
        <div class="d-flex justify-content-between mb-2 small text-sub"><span>Phí giao hàng:</span><span>{{ formatPrice(order.shippingFee) }}</span></div>
        <div v-if="order.discountAmount > 0" class="d-flex justify-content-between mb-2 small text-success"><span>Khuyến mãi:</span><span>-{{ formatPrice(order.discountAmount) }}</span></div>
        <hr class="my-2 border-light"/>
        <div class="d-flex justify-content-between align-items-center">
          <span class="fw-semibold text-main">Thành tiền:</span>
          <span class="fw-bold fs-5 text-primary">{{ formatPrice(order.totalAmount) }}</span>
        </div>
      </div>
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
import dayjs from 'dayjs'

const router = useRouter()

const code = ref('')
const phone = ref('')
const order = ref(null)
const loading = ref(false)
const notFound = ref(false)

function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }
function formatDate(d) { return dayjs(d).format('HH:mm - DD/MM/YYYY') }

async function track() {
  loading.value = true; order.value = null; notFound.value = false
  try {
    const { data } = await orderApi.trackOrder(code.value, phone.value)
    order.value = data.data
  } catch { notFound.value = true }
  finally { loading.value = false }
}
</script>
