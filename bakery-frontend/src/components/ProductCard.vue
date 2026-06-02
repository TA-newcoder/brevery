<template>
  <div class="bakery-card product-card h-100 d-flex flex-column position-relative" @click="$router.push({ name: 'product-detail', params: { id: product.productId } })">
    
    <!-- TOP LEFT BADGE -->
    <div class="position-absolute z-2 d-flex flex-column gap-2" style="top: 20px; left: 20px;">
      <span v-if="product.totalSold > 20" class="badge rounded-pill bg-danger fw-semibold px-2 py-1 shadow-sm d-flex align-items-center gap-1 text-white">
        <PhTrendUp size="12" color="#fff" /> Bán chạy
      </span>
      <span v-else class="badge rounded-pill fw-semibold px-2 py-1 shadow-sm d-flex align-items-center gap-1 text-white" style="background-color: #16A34A;">
        <PhSparkle size="12" color="#fff" /> Mới
      </span>
    </div>

    <!-- TOP RIGHT HEART -->
    <div class="position-absolute z-2" style="top: 20px; right: 20px;">
      <button class="btn btn-light rounded-circle p-1 shadow-sm border-0 heart-btn" @click.stop="toggleHeart">
        <PhHeart :weight="isLiked ? 'fill' : 'regular'" color="#C8502A" size="20" />
      </button>
    </div>

    <!-- IMAGE WRAPPER -->
    <div class="product-img-wrapper position-relative">
      <img :src="product.primaryImageUrl || '/images/cake.png'" :alt="product.name" class="product-card-img w-100" />
      <!-- OVERLAY QUICH VIEW -->
      <div class="quick-view-overlay d-flex align-items-center justify-content-center">
        <span class="btn btn-light rounded-pill px-3 fw-semibold shadow-sm d-flex align-items-center gap-2">
          <PhEye size="18" weight="bold" /> Xem nhanh
        </span>
      </div>
    </div>

    <!-- CONTENT -->
    <div class="p-3 d-flex flex-column flex-grow-1 bg-white" style="border-radius: 0 0 12px 12px; z-index: 1;">
      <h6 class="fw-bold mb-2 text-truncate title-text" style="color: var(--bakery-text)">{{ product.name }}</h6>
      
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div class="d-flex align-items-center gap-1 text-sub small">
          <PhStar weight="fill" color="#f5a623" size="14" />
          <span v-if="product.avgRating > 0">{{ product.avgRating.toFixed(1) }}</span>
          <span v-else class="fw-semibold d-flex align-items-center gap-1 text-primary" style="font-size: 0.75rem;">
            <PhSparkle size="12" /> Mới
          </span>
        </div>
        <span class="badge-caramel small" style="background: var(--bakery-bg); color: var(--bakery-text-sub)">Đã bán {{ product.totalSold || 0 }}</span>
      </div>

      <div class="mt-auto pt-2">
        <div class="text-bakery fs-5 fw-bold mb-3">{{ formatPrice(product.minPrice) }}</div>
        
        <button 
          class="btn btn-bakery w-100 fw-bold d-flex align-items-center justify-content-center gap-2 position-relative overflow-hidden" 
          @click.stop="handleAddToCart">
          <transition name="slide-up" mode="out-in">
            <span v-if="showCheckmark" class="d-flex align-items-center gap-2">
              <PhCheckCircle size="20" weight="fill" /> Đã thêm
            </span>
            <span v-else class="d-flex align-items-center gap-2">
              <PhShoppingCart size="20" weight="bold" /> GIỎ HÀNG
            </span>
          </transition>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { PhStar, PhHeart, PhEye, PhShoppingCart, PhCheckCircle, PhTrendUp, PhSparkle } from '@phosphor-icons/vue'
import { useCartStore } from '@/stores/cart.store'

const props = defineProps({ product: { type: Object, required: true } })

const cartStore = useCartStore()
const isLiked = ref(false)
const showCheckmark = ref(false)

function formatPrice(p) { 
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) 
}

function toggleHeart() {
  isLiked.value = !isLiked.value
}

async function handleAddToCart() {
  if (showCheckmark.value) return; // Đang thêm

  try {
    await cartStore.addToCart({
      productId: props.product.productId,
      variantId: props.product.productId, // Fallback nếu API cần variantId
      name: props.product.name,
      price: props.product.minPrice || props.product.price,
      image: props.product.primaryImageUrl,
      quantity: 1
    })

    showCheckmark.value = true
    setTimeout(() => {
      showCheckmark.value = false
    }, 1500)
  } catch (error) {
    console.error('Thêm giỏ hàng thất bại:', error)
  }
}
</script>

<style scoped>
.product-card { 
  cursor: pointer; 
  padding: 0; 
  overflow: hidden; 
  display: flex; 
  flex-direction: column; 
  border: 1px solid rgba(0,0,0,0.05);
  transition: all 0.3s ease;
}
.product-card:hover {
  box-shadow: 0 10px 20px rgba(0,0,0,0.08);
}

.title-text {
  transition: color 0.2s;
}
.product-card:hover .title-text {
  color: var(--bakery-primary) !important;
}

.product-img-wrapper { 
  overflow: hidden; 
  padding: 12px 12px 0 12px; 
  background: white;
}
.product-card-img { 
  transition: transform 0.4s cubic-bezier(0.165, 0.84, 0.44, 1); 
  border-radius: 12px; 
  height: 200px; 
  object-fit: cover; 
}

/* Hover effects */
.product-card:hover .product-card-img { 
  transform: scale(1.08); 
}

/* Quick view overlay */
.quick-view-overlay {
  position: absolute;
  top: 12px; left: 12px; right: 12px; bottom: 0;
  background: rgba(0,0,0,0.2);
  border-radius: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}
.product-card:hover .quick-view-overlay {
  opacity: 1;
}

/* Heart button */
.heart-btn {
  transition: transform 0.2s;
}
.heart-btn:active {
  transform: scale(0.9);
}

.badge-caramel { 
  padding: 4px 8px; 
  border-radius: var(--radius-badge); 
  font-size: 0.75rem; 
  font-weight: 500;
}

/* Transition for Add to Cart */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.2s ease-out;
}
.slide-up-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
