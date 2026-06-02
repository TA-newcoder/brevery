<template>
  <div class="container py-4">
    <!-- Breadcrumb / Header -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div class="d-flex align-items-center gap-3">
        <button class="btn btn-light d-flex align-items-center justify-content-center rounded-circle border p-2 shadow-sm" style="width: 40px; height: 40px;" @click="router.back()" title="Quay lại">
          <PhArrowLeft size="20" weight="bold" />
        </button>
        <h2 class="fw-bold mb-0 d-flex align-items-center gap-2">
          <PhStorefront weight="fill" color="var(--bakery-primary)" /> Sản phẩm
        </h2>
      </div>
      <!-- Nút mở Offcanvas trên Mobile -->
      <button class="btn btn-bakery-outline d-lg-none d-flex align-items-center gap-2" data-bs-toggle="offcanvas" data-bs-target="#filterOffcanvas">
        <PhFaders size="20" weight="bold" /> Bộ lọc
      </button>
    </div>

    <div class="row g-4">
      <!-- SIDEBAR (Desktop) -->
      <div class="col-lg-3 d-none d-lg-block">
        <div class="bakery-card p-3 sticky-top" style="top: 80px; z-index: 10;">
          
          <!-- Tìm kiếm -->
          <h6 class="fw-bold mb-2">Tìm kiếm</h6>
          <div class="position-relative mb-4">
            <input v-model="filters.search" type="text" class="bakery-input pe-4" placeholder="Tên bánh, đồ uống..." @input="debouncedFetch" />
            <span class="position-absolute" style="top: 50%; right: 12px; transform: translateY(-50%); color: var(--bakery-text-sub);">
              <PhMagnifyingGlass size="18" />
            </span>
          </div>

          <!-- Danh mục -->
          <h6 class="fw-bold mb-2">Danh mục</h6>
          <div class="list-group list-group-flush mb-4">
            <button class="list-group-item list-group-item-action py-2 px-3 border-0 rounded-3 mb-1" 
              :class="{ 'active fw-bold text-bakery bg-bakery-light': !filters.categoryId }" @click="setCategory(null)">
              Tất cả sản phẩm
            </button>
            <button v-for="cat in categories" :key="cat.categoryId"
              class="list-group-item list-group-item-action py-2 px-3 border-0 rounded-3 mb-1"
              :class="{ 'active fw-bold text-bakery bg-bakery-light': filters.categoryId === cat.categoryId }" @click="setCategory(cat.categoryId)">
              {{ cat.name }}
            </button>
          </div>

          <!-- Sắp xếp -->
          <h6 class="fw-bold mb-2">Sắp xếp theo</h6>
          <select v-model="filters.sortBy" class="bakery-input mb-4" @change="fetchProducts">
            <option value="newest">Mới nhất</option>
            <option value="best_selling">Bán chạy nhất</option>
            <option value="price_asc">Giá: Thấp đến Cao</option>
            <option value="price_desc">Giá: Cao đến Thấp</option>
          </select>

          <!-- Khoảng giá -->
          <h6 class="fw-bold mb-2">Khoảng giá</h6>
          <div class="d-flex align-items-center gap-2 mb-2">
            <input v-model="filters.minPrice" type="number" class="bakery-input px-2 text-center" placeholder="Từ" />
            <span>-</span>
            <input v-model="filters.maxPrice" type="number" class="bakery-input px-2 text-center" placeholder="Đến" />
          </div>
          <button class="btn btn-bakery-outline w-100 mb-4 btn-sm fw-bold" @click="fetchProducts">Áp dụng giá</button>

          <!-- Tình trạng -->
          <h6 class="fw-bold mb-2">Tình trạng</h6>
          <div class="form-check form-switch mb-4">
            <input class="form-check-input" type="checkbox" id="stockSwitchDesk" v-model="filters.inStockOnly" @change="fetchProducts">
            <label class="form-check-label small" for="stockSwitchDesk">Chỉ hiện sản phẩm còn hàng</label>
          </div>

          <!-- Xóa lọc -->
          <button v-if="hasActiveFilters" class="btn btn-light w-100 text-danger fw-semibold" @click="resetFilters">Xóa toàn bộ lọc</button>
        </div>
      </div>

      <!-- MAIN CONTENT: LƯỚI SẢN PHẨM -->
      <div class="col-lg-9">
        <!-- Badge bộ lọc đang active -->
        <div class="d-flex flex-wrap gap-2 mb-3" v-if="hasActiveFilters">
          <span v-if="filters.search" class="badge bg-light text-dark border d-flex align-items-center gap-1 p-2">
            Từ khóa: {{ filters.search }} <PhX class="cursor-pointer" @click="filters.search=''; fetchProducts()" />
          </span>
          <span v-if="filters.categoryId" class="badge bg-light text-dark border d-flex align-items-center gap-1 p-2">
            Danh mục: {{ categories.find(c => c.categoryId === filters.categoryId)?.name || '...' }} 
            <PhX class="cursor-pointer" @click="filters.categoryId=null; fetchProducts()" />
          </span>
          <span v-if="filters.minPrice || filters.maxPrice" class="badge bg-light text-dark border d-flex align-items-center gap-1 p-2">
            Giá: {{ filters.minPrice || 0 }} - {{ filters.maxPrice || '...' }} 
            <PhX class="cursor-pointer" @click="filters.minPrice=''; filters.maxPrice=''; fetchProducts()" />
          </span>
          <span v-if="filters.inStockOnly" class="badge bg-light text-dark border d-flex align-items-center gap-1 p-2">
            Chỉ còn hàng <PhX class="cursor-pointer" @click="filters.inStockOnly=false; fetchProducts()" />
          </span>
        </div>

        <div class="row g-4">
          <div v-for="p in products" :key="p.productId" class="col-6 col-md-4">
            <ProductCard :product="p" />
          </div>
          
          <!-- Skeletons -->
          <div v-if="loading" v-for="i in 6" :key="'sk'+i" class="col-6 col-md-4">
            <div class="skeleton" style="height: 280px"></div>
          </div>
          
          <!-- No results -->
          <div v-if="!loading && products.length === 0" class="col-12 text-center py-5 bakery-card">
            <span style="font-size: 3rem">🔍</span>
            <p class="text-sub mt-2 mb-0">Không tìm thấy sản phẩm nào phù hợp</p>
          </div>
        </div>

        <!-- Pagination -->
        <div class="mt-4 d-flex justify-content-center" v-if="totalPages > 1">
          <Pagination v-model="page" :total-pages="totalPages" @update:model-value="fetchProducts" />
        </div>
      </div>
    </div>

    <!-- OFFCANVAS BỘ LỌC CHO MOBILE -->
    <div class="offcanvas offcanvas-end d-lg-none" tabindex="-1" id="filterOffcanvas" aria-labelledby="filterOffcanvasLabel">
      <div class="offcanvas-header border-bottom">
        <h5 class="offcanvas-title fw-bold d-flex align-items-center gap-2" id="filterOffcanvasLabel">
          <PhFaders size="24" weight="fill" color="var(--bakery-primary)" /> Bộ lọc nâng cao
        </h5>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
      </div>
      <div class="offcanvas-body pb-5"> <!-- Thêm pb-5 để tránh chat widget -->
        
        <div class="mb-4">
          <h6 class="fw-bold mb-2">Tìm kiếm</h6>
          <input v-model="filters.search" type="text" class="bakery-input" placeholder="Tên bánh, đồ uống..." />
        </div>

        <div class="mb-4">
          <h6 class="fw-bold mb-2">Danh mục</h6>
          <select v-model="filters.categoryId" class="bakery-input">
            <option :value="null">Tất cả sản phẩm</option>
            <option v-for="cat in categories" :key="cat.categoryId" :value="cat.categoryId">{{ cat.name }}</option>
          </select>
        </div>

        <div class="mb-4">
          <h6 class="fw-bold mb-2">Sắp xếp theo</h6>
          <select v-model="filters.sortBy" class="bakery-input">
            <option value="newest">Mới nhất</option>
            <option value="best_selling">Bán chạy nhất</option>
            <option value="price_asc">Giá: Thấp đến Cao</option>
            <option value="price_desc">Giá: Cao đến Thấp</option>
          </select>
        </div>

        <div class="mb-4">
          <h6 class="fw-bold mb-2">Khoảng giá</h6>
          <div class="d-flex align-items-center gap-2">
            <input v-model="filters.minPrice" type="number" class="bakery-input" placeholder="TỪ" />
            <span>-</span>
            <input v-model="filters.maxPrice" type="number" class="bakery-input" placeholder="ĐẾN" />
          </div>
        </div>

        <div class="mb-4">
          <h6 class="fw-bold mb-2">Tình trạng</h6>
          <div class="form-check form-switch">
            <input class="form-check-input" type="checkbox" id="stockSwitchMob" v-model="filters.inStockOnly">
            <label class="form-check-label" for="stockSwitchMob">Chỉ hiện sản phẩm còn hàng</label>
          </div>
        </div>
        
        <!-- Khoảng trống đẩy footer -->
        <div style="height: 60px;"></div> 
      </div>

      <div class="offcanvas-footer p-3 border-top d-flex gap-2 bg-light position-absolute bottom-0 w-100" style="z-index: 1050; padding-bottom: 20px !important;">
        <button class="btn btn-light w-50 fw-semibold" @click="resetFilters" data-bs-dismiss="offcanvas">Xóa bộ lọc</button>
        <button class="btn btn-bakery w-50 fw-bold" data-bs-dismiss="offcanvas" @click="fetchProducts">Xem kết quả</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api/product.api'
import ProductCard from '@/components/ProductCard.vue'
import Pagination from '@/components/Pagination.vue'
import { PhStorefront, PhFaders, PhArrowLeft, PhMagnifyingGlass, PhX } from '@phosphor-icons/vue'

const route = useRoute()
const router = useRouter()
const products = ref([])
const categories = ref([])
const loading = ref(false)
const page = ref(0)
const totalPages = ref(0)

const filters = reactive({
  search: route.query.search || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : null,
  minPrice: '',
  maxPrice: '',
  sortBy: 'newest',
  inStockOnly: false,
})

const hasActiveFilters = computed(() => {
  return filters.search || filters.categoryId || filters.minPrice || filters.maxPrice || filters.inStockOnly || filters.sortBy !== 'newest'
})

let debounceTimer = null
function debouncedFetch() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => { page.value = 0; fetchProducts() }, 500)
}

function setCategory(id) {
  filters.categoryId = id
  page.value = 0
  fetchProducts()
}

function resetFilters() {
  filters.search = ''
  filters.categoryId = null
  filters.minPrice = ''
  filters.maxPrice = ''
  filters.sortBy = 'newest'
  filters.inStockOnly = false
  page.value = 0
  fetchProducts()
}

async function fetchCategories() {
  try {
    const { data } = await productApi.getCategories()
    categories.value = data.data || []
  } catch (err) {
    console.error('Categories API lỗi:', err.response?.status, err.response?.data)
    // Fallback tĩnh để UI không bị trống
    categories.value = [
      { categoryId: 1, name: 'Bánh kem' },
      { categoryId: 2, name: 'Bánh ngọt' },
      { categoryId: 3, name: 'Đồ uống' }
    ]
  }
}

async function fetchProducts() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: 12,
      sortBy: filters.sortBy
    }
    
    // Xử lý tham số an toàn (tránh truyền rỗng/null)
    if (filters.search && filters.search.trim() !== '') params.search = filters.search.trim()
    if (filters.categoryId) params.categoryId = filters.categoryId
    if (filters.minPrice !== '' && filters.minPrice !== null) params.minPrice = Number(filters.minPrice)
    if (filters.maxPrice !== '' && filters.maxPrice !== null) params.maxPrice = Number(filters.maxPrice)
    if (filters.inStockOnly) params.inStockOnly = true

    const { data } = await productApi.getProducts(params)
    products.value = data.data?.content || []
    totalPages.value = data.data?.totalPages || 0
  } catch (err) {
    console.error('Failed to load products', err)
    products.value = []
  } finally { 
    loading.value = false 
  }
}

watch(() => route.query, (q) => {
  if (q.search !== undefined) filters.search = q.search
  if (q.categoryId !== undefined) filters.categoryId = q.categoryId ? Number(q.categoryId) : null
  page.value = 0
  fetchProducts()
})

onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped>
.list-group-item {
  transition: all 0.2s ease;
  font-size: 0.95rem;
  color: var(--bakery-text);
}
.list-group-item:hover {
  background: var(--bakery-primary-light);
  color: var(--bakery-primary);
}
.cursor-pointer {
  cursor: pointer;
}
.badge {
  font-weight: 500;
  font-size: 0.85rem;
}
</style>
