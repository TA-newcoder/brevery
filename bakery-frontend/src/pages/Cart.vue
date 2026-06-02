<template>
  <div class="container py-4">
    <button class="btn btn-sm text-decoration-none text-dark p-0 mb-3 d-flex align-items-center gap-2" @click="router.back()">
      <PhArrowLeft size="18" weight="bold" /> Quay lại
    </button>
    <h2 class="fw-bold mb-4">🛒 Giỏ hàng</h2>
    <div v-if="cartStore.items.length === 0" class="text-center py-5">
      <span style="font-size: 4rem">🛒</span>
      <p class="text-sub mt-3 mb-4">Giỏ hàng trống</p>
      <router-link to="/products" class="btn btn-bakery">Mua sắm ngay</router-link>
    </div>
    <div v-else class="row g-4">
      <div class="col-lg-8">
        <div v-for="item in cartStore.items" :key="item.cartItemId || item.variantId" class="bakery-card mb-3">
          <div class="d-flex gap-3 align-items-center">
            <img :src="item.imageUrl || item.primaryImageUrl || '/images/cake.png'" style="width:80px; height:80px; object-fit:cover; border-radius: 14px" />
            <div class="flex-grow-1">
              <h6 class="fw-semibold mb-1">{{ item.productName }}</h6>
              <span class="text-sub small">{{ item.variantName }}</span>
              <div class="d-flex align-items-center gap-2 mt-2">
                <button class="btn btn-bakery-outline btn-sm" @click="cartStore.updateQuantity(item.cartItemId || item.variantId, Math.max(1, item.quantity - 1))">−</button>
                <span class="fw-bold">{{ item.quantity }}</span>
                <button class="btn btn-bakery-outline btn-sm" @click="cartStore.updateQuantity(item.cartItemId || item.variantId, item.quantity + 1)">+</button>
              </div>
            </div>
            <div class="text-end">
              <div class="fw-bold text-bakery">{{ formatPrice(item.price * item.quantity) }}</div>
              <button class="btn btn-sm text-danger mt-1" @click="cartStore.removeItem(item.cartItemId || item.variantId)">🗑️ Xóa</button>
            </div>
          </div>
        </div>
      </div>
      <div class="col-lg-4">
        <div class="bakery-card">
          <h5 class="fw-bold mb-3">Tóm tắt đơn hàng</h5>
          <div class="d-flex justify-content-between mb-2"><span class="text-sub">Số lượng</span><span>{{ cartStore.totalItems }} sản phẩm</span></div>
          <div class="d-flex justify-content-between mb-3"><span class="text-sub">Tạm tính</span><span class="fw-bold text-bakery">{{ formatPrice(cartStore.totalPrice) }}</span></div>
          <hr />
          <router-link to="/checkout" class="btn btn-bakery w-100">Tiến hành thanh toán</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart.store'
import { PhArrowLeft } from '@phosphor-icons/vue'
const router = useRouter()
const cartStore = useCartStore()
function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }
</script>
