<template>
  <div class="container py-4 py-lg-5 cart-page">
    <button class="btn btn-sm btn-bakery-ghost p-0 mb-4 d-flex align-items-center gap-2" @click="router.back()">
      <PhArrowLeft size="18" weight="bold" /> Quay lại
    </button>
    <h2 class="fw-bold mb-4">Giỏ hàng của bạn</h2>
    
    <div v-if="cartStore.items.length === 0" class="empty-state bakery-card">
      <div class="empty-state-icon mx-auto text-muted mb-3">
        <PhShoppingCart size="48" weight="duotone" />
      </div>
      <h4 class="fw-bold">Giỏ hàng trống</h4>
      <p class="text-sub mt-2 mb-4">Bạn chưa chọn món nào. Cùng khám phá thực đơn tươi mới mỗi ngày của Brevery nhé!</p>
      <router-link to="/products" class="btn btn-bakery px-4 py-2 shadow-sm">Khám phá thực đơn</router-link>
    </div>
    
    <div v-else class="row g-4">
      <div class="col-lg-8">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <span class="fw-semibold">{{ cartStore.totalItems }} sản phẩm</span>
          <button class="btn btn-sm btn-outline-danger px-3 rounded-pill" @click="cartStore.clearCart()">Xóa tất cả</button>
        </div>
        
        <transition-group name="list" tag="div">
          <div v-for="item in cartStore.items" :key="item.cartItemId || item.variantId" class="bakery-card p-3 mb-3 d-flex gap-3 align-items-center cart-item-row">
            <img :src="item.imageUrl || item.primaryImageUrl || '/images/cake.png'" class="cart-item-img shadow-sm" />
            <div class="flex-grow-1">
              <h6 class="fw-bold mb-1 text-truncate" style="max-width: 200px;">{{ item.productName }}</h6>
              <span class="badge bg-secondary-subtle text-secondary small rounded-pill px-2 py-1 mb-2">{{ item.variantName || 'Mặc định' }}</span>
              <div class="fw-bold text-bakery d-md-none">{{ formatPrice(item.price) }}</div>
            </div>
            
            <div class="d-flex align-items-center gap-2 bg-light rounded-pill px-2 py-1 shadow-inner">
              <button class="btn btn-sm p-1 stepper-btn" @click="cartStore.updateQuantity(item.cartItemId || item.variantId, Math.max(1, item.quantity - 1))">
                <PhMinus size="16" weight="bold" />
              </button>
              <span class="fw-bold px-2 quantity-text" :key="item.quantity">{{ item.quantity }}</span>
              <button class="btn btn-sm p-1 stepper-btn" @click="cartStore.updateQuantity(item.cartItemId || item.variantId, item.quantity + 1)">
                <PhPlus size="16" weight="bold" />
              </button>
            </div>
            
            <div class="text-end ms-3 d-none d-md-block" style="min-width: 100px;">
              <div class="fw-bold text-bakery fs-5">{{ formatPrice(item.price * item.quantity) }}</div>
            </div>
            
            <button class="btn btn-sm text-danger ms-2 btn-delete" @click="cartStore.removeItem(item.cartItemId || item.variantId)" title="Xóa">
              <PhTrash size="20" weight="fill" />
            </button>
          </div>
        </transition-group>
      </div>
      
      <div class="col-lg-4">
        <div class="bakery-card position-sticky" style="top: 100px;">
          <h5 class="fw-bold mb-4">Tóm tắt đơn hàng</h5>
          
          <div class="mb-4">
            <label class="form-label small text-sub fw-semibold">Mã giảm giá</label>
            <div class="d-flex gap-2">
              <input type="text" class="form-control bakery-input py-1 px-3" placeholder="Nhập mã (nếu có)">
              <button class="btn btn-dark px-3 rounded-pill fw-bold">Áp dụng</button>
            </div>
          </div>
          
          <div class="d-flex justify-content-between mb-2">
            <span class="text-sub">Tạm tính ({{ cartStore.totalItems }} món)</span>
            <span class="fw-semibold">{{ formatPrice(cartStore.totalPrice) }}</span>
          </div>
          <div class="d-flex justify-content-between mb-3">
            <span class="text-sub">Phí giao hàng</span>
            <span class="fw-semibold text-success">Miễn phí</span>
          </div>
          
          <hr class="my-3 text-secondary opacity-25" />
          
          <div class="d-flex justify-content-between mb-4 align-items-center">
            <span class="fw-bold">Tổng cộng</span>
            <span class="fw-bold text-bakery fs-4">{{ formatPrice(cartStore.totalPrice) }}</span>
          </div>
          
          <router-link to="/checkout" class="btn-premium-action w-100 py-3 shadow-sm">
            <PhSparkle size="22" weight="fill" class="action-icon" />
            <span class="text_button">Thanh toán ngay</span>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart.store'
import { PhArrowLeft, PhShoppingCart, PhMinus, PhPlus, PhTrash, PhArrowRight, PhSparkle } from '@phosphor-icons/vue'
const router = useRouter()
const cartStore = useCartStore()
function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }
</script>

<style scoped>
.cart-item-row {
  transition: all 0.3s ease;
}
.cart-item-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 12px;
}
.stepper-btn {
  color: var(--text-main);
  transition: transform 0.1s, color 0.2s;
}
.stepper-btn:hover {
  color: var(--primary);
  transform: scale(1.1);
}
.quantity-text {
  display: inline-block;
  min-width: 20px;
  text-align: center;
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes popIn {
  0% { transform: scale(0.5); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
.btn-delete {
  opacity: 0.6;
  transition: all 0.2s ease;
}
.cart-item-row:hover .btn-delete {
  opacity: 1;
}
.btn-delete:hover {
  transform: translateY(-2px);
  background: var(--color-danger-light);
}
.shadow-inner {
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.05);
}

.list-enter-active,
.list-leave-active {
  transition: all 0.4s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* PREMIUM ACTION BUTTON */
.btn-premium-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: var(--gradient-primary);
  color: #fff;
  border-radius: var(--radius-btn);
  font-weight: 700;
  font-size: 1.05rem;
  letter-spacing: 0.02em;
  text-decoration: none;
  border: none;
  position: relative;
  overflow: hidden;
  transition: all 0.35s var(--ease-spring);
  box-shadow: 0 4px 12px rgba(200, 90, 50, 0.25);
}
.btn-premium-action::before {
  content: '';
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: linear-gradient(180deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}
.btn-premium-action:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px -6px rgba(200, 90, 50, 0.4);
  color: #fff;
}
.btn-premium-action:hover::before {
  opacity: 1;
}
.btn-premium-action:active {
  transform: scale(0.97) translateY(0);
}
.action-icon {
  animation: premium-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
@keyframes premium-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.8; }
}
</style>
