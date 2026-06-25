<template>
  <div class="admin-inventory">

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
          <div class="d-flex align-items-center justify-content-between mb-4">
            <h6 class="fw-bold mb-0" style="color: var(--text-main)">Lịch sử nhập kho</h6>
            <div class="d-flex gap-2 align-items-center">
              <select class="form-select bakery-input form-select-sm" v-model="receiptFilter.dateRange" @change="handleDateRangeChange" style="width: auto;">
                <option value="">Tất cả thời gian</option>
                <option value="today">Hôm nay</option>
                <option value="yesterday">Hôm qua</option>
                <option value="custom">Tuỳ chọn...</option>
              </select>
              <div v-if="receiptFilter.dateRange === 'custom'" class="d-flex gap-2">
                <input type="date" class="form-control bakery-input form-control-sm" v-model="receiptFilter.startDate" @change="fetchReceipts(0)" />
                <span class="text-muted d-flex align-items-center">-</span>
                <input type="date" class="form-control bakery-input form-control-sm" v-model="receiptFilter.endDate" @change="fetchReceipts(0)" />
              </div>
            </div>
          </div>
          
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
                  <th>Thời gian</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="receipt in receipts" :key="receipt.receiptId">
                  <td>
                    <div class="fw-semibold product-history-link" @click="viewProductHistory(receipt.productId, receipt.productName)" title="Xem chi tiết lịch sử">
                      {{ receipt.productName }}
                    </div>
                    <div class="small mt-1 text-sub">
                      <span v-if="receipt.variantName" class="fw-bold" style="color: var(--bakery-primary)">Size {{ receipt.variantName }}</span>
                      <span v-if="receipt.variantName && displayNote(receipt.note)" class="mx-2">•</span>
                      <span v-if="displayNote(receipt.note)">{{ displayNote(receipt.note) }}</span>
                    </div>
                  </td>
                  <td class="fw-bold fs-6" :class="receipt.quantity > 0 ? 'text-success' : 'text-danger'">
                    {{ receipt.quantity > 0 ? '+' : '' }}{{ receipt.quantity }}
                  </td>
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
           <input type="text" class="form-control bakery-input form-control-sm" placeholder="Tìm tên, danh mục, size..." style="width: 200px;" v-model="searchQuery" />
        </div>
      </div>
      <div class="table-responsive">
        <table class="admin-table">
          <thead>
            <tr>
              <th style="width: 50px" class="text-center">STT</th>
              <th style="width: 60px">Ảnh</th>
              <th>Sản phẩm</th>
              <th>Danh mục</th>
              <th>Size / Tồn kho hiện tại</th>
              <th class="text-end">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(p, index) in filteredProductOptions" :key="p.productId">
              <td class="text-center text-muted fw-semibold">{{ index + 1 }}</td>
              <td>
                <img :src="p.images?.[0]?.imageUrl || 'https://placehold.co/100x100?text=No+Image'" class="rounded" style="width: 44px; height: 44px; object-fit: cover; border: 1px solid var(--border-color);" />
              </td>
              <td>
                <div class="fw-bold" style="color: var(--text-main)">{{ p.name }}</div>
                <div class="small text-muted">{{ p.variants?.length || 0 }} biến thể</div>
              </td>
              <td>
                <span class="badge px-2 py-1" style="background: rgba(222, 172, 113, 0.15); color: var(--bakery-primary); border: 1px solid rgba(222, 172, 113, 0.3); font-weight: 600;">{{ p.categoryName || '---' }}</span>
              </td>
              <td>
                <div class="d-flex flex-wrap gap-2 py-1">
                  <div v-for="v in p.variants" :key="v.variantId" class="stock-chip">
                    <span class="size-label">{{ v.size }} :</span>
                    
                    <template v-if="editingVariant !== v.variantId">
                      <span class="stock-val" :class="v.stock < 5 ? 'text-danger' : (v.stock < 10 ? 'text-warning' : 'text-success')">
                        {{ v.stock }}
                      </span>
                      <button class="btn btn-link p-0 ms-2 edit-btn" title="Chỉnh sửa nhanh" @click="startEditStock(v)">
                        <PhPencilSimple size="14" weight="bold" />
                      </button>
                    </template>
                    
                    <template v-else>
                      <input type="number" class="form-control form-control-sm p-1 text-center fw-bold ms-1" style="width: 50px; height: 26px;" v-model="editStockValue" @keyup.enter="saveStock(v)" />
                      <div class="d-flex gap-1 ms-2">
                        <button class="btn btn-sm p-0 text-success" title="Lưu" @click="saveStock(v)"><PhCheck size="16" weight="bold"/></button>
                        <button class="btn btn-sm p-0 text-secondary" title="Huỷ" @click="cancelEditStock()"><PhX size="16" weight="bold"/></button>
                      </div>
                    </template>
                  </div>
                  <div v-if="!p.variants || p.variants.length === 0" class="small text-muted">Chưa có biến thể</div>
                </div>
              </td>
              <td class="text-end">
                <div class="d-flex flex-column gap-2">
                  <button class="btn btn-sm btn-bakery-ghost" @click="quickImport(p)">
                    <PhPlus /> Nhập
                  </button>
                  <button class="btn btn-sm btn-outline-secondary" @click="viewProductHistory(p.productId, p.name)">
                    <PhClockCounterClockwise /> Lịch sử
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredProductOptions.length === 0">
              <td colspan="6" class="text-center py-4 text-muted">Không có dữ liệu sản phẩm</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- MODAL NHẬP / CHỈNH SỬA KHO NHANH -->
    <div class="modal fade" id="quickImportModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bakery-card p-0 overflow-hidden">
          <div class="modal-header border-0 bg-light pb-2">
            <h5 class="modal-title fw-bold" style="color: var(--text-main)">Nhập / Xuất Tồn Kho</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <form @submit.prevent="submitQuickImport">
            <div class="modal-body pt-3 pb-4 px-4">
              <div class="mb-4 text-center">
                <div class="fw-bold fs-5 text-main mb-1">{{ formQuick.productName }}</div>
              </div>
              <div class="mb-3">
                <label class="form-label text-sub fw-semibold small">Phân loại (Size)</label>
                <select class="form-select bakery-input" v-model="formQuick.variantId" @change="updateQuickFormStock" required>
                  <option value="" disabled>-- Chọn size --</option>
                  <option v-for="v in formQuick.variants" :key="v.variantId" :value="v.variantId">Size {{ v.size }} (Tồn hiện tại: {{ v.stock }})</option>
                </select>
              </div>
              
              <div v-if="formQuick.variantId" class="mb-3">
                <label class="form-label text-sub fw-semibold small">Loại thao tác</label>
                <div class="d-flex gap-4 mb-3">
                  <div class="form-check">
                    <input class="form-check-input" type="radio" name="adjustType" id="typeAdd" value="add" v-model="formQuick.actionType">
                    <label class="form-check-label fw-semibold text-success" for="typeAdd">Nhập thêm (+)</label>
                  </div>
                  <div class="form-check">
                    <input class="form-check-input" type="radio" name="adjustType" id="typeSub" value="sub" v-model="formQuick.actionType">
                    <label class="form-check-label fw-semibold text-danger" for="typeSub">Xuất bớt (-)</label>
                  </div>
                </div>

                <label class="form-label text-sub fw-semibold small">Số lượng</label>
                <div class="d-flex align-items-center">
                  <input type="number" class="form-control bakery-input text-center fw-bold fs-5" v-model.number="formQuick.quantity" min="1" required />
                </div>
                
                <div class="mt-4 text-center p-3 rounded" style="background: var(--bg-muted)">
                  <span class="small text-muted mb-1 d-block">Tồn kho sau khi cập nhật:</span>
                  <span class="fw-bold fs-3" :class="finalCalculatedStock < 0 ? 'text-danger' : 'text-success'">
                    {{ finalCalculatedStock }}
                  </span>
                </div>
              </div>
            </div>
            <div class="modal-footer border-0 px-4 pb-4">
              <div class="d-flex w-100 gap-2">
                <button type="button" class="btn btn-light px-4 fw-semibold flex-fill" data-bs-dismiss="modal">Hủy</button>
                <button type="submit" class="btn btn-bakery px-4 fw-semibold flex-fill" :disabled="isSubmittingQuick || !formQuick.variantId || finalCalculatedStock < 0 || formQuick.quantity <= 0">
                  <span v-if="isSubmittingQuick" class="spinner-border spinner-border-sm me-2" role="status"></span>
                  Xác nhận
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- MODAL LỊCH SỬ NHẬP CỦA SẢN PHẨM -->
    <div class="modal fade" id="productHistoryModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content bakery-card p-0 overflow-hidden">
          <div class="modal-header border-0 bg-light pb-2">
            <h5 class="modal-title fw-bold" style="color: var(--text-main)">Lịch sử biến động: {{ selectedHistoryProductName }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div v-if="isLoadingProductHistory" class="text-center py-4">
              <span class="spinner-border text-primary" role="status"></span>
            </div>
            <div v-else-if="productHistoryList.length === 0" class="text-center py-4 text-sub">
              Chưa có lịch sử nhập/điều chỉnh nào.
            </div>
            <div v-else class="table-responsive">
              <table class="admin-table">
                <thead>
                  <tr>
                    <th>Loại / Ghi chú</th>
                    <th>Số lượng</th>
                    <th>Thời gian</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="receipt in productHistoryList" :key="receipt.receiptId">
                    <td>
                      <div class="text-sub">
                        <span v-if="receipt.variantName" class="fw-bold" style="color: var(--bakery-primary)">Size {{ receipt.variantName }}</span>
                        <span v-if="receipt.variantName && displayNote(receipt.note)" class="mx-2">•</span>
                        <span v-if="displayNote(receipt.note)">{{ displayNote(receipt.note) }}</span>
                      </div>
                    </td>
                    <td class="fw-bold fs-6" :class="receipt.quantity > 0 ? 'text-success' : 'text-danger'">
                      {{ receipt.quantity > 0 ? '+' : '' }}{{ receipt.quantity }}
                    </td>
                    <td class="text-sub small">{{ formatDate(receipt.createdAt) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <!-- Pagination for modal -->
            <div class="d-flex justify-content-end mt-3" v-if="productHistoryTotalPages > 1">
              <button class="btn btn-sm btn-outline-secondary me-2" :disabled="productHistoryPage === 0" @click="fetchProductHistory(productHistoryPage - 1)">Trang trước</button>
              <button class="btn btn-sm btn-outline-secondary" :disabled="productHistoryPage >= productHistoryTotalPages - 1" @click="fetchProductHistory(productHistoryPage + 1)">Trang sau</button>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '@/api/admin.api'
import { productApi } from '@/api/product.api' // To fetch product details
import { toast } from 'vue3-toastify'
import { PhWarningCircle, PhPlus, PhArrowsClockwise, PhPencilSimple, PhCheck, PhX, PhClockCounterClockwise } from '@phosphor-icons/vue'

const lowStockItems = ref([])
const isLoadingLowStock = ref(false)

const receipts = ref([])
const isLoadingReceipts = ref(false)
const currentPage = ref(0)
const totalPages = ref(1)

const isSubmittingQuick = ref(false)

const formQuick = ref({
  productId: '',
  productName: '',
  variants: [],
  variantId: '',
  actionType: 'add',
  quantity: 1
})

const searchQuery = ref('')
const filteredProductOptions = computed(() => {
  if (!searchQuery.value) return productOptions.value
  const q = searchQuery.value.toLowerCase()
  return productOptions.value.filter(p => 
    p.name.toLowerCase().includes(q) || 
    p.categoryName?.toLowerCase().includes(q) ||
    p.variants?.some(v => v.size.toLowerCase().includes(q))
  )
})

const productOptions = ref([])

const editingVariant = ref(null)
const editStockValue = ref(0)

const startEditStock = (v) => {
  editingVariant.value = v.variantId
  editStockValue.value = v.stock
}

const cancelEditStock = () => {
  editingVariant.value = null
}

const saveStock = async (v) => {
  if (editStockValue.value < 0) {
    toast.error('Số lượng không được âm')
    return
  }
  try {
    await adminApi.updateVariantStock(v.variantId, editStockValue.value)
    v.stock = editStockValue.value
    toast.success('Cập nhật số lượng thành công!')
    editingVariant.value = null
    fetchLowStock()
    fetchReceipts(0)
  } catch (error) {
    toast.error('Có lỗi xảy ra khi cập nhật số lượng')
  }
}

let quickModalInstance = null

onMounted(async () => {
  // Load bootstrap modal dynamically
  const { Modal } = await import('bootstrap')
  quickModalInstance = new Modal(document.getElementById('quickImportModal'))
  historyModalInstance = new Modal(document.getElementById('productHistoryModal'))
  
  fetchLowStock()
  fetchReceipts()
  fetchProductOptions()
})

const formatPrice = (p) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0)
const formatDate = (d) => {
  const date = new Date(d)
  return `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getFullYear()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const displayNote = (note) => {
  if (!note) return ''
  if (note.includes('Điều chỉnh') || note.includes('Cập nhật trực tiếp')) return ''
  return note
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

const receiptFilter = ref({ dateRange: '', startDate: '', endDate: '' })

const handleDateRangeChange = () => {
  const today = new Date()
  if (receiptFilter.value.dateRange === 'today') {
    const d = today.toISOString().split('T')[0]
    receiptFilter.value.startDate = d
    receiptFilter.value.endDate = d
    fetchReceipts(0)
  } else if (receiptFilter.value.dateRange === 'yesterday') {
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)
    const d = yesterday.toISOString().split('T')[0]
    receiptFilter.value.startDate = d
    receiptFilter.value.endDate = d
    fetchReceipts(0)
  } else if (receiptFilter.value.dateRange === 'custom') {
    // Wait for user to input date
  } else {
    receiptFilter.value.startDate = ''
    receiptFilter.value.endDate = ''
    fetchReceipts(0)
  }
}

let historyModalInstance = null
const selectedHistoryProductId = ref(null)
const selectedHistoryProductName = ref('')
const productHistoryList = ref([])
const productHistoryPage = ref(0)
const productHistoryTotalPages = ref(1)
const isLoadingProductHistory = ref(false)

const viewProductHistory = async (productId, productName) => {
  selectedHistoryProductId.value = productId
  selectedHistoryProductName.value = productName
  productHistoryPage.value = 0
  await fetchProductHistory(0)
  historyModalInstance?.show()
}

const fetchProductHistory = async (page = 0) => {
  if (!selectedHistoryProductId.value) return
  isLoadingProductHistory.value = true
  try {
    const res = await adminApi.getReceipts({ page, size: 5, productId: selectedHistoryProductId.value })
    productHistoryList.value = res.data?.data?.content || []
    productHistoryPage.value = res.data?.data?.number || 0
    productHistoryTotalPages.value = res.data?.data?.totalPages || 1
  } catch (error) {
    console.error(error)
  } finally {
    isLoadingProductHistory.value = false
  }
}

const fetchReceipts = async (page = 0) => {
  isLoadingReceipts.value = true
  try {
    const params = { page, size: 5 }
    if (receiptFilter.value.startDate && receiptFilter.value.dateRange !== '') params.startDate = receiptFilter.value.startDate
    if (receiptFilter.value.endDate && receiptFilter.value.dateRange !== '') params.endDate = receiptFilter.value.endDate
    
    const res = await adminApi.getReceipts(params)
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
    const res = await adminApi.getInventoryProducts({ size: 100 })
    productOptions.value = res.data?.data?.content || []
  } catch (error) {
    console.error(error)
  }
}

const finalCalculatedStock = computed(() => {
  if (!formQuick.value.variantId) return 0
  const variant = formQuick.value.variants.find(v => v.variantId === formQuick.value.variantId)
  const currentStock = variant ? variant.stock : 0
  
  let q = parseInt(formQuick.value.quantity) || 0
  if (q < 0) q = 0
  
  if (formQuick.value.actionType === 'add') {
    return currentStock + q
  } else {
    return currentStock - q
  }
})

const updateQuickFormStock = () => {
  formQuick.value.quantity = 1
  formQuick.value.actionType = 'add'
}

const quickImport = async (item) => {
  formQuick.value.productId = item.productId
  formQuick.value.productName = item.productName || item.name || 'Sản phẩm'
  formQuick.value.variants = item.variants || []
  
  if (formQuick.value.variants.length === 0) {
    try {
      const res = await productApi.getProductDetail(item.productId)
      formQuick.value.variants = res.data?.data?.variants || []
    } catch (e) { console.error(e) }
  }
  
  formQuick.value.variantId = item.variantId || (formQuick.value.variants[0]?.variantId || '')
  formQuick.value.actionType = 'add'
  formQuick.value.quantity = 1
  
  quickModalInstance?.show()
}

const submitQuickImport = async () => {
  const finalStock = finalCalculatedStock.value
  if (finalStock < 0) return
  if (formQuick.value.quantity <= 0) return
  
  isSubmittingQuick.value = true
  try {
    await adminApi.updateVariantStock(formQuick.value.variantId, finalStock)
    toast.success('Cập nhật tồn kho thành công!')
    quickModalInstance?.hide()
    
    fetchLowStock()
    fetchReceipts(0)
    fetchProductOptions()
  } catch (error) {
    toast.error('Có lỗi xảy ra khi cập nhật tồn kho')
  } finally {
    isSubmittingQuick.value = false
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

/* Stock Chip Design */
.stock-chip {
  background: var(--bg-surface);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 4px 10px;
  display: inline-flex;
  align-items: center;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}
.stock-chip:hover {
  border-color: var(--bakery-primary);
  background: rgba(222, 172, 113, 0.05);
}
.stock-chip .size-label {
  font-size: 0.8rem;
  color: var(--text-sub);
  margin-right: 6px;
  font-weight: 600;
}
.stock-chip .stock-val {
  font-size: 0.95rem;
  font-weight: 700;
}
.stock-chip .edit-btn {
  color: var(--text-muted);
  opacity: 0;
  transition: opacity 0.2s ease, color 0.2s ease;
}
.stock-chip:hover .edit-btn {
  opacity: 1;
}
.stock-chip .edit-btn:hover {
  color: var(--bakery-primary);
}

.product-history-link {
  color: var(--text-main);
  cursor: pointer;
  transition: color 0.2s ease;
}
.product-history-link:hover {
  color: var(--bakery-primary);
}
</style>
