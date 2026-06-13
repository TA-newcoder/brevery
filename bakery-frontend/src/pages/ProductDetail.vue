<template>
  <div class="container py-4">
    <button class="btn btn-sm text-decoration-none text-dark p-0 mb-3 d-flex align-items-center gap-2" @click="router.back()">
      <PhArrowLeft size="18" weight="bold" /> Quay lại
    </button>
    <div v-if="loading" class="row g-4"><div class="col-md-6"><div class="skeleton" style="height:400px"></div></div><div class="col-md-6"><div class="skeleton" style="height:400px"></div></div></div>
    <div v-else-if="product" class="row g-4">
      <!-- Images -->
      <div class="col-md-6">
        <div class="bakery-card p-0 overflow-hidden">
          <img :src="mainImage" :alt="product.name" class="w-100" style="height: 400px; object-fit: cover" />
          <div class="d-flex gap-2 p-3" v-if="product.images?.length > 1">
            <img v-for="img in product.images" :key="img.imageId" :src="img.imageUrl" @click="mainImage = img.imageUrl"
              :class="['thumb', { active: mainImage === img.imageUrl }]"
              style="width:60px; height:60px; object-fit:cover; border-radius:12px; cursor:pointer" />
          </div>
        </div>
      </div>

      <!-- Details -->
      <div class="col-md-6">
        <span class="badge-caramel mb-3 d-inline-block">{{ product.categoryName }}</span>
        <h2 class="fw-bold mb-2">{{ product.name }}</h2>
        <div class="d-flex align-items-center gap-3 mb-3">
          <div class="d-flex align-items-center gap-1">
            <PhStar weight="fill" color="#f5a623" size="18" />
            <span class="fw-bold" style="color: #f5a623">{{ product.avgRating?.toFixed(1) || '0.0' }}</span>
          </div>
          <span class="text-sub small">({{ product.reviewCount || 0 }} đánh giá)</span>
          <span class="text-sub small">Đã bán {{ product.totalSold || 0 }}</span>
        </div>

        <!-- Variants -->
        <div class="mb-3">
          <label class="form-label small fw-semibold">Chọn loại:</label>
          <div class="d-flex flex-wrap gap-2">
            <button v-for="v in product.variants" :key="v.variantId"
              :class="['btn btn-sm', selectedVariant?.variantId === v.variantId ? 'btn-bakery' : 'btn-bakery-outline']"
              :disabled="v.stock <= 0"
              @click="selectedVariant = v">
              {{ v.variantName }} — {{ formatPrice(v.price) }}
              <span v-if="v.stock <= 0" class="ms-1">(Hết)</span>
            </button>
          </div>
        </div>

        <!-- Price -->
        <div class="mb-4">
          <span class="fs-3 fw-bold text-bakery">{{ formatPrice(selectedVariant?.price || product.minPrice) }}</span>
          <span v-if="selectedVariant" class="text-sub small ms-2">Còn {{ selectedVariant.stock }} sản phẩm</span>
        </div>

        <!-- Quantity + Add -->
        <div class="d-flex gap-3 align-items-center mb-4">
          <div class="d-flex align-items-center gap-2">
            <button class="btn btn-bakery-outline btn-sm" @click="qty = Math.max(1, qty-1)">−</button>
            <span class="fw-bold px-3">{{ qty }}</span>
            <button class="btn btn-bakery-outline btn-sm" @click="qty++">+</button>
          </div>
          <button class="btn btn-bakery flex-grow-1 d-flex align-items-center justify-content-center gap-2 fw-bold" @click="handleAddToCart" :disabled="!selectedVariant">
            <PhShoppingCart weight="bold" size="20" /> Thêm vào giỏ
          </button>
        </div>

        <!-- Description -->
        <div class="bakery-card bg-bakery-light">
          <h6 class="fw-bold mb-2">Mô tả</h6>
          <p class="text-sub mb-0" style="white-space: pre-line">{{ product.description || 'Chưa có mô tả' }}</p>
        </div>
      </div>
    </div>

    <!-- Reviews -->
    <div v-if="product" class="mt-5">
      <h4 class="fw-bold mb-3 d-flex align-items-center gap-2"><PhChatCircle weight="bold" size="24" color="var(--bakery-primary)" /> Đánh giá ({{ product.reviewCount || 0 }})</h4>
      <div v-for="r in reviews" :key="r.reviewId" class="bakery-card mb-3">
        <div class="d-flex justify-content-between">
          <div>
            <span class="fw-semibold">{{ r.userName }}</span>
            <span class="ms-2 d-inline-flex gap-1">
              <PhStar v-for="i in r.rating" :key="i" weight="fill" color="#f5a623" size="14" />
            </span>
          </div>
          <span class="text-sub small">{{ formatDate(r.createdAt) }}</span>
        </div>
        <p class="mb-0 mt-2 text-sub">{{ r.comment }}</p>
        <div v-if="r.adminReply" class="mt-2 p-2 rounded small" style="background: var(--bg-muted); border-left: 3px solid var(--bakery-primary)">
          <span class="fw-semibold text-main">Brevery Shop:</span> {{ r.adminReply }}
        </div>
      </div>
      <Pagination v-model="reviewPage" :total-pages="reviewTotalPages" @update:model-value="fetchReviews" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api/product.api'
import { useCartStore } from '@/stores/cart.store'
import Pagination from '@/components/Pagination.vue'
import { PhStar, PhShoppingCart, PhChatCircle, PhArrowLeft } from '@phosphor-icons/vue'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const product = ref(null)
const loading = ref(true)
const mainImage = ref('')
const selectedVariant = ref(null)
const qty = ref(1)
const reviews = ref([])
const reviewPage = ref(0)
const reviewTotalPages = ref(0)

function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }
function formatDate(d) { return dayjs(d).format('DD/MM/YYYY') }

async function fetchProduct() {
  loading.value = true
  try {
    const { data } = await productApi.getProductDetail(route.params.id)
    product.value = data.data
    mainImage.value = data.data.primaryImageUrl || data.data.images?.[0]?.imageUrl || '/images/cake.png'
    if (data.data.variants?.length) selectedVariant.value = data.data.variants[0]
  } catch { product.value = null }
  finally { loading.value = false }
}

async function fetchReviews() {
  try {
    const { data } = await productApi.getReviews(route.params.id, { page: reviewPage.value, size: 5 })
    reviews.value = data.data?.content || []
    reviewTotalPages.value = data.data?.totalPages || 0
  } catch { reviews.value = [] }
}

function handleAddToCart() {
  if (!selectedVariant.value) return
  cartStore.addToCart({
    variantId: selectedVariant.value.variantId,
    quantity: qty.value,
    productName: product.value.name,
    variantName: selectedVariant.value.variantName,
    price: selectedVariant.value.price,
    imageUrl: mainImage.value,
  })
}

onMounted(() => { fetchProduct(); fetchReviews() })
watch(() => route.params.id, () => { fetchProduct(); fetchReviews() })
</script>

<style scoped>
.thumb { border: 2px solid transparent; transition: border-color .15s; }
.thumb.active { border-color: var(--bakery-primary); }
</style>
