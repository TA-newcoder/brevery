<template>
  <div class="admin-inventory">
    <div class="d-flex justify-content-end align-items-center mb-4 flex-wrap gap-3">
      <button class="btn btn-primary d-flex align-items-center gap-2" @click="openReceiptModal">
        <PhPlus size="20" weight="bold" /> Thêm Phiếu Nhập
      </button>
    </div>

    <div class="row g-4">
      <!-- CẢNH BÁO TỒN KHO -->
      <div class="col-lg-5">
        <div class="bakery-card h-100">
          <div class="d-flex align-items-center justify-content-between mb-4">
            <div class="d-flex align-items-center gap-2">
              <PhWarningCircle size="24" weight="fill" color="var(--color-warning)" />
              <h6 class="fw-bold mb-0" style="color: var(--text-main)">Cảnh báo sắp hết hàng</h6>
            </div>
            <button class="btn btn-sm btn-outline-secondary" @click="fetchLowStock" :disabled="isLoadingLowStock">
              <PhArrowsClockwise size="16" :class="{ 'spin': isLoadingLowStock }" />
            </button>
          </div>

          <div v-if="isLoadingLowStock" class="text-center py-4">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <div v-else-if="lowStockItems.length === 0" class="text-center py-4 text-sub">
            Mọi sản phẩm đều đủ số lượng tồn kho an toàn.
          </div>
          <div v-else class="low-stock-list">
            <div v-for="(item, index) in lowStockItems" :key="index" class="d-flex justify-content-between align-items-center p-3 rounded-3 mb-2" style="background: var(--bg-muted);">
              <div>
                <div class="fw-bold text-main">{{ item.productName }}</div>
                <div class="small text-sub">Size: {{ item.size }}</div>
              </div>
              <div class="d-flex align-items-center gap-3">
                <span class="badge" style="background: var(--color-warning-light); color: var(--color-warning);">
                  Còn {{ item.stock }}
                </span>
                <button class="btn btn-sm btn-bakery-ghost px-2" @click="quickImport(item)">Nhập</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- LỊCH SỬ PHIẾU NHẬP -->
      <div class="col-lg-7">
        <div class="bakery-card h-100">
          <h6 class="fw-bold mb-4" style="color: var(--text-main)">Lịch sử nhập kho</h6>
          
          <div v-if="isLoadingReceipts" class="text-center py-4">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <div v-else-if="receipts.length === 0" class="text-center py-4 text-sub">
            Chưa có phiếu nhập kho nào.
          </div>
          <div v-else class="table-responsive">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>Sản phẩm</th>
                  <th>Số lượng</th>
                  <th>Giá nhập</th>
                  <th>Thời gian</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="receipt in receipts" :key="receipt.receiptId">
                  <td>
                    <div class="fw-semibold">{{ receipt.productName }}</div>
                    <div class="small text-sub text-truncate" style="max-width: 150px;" :title="receipt.note">{{ receipt.note }}</div>
                  </td>
                  <td class="fw-bold" style="color: var(--color-success)">+{{ receipt.quantity }}</td>
                  <td>{{ formatPrice(receipt.importPrice) }}</td>
                  <td class="text-sub small">{{ formatDate(receipt.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <!-- Pagination -->
          <div class="d-flex justify-content-end mt-3" v-if="totalPages > 1">
            <button class="btn btn-sm btn-outline-secondary me-2" :disabled="currentPage === 0" @click="fetchReceipts(currentPage - 1)">Trang trước</button>
            <button class="btn btn-sm btn-outline-secondary" :disabled="currentPage >= totalPages - 1" @click="fetchReceipts(currentPage + 1)">Trang sau</button>
          </div>
        </div>
      </div>
    </div>

    <!-- DANH SÁCH TỒN KHO -->
    <div class="bakery-card mt-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h6 class="fw-bold mb-0" style="color: var(--text-main)">Danh sách Tồn Kho Toàn Bộ</h6>
        <div class="d-flex gap-2">
           <input type="text" class="form-control bakery-input form-control-sm" placeholder="Tìm sản phẩm..." style="width: 200px;" />
        </div>
      </div>
      <div class="table-responsive">
        <table class="admin-table">
          <thead>
            <tr>
              <th style="width: 60px">Ảnh</th>
              <th>Sản phẩm</th>
              <th>Danh mục</th>
              <th>Size / Tồn kho hiện tại</th>
              <th class="text-end">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in productOptions" :key="p.productId">
              <td>
                <img :src="p.imageUrl || 'https://placehold.co/100x100?text=No+Image'" class="rounded" style="width: 44px; height: 44px; object-fit: cover; border: 1px solid var(--border-color);" />
              </td>
              <td>
                <div class="fw-bold" style="color: var(--text-main)">{{ p.name }}</div>
                <div class="small text-muted">{{ p.variants?.length || 0 }} biến thể</div>
              </td>
              <td>
                <span class="badge bg-light text-dark border">{{ p.category?.name || '---' }}</span>
              </td>
              <td>
                <div class="d-flex flex-column gap-2 py-1">
                  <div v-for="v in p.variants" :key="v.variantId" class="d-flex align-items-center gap-2">
                    <span class="badge" style="background: var(--bg-surface); color: var(--text-main); border: 1px solid var(--border-color); width: 60px;">Size {{ v.size }}</span>
                    <div class="d-flex align-items-center gap-1" style="min-width: 80px;">
                      <span class="fw-bold" :class="v.stock < 5 ? 'text-danger' : (v.stock < 10 ? 'text-warning' : 'text-success')">
                        {{ v.stock }}
                      </span>
                      <span class="small text-muted">đơn vị</span>
                    </div>
                  </div>
                  <div v-if="!p.variants || p.variants.length === 0" class="small text-muted">Chưa có biến thể</div>
                </div>
              </td>
              <td class="text-end">
                <button class="btn btn-sm btn-bakery-ghost" @click="quickImport({ productId: p.productId, variantId: p.variants?.[0]?.variantId })">
                  <PhPlus /> Nhập
                </button>
              </td>
            </tr>
            <tr v-if="productOptions.length === 0">
              <td colspan="5" class="text-center py-4 text-muted">Không có dữ liệu sản phẩm</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- MODAL NHẬP KHO -->
    <div class="modal fade" id="receiptModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bakery-card p-0 overflow-hidden">
          <div class="modal-header border-0 bg-light pb-2">
            <h5 class="modal-title fw-bold" style="color: var(--text-main)">Tạo Phiếu Nhập Kho</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitReceipt">
            <div class="modal-body pt-2">
              <div class="mb-3">
                <label class="form-label text-sub fw-semibold small">Sản phẩm</label>
                <select class="form-select bakery-input" v-model="form.productId" @change="loadVariants" required>
                  <option value="" disabled>-- Chọn sản phẩm --</option>
                  <option v-for="p in productOptions" :key="p.productId" :value="p.productId">{{ p.name }}</option>
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label text-sub fw-semibold small">Biến thể (Size)</label>
                <select class="form-select bakery-input" v-model="form.variantId" required :disabled="!form.productId">
                  <option value="" disabled>-- Chọn size --</option>
                  <option v-for="v in variantOptions" :key="v.variantId" :value="v.variantId">Size {{ v.size }} (Tồn: {{ v.stock }})</option>
                </select>
              </div>
              <div class="row g-3 mb-3">
                <div class="col-6">
                  <label class="form-label text-sub fw-semibold small">Số lượng</label>
                  <input type="number" class="form-control bakery-input" v-model="form.quantity" min="1" required />
                </div>
                <div class="col-6">
                  <label class="form-label text-sub fw-semibold small">Giá nhập (VNĐ)</label>
                  <input type="number" class="form-control bakery-input" v-model="form.importPrice" min="0" required />
                </div>
              </div>
              <div class="mb-3">
                <label class="form-label text-sub fw-semibold small">Nhà cung cấp (Tùy chọn)</label>
                <input type="text" class="form-control bakery-input" v-model="form.supplier" />
              </div>
              <div class="mb-3">
                <label class="form-label text-sub fw-semibold small">Ghi chú</label>
                <textarea class="form-control bakery-input" v-model="form.note" rows="2"></textarea>
              </div>
            </div>
            <div class="modal-footer border-0">
              <button type="button" class="btn btn-outline-secondary px-4 fw-semibold" data-bs-dismiss="modal">Hủy</button>
              <button type="submit" class="btn btn-primary px-4 fw-semibold" :disabled="isSubmitting">
                <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2" role="status"></span>
                Nhập Kho
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
import { adminApi } from '@/api/admin.api'
import { productApi } from '@/api/product.api' // To fetch product details
import { toast } from 'vue3-toastify'
import { PhWarningCircle, PhPlus, PhArrowsClockwise } from '@phosphor-icons/vue'

const lowStockItems = ref([])
const isLoadingLowStock = ref(false)

const receipts = ref([])
const isLoadingReceipts = ref(false)
const currentPage = ref(0)
const totalPages = ref(1)

const productOptions = ref([])
const variantOptions = ref([])
const isSubmitting = ref(false)

const form = ref({
  productId: '',
  variantId: '',
  quantity: 1,
  importPrice: 0,
  supplier: '',
  note: ''
})

let modalInstance = null

onMounted(async () => {
  // Load bootstrap modal dynamically
  const { Modal } = await import('bootstrap')
  modalInstance = new Modal(document.getElementById('receiptModal'))
  
  fetchLowStock()
  fetchReceipts()
  fetchProductOptions()
})

const formatPrice = (p) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0)
const formatDate = (d) => {
  const date = new Date(d)
  return `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getFullYear()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const fetchLowStock = async () => {
  isLoadingLowStock.value = true
  try {
    const res = await adminApi.getLowStock(10)
    lowStockItems.value = res.data?.data || []
  } catch (error) {
    console.error(error)
  } finally {
    isLoadingLowStock.value = false
  }
}

const fetchReceipts = async (page = 0) => {
  isLoadingReceipts.value = true
  try {
    const res = await adminApi.getReceipts({ page, size: 5 })
    receipts.value = res.data?.data?.content || []
    currentPage.value = res.data?.data?.number || 0
    totalPages.value = res.data?.data?.totalPages || 1
  } catch (error) {
    console.error(error)
  } finally {
    isLoadingReceipts.value = false
  }
}

const fetchProductOptions = async () => {
  try {
    const res = await productApi.getProducts({ size: 100 })
    productOptions.value = res.data?.data?.content || []
  } catch (error) {
    console.error(error)
  }
}

const loadVariants = async () => {
  form.value.variantId = ''
  variantOptions.value = []
  if (!form.value.productId) return
  
  try {
    const res = await productApi.getProductDetail(form.value.productId)
    variantOptions.value = res.data?.data?.variants || []
  } catch (error) {
    console.error(error)
  }
}

const openReceiptModal = () => {
  form.value = { productId: '', variantId: '', quantity: 1, importPrice: 0, supplier: '', note: '' }
  variantOptions.value = []
  modalInstance?.show()
}

const quickImport = async (item) => {
  form.value.productId = item.productId
  await loadVariants()
  form.value.variantId = item.variantId
  form.value.quantity = 10
  form.value.importPrice = 0
  form.value.supplier = ''
  form.value.note = 'Bổ sung tồn kho khẩn cấp'
  modalInstance?.show()
}

const submitReceipt = async () => {
  isSubmitting.value = true
  try {
    await adminApi.createReceipt(form.value)
    toast.success('Nhập kho thành công!')
    modalInstance?.hide()
    fetchLowStock()
    fetchReceipts(0)
  } catch (error) {
    toast.error(error.response?.data?.message || 'Có lỗi xảy ra khi tạo phiếu nhập')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.spin {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  100% { transform: rotate(360deg); }
}

.low-stock-list {
  max-height: 400px;
  overflow-y: auto;
}
.low-stock-list::-webkit-scrollbar {
  width: 4px;
}
.low-stock-list::-webkit-scrollbar-track {
  background: transparent;
}
.low-stock-list::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 10px;
}
</style>
