<template>
  <div class="magic-product-card h-100" @click="$router.push({ name: 'product-detail', params: { id: product.productId } })">
    <div class="magic-product-card-info position-relative h-100 d-flex flex-column">
      
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
        <button class="btn btn-light rounded-circle p-1 shadow-sm border-0 heart-btn" :class="{ 'heart-pop': heartPop }" @click.stop="toggleHeart" :title="isLiked ? 'Bỏ yêu thích' : 'Thêm vào yêu thích'" :aria-pressed="isLiked">
          <PhHeart :weight="isLiked ? 'fill' : 'regular'" color="#C8502A" size="20" />
        </button>
      </div>

      <!-- IMAGE WRAPPER -->
      <div class="product-img-wrapper position-relative">
        <img :src="product.primaryImageUrl || '/images/cake.png'" :alt="product.name" class="product-card-img w-100" />
        <!-- OVERLAY QUICK VIEW -->
        <div class="quick-view-overlay d-flex align-items-center justify-content-center">
          <span class="btn btn-light rounded-pill px-3 fw-semibold shadow-sm d-flex align-items-center gap-2">
            <PhEye size="18" weight="bold" /> Xem nhanh
          </span>
        </div>
      </div>

      <!-- CONTENT -->
      <div class="p-3 d-flex flex-column flex-grow-1 card-content-area">
        <h6 class="fw-bold mb-2 text-truncate title-text">{{ product.name }}</h6>
        
        <div class="d-flex justify-content-between align-items-center mb-3">
          <div class="d-flex align-items-center gap-1 small" style="color: var(--card-dark-sub)">
            <PhStar weight="fill" color="#f5a623" size="14" />
            <span v-if="product.avgRating > 0">{{ product.avgRating.toFixed(1) }}</span>
            <span v-else class="fw-semibold d-flex align-items-center gap-1" style="font-size: 0.75rem; color: var(--card-dark-accent);">
              <PhSparkle size="12" /> Mới
            </span>
          </div>
          <span class="badge-caramel small" style="background: var(--bg-muted); color: var(--card-dark-sub);">Đã bán {{ product.totalSold || 0 }}</span>
        </div>

        <div class="mt-auto pt-2">
          <div class="fs-5 fw-bold mb-3" style="color: var(--card-dark-accent);">{{ formatPrice(product.minPrice) }}</div>
          
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { PhStar, PhHeart, PhEye, PhShoppingCart, PhCheckCircle, PhTrendUp, PhSparkle } from '@phosphor-icons/vue'
import { useCartStore } from '@/stores/cart.store'
import { toast } from 'vue3-toastify'

const props = defineProps({ product: { type: Object, required: true } })

const cartStore = useCartStore()
const isLiked = ref(false)
const heartPop = ref(false)
const showCheckmark = ref(false)

const WISHLIST_KEY = 'brevery-wishlist'
function readWishlist() {
  try { return JSON.parse(localStorage.getItem(WISHLIST_KEY)) || [] } catch { return [] }
}
function writeWishlist(ids) {
  localStorage.setItem(WISHLIST_KEY, JSON.stringify(ids))
}

onMounted(() => {
  isLiked.value = readWishlist().includes(props.product.productId)
})

function formatPrice(p) { 
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) 
}

function toggleHeart() {
  isLiked.value = !isLiked.value
  const ids = readWishlist()
  const id = props.product.productId
  if (isLiked.value) {
    if (!ids.includes(id)) ids.push(id)
    heartPop.value = true
    setTimeout(() => { heartPop.value = false }, 350)
    toast.success('Đã thêm vào yêu thích ❤️')
  } else {
    writeWishlist(ids.filter(x => x !== id))
    toast.info('Đã bỏ khỏi yêu thích')
    return
  }
  writeWishlist(ids)
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
.magic-product-card {
  --background: linear-gradient(135deg, var(--primary) 0%, #c0392b 100%);
  padding: 3px;
  border-radius: 16px;
  overflow: visible;
  background: var(--background);
  position: relative;
  z-index: 1;
  cursor: pointer;
  transform-style: preserve-3d;
  perspective: 1000px;
  transition: all 0.45s var(--ease-spring);
}
.magic-product-card::after {
  position: absolute;
  content: "";
  top: 15px; left: 0; right: 0;
  z-index: -1;
  height: 100%; width: 100%;
  transform: scale(0.85);
  filter: blur(20px);
  background: var(--background);
  opacity: 0;
  transition: opacity 0.45s var(--ease-smooth);
}
.magic-product-card:hover {
  transform: translateY(-8px) translateZ(10px) scale(1.02);
}
.magic-product-card:hover::after {
  opacity: 0.65;
}
.magic-product-card-info {
  background: var(--card-dark-bg);
  color: var(--card-dark-text);
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 13px;
  border: 1px solid var(--card-dark-border);
  transition: background-color var(--duration-slow) var(--ease-smooth),
              color var(--duration-slow) var(--ease-smooth),
              border-color var(--duration-slow) var(--ease-smooth);
}

.card-content-area {
  border-radius: 0 0 12px 12px;
  z-index: 1;
  background: var(--card-dark-bg);
  transition: background-color var(--duration-slow) var(--ease-smooth);
}

.title-text {
  color: var(--card-dark-text);
  transition: color 0.25s var(--ease-smooth);
}
.magic-product-card:hover .title-text {
  color: var(--card-dark-accent) !important;
}

.product-img-wrapper { 
  overflow: hidden; 
  padding: 12px 12px 0 12px; 
  background: var(--card-dark-bg);
  transition: background-color var(--duration-slow) var(--ease-smooth);
}
.product-card-img { 
  transition: transform 0.45s var(--ease-spring); 
  border-radius: 12px; 
  height: 200px; 
  object-fit: cover; 
}

/* Hover effects */
.magic-product-card:hover .product-card-img { 
  transform: scale(1.08); 
}

/* Quick view overlay */
.quick-view-overlay {
  position: absolute;
  top: 12px; left: 12px; right: 12px; bottom: 0;
  background: rgba(0,0,0,0.2);
  border-radius: 12px;
  opacity: 0;
  transition: opacity 0.35s var(--ease-smooth);
  pointer-events: none;
}
.magic-product-card:hover .quick-view-overlay {
  opacity: 1;
}

/* Heart button */
.heart-btn {
  transition: transform 0.2s var(--ease-smooth);
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
  transition: all 0.25s var(--ease-smooth);
}
.slide-up-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
@media (prefers-reduced-motion: reduce) {
  .magic-product-card {
    transition: none !important;
    transform: none !important;
  }
  .magic-product-card::after {
    display: none !important;
  }
  .product-card-img {
    transition: none !important;
    transform: none !important;
  }
}
</style>
