<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div class="position-relative" style="width: 300px;">
        <input v-model="searchKeyword" @keyup.enter="fetchProducts" type="text" class="form-control bakery-input" placeholder="Tìm kiếm sản phẩm..." />
        <PhMagnifyingGlass class="position-absolute" style="top:50%;right:12px;transform:translateY(-50%);color:var(--text-muted)" size="18"/>
      </div>
      <router-link to="/admin/products/new" class="btn btn-bakery d-flex align-items-center gap-2">
        <PhPlus size="18" weight="bold" /> Thêm sản phẩm
      </router-link>
    </div>

    <!-- Error State -->
    <div v-if="error" class="error-state">
      <div class="error-state-icon mx-auto"><PhWarningCircle size="32" color="var(--color-danger)" /></div>
      <div class="error-state-title">Lỗi tải sản phẩm</div>
      <p class="error-state-text">{{ error }}</p>
      <button class="btn btn-bakery btn-sm" @click="error=null; fetchProducts()">Thử lại</button>
    </div>

    <div v-else class="bakery-card bakery-card--static">
      <!-- Loading Skeleton -->
      <div v-if="loading" class="table-responsive">
        <table class="admin-table">
          <thead><tr><th>Ảnh</th><th>Tên</th><th>Danh mục</th><th>Giá</th><th>Tồn kho</th><th>Trạng thái</th><th>Hành động</th></tr></thead>
          <tbody>
            <tr v-for="i in 5" :key="i">
              <td><div class="skeleton" style="width:50px;height:50px;border-radius:12px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:140px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:80px;height:24px;border-radius:999px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:90px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:40px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:70px;height:24px;border-radius:999px"></div></td>
              <td><div class="d-flex gap-1"><div class="skeleton" style="width:32px;height:32px;border-radius:8px"></div><div class="skeleton" style="width:32px;height:32px;border-radius:8px"></div></div></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Empty State -->
      <div v-else-if="products.length === 0" class="empty-state py-5">
        <div class="empty-state-icon mx-auto"><PhCake size="36" color="var(--text-muted)" /></div>
        <div class="empty-state-title">Chưa có sản phẩm nào</div>
        <p class="empty-state-text">Thêm sản phẩm đầu tiên để bắt đầu bán hàng</p>
        <router-link to="/admin/products/new" class="btn btn-bakery btn-sm">Thêm sản phẩm</router-link>
      </div>

      <!-- Data Table -->
      <div v-else class="table-responsive">
        <table class="admin-table">
          <thead><tr>
            <th>Ảnh</th>
            <th class="sortable" @click="toggleSort('name')">Tên <PhCaretUpDown size="12" class="sort-icon" /></th>
            <th>Danh mục</th>
            <th class="sortable" @click="toggleSort('price')">Giá <PhCaretUpDown size="12" class="sort-icon" /></th>
            <th class="sortable" @click="toggleSort('stock')">Tồn kho <PhCaretUpDown size="12" class="sort-icon" /></th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr></thead>
          <tbody>
            <tr v-for="p in sortedProducts" :key="p.productId">
              <td><img :src="p.primaryImageUrl || '/images/cake.png'" style="width:50px;height:50px;object-fit:cover;border-radius:12px;border:1px solid var(--border-light)"/></td>
              <td class="fw-semibold" style="color: var(--text-main)">{{ p.name }}</td>
              <td><span class="badge-purple">{{ p.categoryName || '-' }}</span></td>
              <td style="color: var(--text-main)">{{ formatPrice(p.minPrice) }}</td>
              <td>
                <span :class="p.totalStock > 20 ? 'badge-green' : p.totalStock > 10 ? 'badge-honey' : 'badge-pink'" class="fw-bold">
                  {{ p.totalStock ?? '-' }}
                </span>
              </td>
              <td><span :class="p.isAvailable !== false ? 'badge-green' : 'badge bg-secondary'" style="font-size:0.8rem">{{ p.isAvailable !== false ? 'Đang bán' : 'Ẩn' }}</span></td>
              <td>
                <div class="d-flex gap-1">
                  <router-link :to="`/admin/products/${p.productId}/edit`" class="btn btn-sm btn-bakery-outline" title="Sửa" style="padding:6px 10px"><PhPencilSimple size="18" /></router-link>
                  <button class="btn btn-sm btn-bakery-outline" @click="toggleProduct(p.productId)" title="Ẩn/Hiện" style="padding:6px 10px">
                    <component :is="p.isAvailable !== false ? PhEye : PhEyeClosed" size="18" />
                  </button>
                  <button class="btn btn-sm" style="padding:6px 10px;background:var(--color-danger-light);color:var(--color-danger);border:1px solid var(--color-danger);border-radius:var(--radius-sm)" @click="confirmDelete(p)" title="Xoá">
                    <PhTrash size="18" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="mt-3"><Pagination v-model="page" :total-pages="totalPages" @update:model-value="fetchProducts" /></div>
    </div>

    <!-- Custom Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="modal-overlay d-flex align-items-center justify-content-center" @click.self="showDeleteModal = false">
      <div class="bakery-card p-4 shadow-lg text-center" style="max-width: 400px; width: 90%;">
        <div class="text-danger mb-3" style="font-size: 2.5rem;">⚠️</div>
        <h5 class="fw-bold mb-3" style="color: var(--text-main)">Xác nhận xóa</h5>
        <p class="text-sub mb-4">Bạn có chắc muốn xóa <strong>{{ deleteItemName }}</strong>? Hành động này không thể hoàn tác.</p>
        <div class="d-flex gap-2 justify-content-center">
          <button class="btn btn-light px-4 fw-semibold" @click="showDeleteModal = false">Hủy</button>
          <button class="btn btn-danger px-4 fw-semibold" @click="handleDeleteConfirm">Xác nhận xóa</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { productApi } from '@/api/product.api'
import { adminApi } from '@/api/admin.api'
import { toast } from 'vue3-toastify'
import Pagination from '@/components/Pagination.vue'
import { PhPencilSimple, PhEye, PhEyeClosed, PhTrash, PhPlus, PhWarningCircle, PhCake, PhCaretUpDown, PhMagnifyingGlass } from '@phosphor-icons/vue'

const products = ref([])
const page = ref(0)
const totalPages = ref(0)
const loading = ref(true)
const error = ref(null)
const sortKey = ref('')
const sortDir = ref('asc')
const searchKeyword = ref('')

const showDeleteModal = ref(false)
const deleteItemId = ref(null)
const deleteItemName = ref('')

function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }

function toggleSort(key) {
  if (sortKey.value === key) { sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc' }
  else { sortKey.value = key; sortDir.value = 'asc' }
}

const sortedProducts = computed(() => {
  if (!sortKey.value) return products.value
  const items = [...products.value]
  const dir = sortDir.value === 'asc' ? 1 : -1
  return items.sort((a, b) => {
    if (sortKey.value === 'name') return dir * a.name.localeCompare(b.name)
    if (sortKey.value === 'price') return dir * ((a.minPrice || 0) - (b.minPrice || 0))
    if (sortKey.value === 'stock') return dir * ((a.totalStock || 0) - (b.totalStock || 0))
    return 0
  })
})

async function fetchProducts() {
  loading.value = true; error.value = null
  try {
    const params = { page: page.value, size: 20 }
    if (searchKeyword.value.trim()) params.search = searchKeyword.value.trim()
    const { data } = await productApi.getProducts(params)
    products.value = data.data?.content || []
    totalPages.value = data.data?.totalPages || 0
  } catch (err) {
    error.value = err.response?.data?.message || 'Không thể tải sản phẩm'
    products.value = []
  } finally { loading.value = false }
}

async function toggleProduct(id) {
  try { await adminApi.toggleProduct(id); toast.success('Đã cập nhật'); fetchProducts() }
  catch (err) { toast.error(err.response?.data?.message || 'Thất bại') }
}

function confirmDelete(p) {
  deleteItemId.value = p.productId
  deleteItemName.value = p.name
  showDeleteModal.value = true
}

async function handleDeleteConfirm() {
  try {
    await adminApi.deleteProduct(deleteItemId.value)
    toast.success('Đã xoá sản phẩm')
    showDeleteModal.value = false
    fetchProducts()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Xoá sản phẩm thất bại')
  }
}

onMounted(fetchProducts)
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 2000;
}
</style>
