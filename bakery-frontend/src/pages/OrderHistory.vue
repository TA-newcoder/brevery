<template>
  <div class="container py-4" style="max-width: 900px">
    <h3 class="fw-bold mb-4" style="color: var(--text-main)">Lịch sử mua hàng</h3>

    <div v-if="loading" class="text-center py-5">
      <span class="spinner-border text-primary" role="status"></span>
    </div>
    
    <div v-else-if="orders.length === 0" class="text-center py-5 mt-4 bakery-card shadow-sm border-0">
      <div class="mb-3">
        <PhPackage size="64" color="var(--text-muted)" weight="duotone" />
      </div>
      <h5 class="fw-bold mb-2">Chưa có đơn hàng nào</h5>
      <p class="text-sub mb-4">Bạn chưa thực hiện bất kỳ giao dịch nào với Brevery.</p>
      <router-link to="/products" class="btn btn-primary px-4 py-2 fw-semibold rounded-pill shadow-sm">Mua sắm ngay</router-link>
    </div>

    <div v-else class="order-list">
      <div v-for="order in orders" :key="order.orderId" class="bakery-card border-0 mb-4 shadow-sm" style="overflow: hidden">
        <div class="p-3" style="background: var(--bg-muted); border-bottom: 1px solid var(--border-light)">
          <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
            <div>
              <span class="fw-bold text-main d-block mb-1">Mã Đơn: #{{ order.orderCode }}</span>
              <span class="small text-sub"><PhClock size="14" class="me-1"/>{{ formatDate(order.createdAt) }}</span>
            </div>
            <div class="d-flex align-items-center gap-3">
              <OrderStatusBadge :status="order.status" />
              <button class="btn btn-sm btn-outline-secondary d-flex align-items-center gap-1" @click="toggleOrder(order.orderId)">
                <span class="small fw-semibold">{{ expandedOrders.includes(order.orderId) ? 'Thu gọn' : 'Xem chi tiết' }}</span>
                <PhCaretDown v-if="!expandedOrders.includes(order.orderId)" />
                <PhCaretUp v-else />
              </button>
            </div>
          </div>
        </div>

        <div class="p-3">
          <!-- Chỉ hiển thị 1 sản phẩm đầu tiên nếu đang thu gọn, hoặc toàn bộ nếu đang mở -->
          <div v-for="(item, index) in order.orderDetails" :key="item.orderDetailId">
            <div class="d-flex align-items-center py-2" v-if="index === 0 || expandedOrders.includes(order.orderId)">
              <img :src="item.primaryImageUrl || '/assets/images/placeholder.png'" alt="Product" class="rounded object-fit-cover me-3" style="width: 64px; height: 64px; border: 1px solid var(--border-light)" />
              <div class="flex-grow-1">
                <div class="fw-semibold text-main mb-1">{{ item.productName }}</div>
                <div class="small text-sub">Size: {{ item.productSize }} x {{ item.quantity }}</div>
              </div>
              <div class="text-end">
                <div class="fw-semibold text-primary mb-1">{{ formatPrice(item.price) }}</div>
                <!-- Nút Đánh giá (Hiển thị khi đơn hàng đã giao và đang xem chi tiết) -->
                <button v-if="order.status === 'COMPLETED' && expandedOrders.includes(order.orderId)" 
                        class="btn btn-sm btn-outline-warning mt-1 px-3 d-flex align-items-center gap-1"
                        @click="openReviewModal(order.orderId, item)">
                  <PhStar size="14" weight="bold"/> Đánh giá
                </button>
              </div>
            </div>
            <hr v-if="(index === 0 || expandedOrders.includes(order.orderId)) && index < order.orderDetails.length - 1" class="my-1 border-light" />
          </div>
          
          <div v-if="!expandedOrders.includes(order.orderId) && order.orderDetails.length > 1" class="text-center py-2">
            <span class="small text-sub">và {{ order.orderDetails.length - 1 }} sản phẩm khác...</span>
          </div>
        </div>

        <div class="p-3 bg-light border-top border-light d-flex justify-content-between align-items-center">
          <div v-if="order.status === 'PENDING'">
            <button class="btn btn-sm btn-outline-danger px-3 py-1 fw-semibold rounded-pill" @click="cancelOrder(order.orderId)">Hủy đơn</button>
          </div>
          <div v-else>
            <span class="small text-sub">Phương thức: <span class="fw-semibold text-main">{{ order.paymentMethod === 'COD' ? 'Thanh toán khi nhận hàng' : 'VNPay' }}</span></span>
          </div>
          
          <div class="text-end">
            <span class="small text-sub me-2">Thành tiền:</span>
            <span class="fw-bold fs-5" style="color: var(--primary)">{{ formatPrice(order.totalAmount) }}</span>
          </div>
        </div>
        
        <!-- Expanded details info -->
        <div v-if="expandedOrders.includes(order.orderId)" class="p-3" style="background: var(--bg-surface); border-top: 1px dashed var(--border-light)">
          <div class="row text-sub small">
            <div class="col-md-6 mb-2 mb-md-0">
              <span class="fw-semibold text-main d-block mb-1">Thông tin nhận hàng</span>
              <div>{{ order.receiverName }} | {{ order.receiverPhone }}</div>
              <div>{{ order.shippingAddress }}</div>
            </div>
            <div class="col-md-6 text-md-end">
              <span class="fw-semibold text-main d-block mb-1">Tóm tắt thanh toán</span>
              <div>Tạm tính: {{ formatPrice(order.subTotal) }}</div>
              <div>Phí ship: {{ formatPrice(order.shippingFee) }}</div>
              <div v-if="order.discountAmount > 0">Giảm giá: -{{ formatPrice(order.discountAmount) }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <Pagination v-model="page" :total-pages="totalPages" @update:model-value="fetchOrders" />
    </div>

    <!-- REVIEW MODAL -->
    <div class="modal fade" id="reviewModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bakery-card p-0 overflow-hidden">
          <div class="modal-header border-0 bg-light pb-2">
            <h5 class="modal-title fw-bold" style="color: var(--text-main)">Đánh giá sản phẩm</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitReview">
            <div class="modal-body pt-3 text-center">
              <img :src="reviewItem?.primaryImageUrl || '/assets/images/placeholder.png'" class="rounded mb-3 object-fit-cover" style="width:80px;height:80px">
              <h6 class="fw-bold text-main mb-3">{{ reviewItem?.productName }}</h6>
              
              <div class="d-flex justify-content-center gap-2 mb-4">
                <PhStar v-for="i in 5" :key="i" 
                        size="32" 
                        :weight="i <= reviewForm.rating ? 'fill' : 'regular'" 
                        :color="i <= reviewForm.rating ? '#FFC107' : '#E0E0E0'"
                        style="cursor: pointer; transition: all 0.2s"
                        @click="reviewForm.rating = i"
                        @mouseenter="hoverRating = i"
                        @mouseleave="hoverRating = 0" />
              </div>
              
              <div class="text-start">
                <label class="form-label small fw-semibold text-sub">Chia sẻ cảm nhận của bạn</label>
                <textarea class="form-control bakery-input" v-model="reviewForm.comment" rows="3" placeholder="Sản phẩm tuyệt vời, bánh rất ngon..."></textarea>
              </div>
            </div>
            <div class="modal-footer border-0">
              <button type="button" class="btn btn-outline-secondary px-4 fw-semibold rounded-pill" data-bs-dismiss="modal">Hủy</button>
              <button type="submit" class="btn btn-primary px-4 fw-semibold rounded-pill" :disabled="isSubmitting || reviewForm.rating === 0">
                <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                Gửi Đánh Giá
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api/order.api'
import { toast } from 'vue3-toastify'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import Pagination from '@/components/Pagination.vue'
import dayjs from 'dayjs'
import { PhPackage, PhClock, PhCaretDown, PhCaretUp, PhStar } from '@phosphor-icons/vue'

const orders = ref([])
const loading = ref(true)
const page = ref(0)
const totalPages = ref(0)
const expandedOrders = ref([])

const reviewItem = ref(null)
const reviewOrderId = ref(null)
const isSubmitting = ref(false)
const hoverRating = ref(0)
const reviewForm = ref({ productId: null, rating: 0, comment: '' })

let reviewModalInstance = null

function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }
function formatDate(d) { return dayjs(d).format('HH:mm - DD/MM/YYYY') }

const toggleOrder = (id) => {
  if (expandedOrders.value.includes(id)) {
    expandedOrders.value = expandedOrders.value.filter(oId => oId !== id)
  } else {
    expandedOrders.value.push(id)
  }
}

async function fetchOrders() {
  loading.value = true
  try {
    const { data } = await orderApi.getHistory({ page: page.value, size: 10 })
    orders.value = data.data?.content || []
    totalPages.value = data.data?.totalPages || 0
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

async function cancelOrder(id) {
  if (!confirm('Bạn chắc chắn muốn hủy đơn hàng này?')) return
  try {
    await orderApi.cancelOrder(id)
    toast.success('Đã hủy đơn hàng thành công')
    fetchOrders()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Không thể hủy đơn hàng lúc này')
  }
}

async function openReviewModal(orderId, item) {
  reviewOrderId.value = orderId
  reviewItem.value = item
  reviewForm.value = { productId: item.productId, rating: 5, comment: '' }
  
  if (!reviewModalInstance) {
    const { Modal } = await import('bootstrap')
    reviewModalInstance = new Modal(document.getElementById('reviewModal'))
  }
  reviewModalInstance.show()
}

async function submitReview() {
  isSubmitting.value = true
  try {
    await orderApi.addReview(reviewOrderId.value, reviewForm.value)
    toast.success('Cảm ơn bạn đã đánh giá sản phẩm!')
    reviewModalInstance?.hide()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Bạn đã đánh giá sản phẩm này rồi hoặc có lỗi xảy ra.')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(fetchOrders)
</script>

<style scoped>
.order-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
</style>
