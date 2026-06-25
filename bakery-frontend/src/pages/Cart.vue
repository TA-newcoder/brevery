<template>
  <div class="container py-4 py-lg-5 cart-page">
    <div class="d-flex align-items-center gap-3 mb-4">
      <button class="btn btn-bakery-ghost p-2 rounded-circle d-flex align-items-center justify-content-center" style="width: 40px; height: 40px;" @click="router.back()">
        <PhArrowLeft size="24" weight="bold" />
      </button>
      <h2 class="fw-bold mb-0">Giỏ hàng của bạn</h2>
    </div>
    
    <div v-if="cartStore.items.length === 0" class="empty-state bakery-card">
      <div class="empty-state-icon mx-auto text-muted mb-3">
        <PhShoppingCart size="48" weight="duotone" />
      </div>
      <h4 class="fw-bold">Giỏ hàng trống</h4>
      <p class="text-sub mt-2 mb-4">Bạn chưa chọn món nào. Cùng khám phá thực đơn các loại bánh và thức uống của Brevery nhé!</p>
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
              <router-link :to="`/product/${item.productId}`" class="text-decoration-none text-dark d-block">
                <h6 class="fw-bold mb-1 text-truncate cart-item-title" style="max-width: 250px;">{{ item.productName }}</h6>
              </router-link>
              <div v-if="item.availableVariants && item.availableVariants.length > 1" class="mb-2 dropdown">
                <button class="btn variant-select d-flex justify-content-between align-items-center gap-2 w-100 shadow-sm" type="button" data-bs-toggle="dropdown" aria-expanded="false" style="max-width: 200px;">
                  <span class="text-truncate">Phân loại: {{ item.productSize || item.variantName }}</span>
                  <PhCaretDown weight="bold" size="12" class="variant-select-icon position-static transform-none" />
                </button>
                <ul class="dropdown-menu dropdown-menu-dark shadow border-secondary-subtle py-2" style="background-color: var(--bg-card); min-width: 220px; border-radius: 12px;">
                  <li v-for="v in item.availableVariants" :key="v.variantId">
                    <a class="dropdown-item small py-2 px-3 d-flex justify-content-between align-items-center" href="#" 
                       :style="v.variantId === item.variantId ? 'background-color: var(--bakery-primary); color: white;' : 'color: var(--text-main);'"
                       @click.prevent="cartStore.changeVariant(item, v)">
                      <span class="fw-medium">Size {{ v.size }}</span>
                      <span class="fw-bold">{{ formatPrice(v.salePrice || v.price) }}</span>
                    </a>
                  </li>
                </ul>
              </div>
              <div class="fw-bold text-bakery d-md-none">{{ formatPrice(item.price) }}</div>
            </div>
            
            <div class="d-flex align-items-center gap-2 rounded-pill px-2 py-1 shadow-inner" style="background: var(--bg-body); border: 1px solid var(--border-color);">
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
          
          <div class="mb-4 p-3 rounded-4" style="background: rgba(212, 135, 90, 0.05); border: 1px dashed var(--bakery-primary);">
            <label class="form-label small text-sub fw-bold mb-2"><PhSparkle weight="fill" color="var(--bakery-primary)"/> Khuyến mãi & Ưu đãi</label>
            <div class="d-flex gap-2">
              <input type="text" class="form-control bakery-input py-2 px-3 border-0 shadow-sm" placeholder="Nhập mã giảm giá...">
              <button class="btn btn-bakery px-3 rounded-pill fw-bold shadow-sm">Áp dụng</button>
            </div>
          </div>
          
          <div class="d-flex justify-content-between mb-2">
            <span class="text-sub">Tạm tính ({{ cartStore.totalItems }} món)</span>
            <span class="fw-semibold">{{ formatPrice(cartStore.totalPrice) }}</span>
          </div>
          <div class="d-flex justify-content-between mb-3">
            <span class="text-sub">Phí giao hàng</span>
            <span v-if="cartStore.totalPrice >= 500000" class="fw-semibold text-success">Miễn phí</span>
            <span v-else class="fw-semibold text-muted small">Tính khi thanh toán</span>
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
import { PhArrowLeft, PhShoppingCart, PhMinus, PhPlus, PhTrash, PhArrowRight, PhSparkle, PhPencilSimple, PhCaretDown } from '@phosphor-icons/vue'
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
.cart-item-title {
  color: var(--text-main);
  transition: color 0.2s ease;
}
.cart-item-title:hover {
  color: var(--bakery-primary);
}

.variant-select-wrapper {
  position: relative;
  display: inline-block;
  max-width: 220px;
}
.variant-select {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  color: var(--bakery-primary);
  padding: 4px 28px 4px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  outline: none;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
  width: 100%;
}
.variant-select:hover, .variant-select:focus {
  border-color: var(--bakery-primary);
  box-shadow: 0 0 0 3px rgba(212, 135, 90, 0.1);
  background-color: var(--bg-body);
}
.variant-select-icon {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
  color: var(--bakery-primary);
  transition: transform 0.2s ease;
}

.dropdown-item:hover {
  background-color: rgba(212, 135, 90, 0.1) !important;
  color: var(--bakery-primary) !important;
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
