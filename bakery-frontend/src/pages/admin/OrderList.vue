<template>
  <div>
    <!-- FILTER BAR -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div class="status-tabs">
        <button v-for="tab in statusTabs" :key="tab.value"
          :class="['status-tab', activeTab === tab.value ? 'active' : '']"
          @click="activeTab = tab.value; page = 0; fetchOrders()">
          <component :is="tab.icon" v-if="tab.icon" size="15" weight="bold" />
          {{ tab.label }}
          <span v-if="tab.count !== undefined && tab.count > 0" class="tab-count">{{ tab.count }}</span>
        </button>
      </div>

      <div class="d-flex gap-2">
        <input type="text" v-model="searchCode" @keyup.enter="fetchOrders" placeholder="Tìm mã đơn (vd: 16)" class="form-control form-control-sm border" style="width: 150px; background: var(--bg-surface); color: var(--text-main)" />
        <input type="date" v-model="startDate" @change="fetchOrders" class="form-control form-control-sm border" style="width: 130px; background: var(--bg-surface); color: var(--text-main)" />
        <input type="date" v-model="endDate" @change="fetchOrders" class="form-control form-control-sm border" style="width: 130px; background: var(--bg-surface); color: var(--text-main)" />
        <button class="btn btn-bakery-outline btn-sm d-flex align-items-center gap-1" @click="exportExcel" :disabled="exporting">
          <PhDownloadSimple size="16" weight="bold" /> {{ exporting ? 'Đang xuất...' : 'Xuất Excel' }}
        </button>
      </div>
    </div>

    <!-- Error State -->
    <div v-if="error" class="error-state">
      <div class="error-state-icon mx-auto"><PhWarningCircle size="32" color="var(--color-danger)" /></div>
      <div class="error-state-title">Lỗi tải đơn hàng</div>
      <p class="error-state-text">{{ error }}</p>
      <button class="btn btn-bakery btn-sm" @click="error=null; fetchOrders()">Thử lại</button>
    </div>

    <!-- Order Table -->
    <div v-else class="bakery-card bakery-card--static">
      <!-- Loading Skeleton -->
      <div v-if="loading" class="table-responsive">
        <table class="admin-table">
          <thead><tr><th>Mã đơn</th><th>Khách hàng</th><th>Tổng tiền</th><th>Trạng thái</th><th>Ngày đặt</th><th>Thao tác</th></tr></thead>
          <tbody>
            <tr v-for="i in 6" :key="i">
              <td><div class="skeleton skeleton-text" style="width:80px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:120px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:90px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:100px;height:24px;border-radius:999px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:100px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:120px;height:32px;border-radius:8px"></div></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Empty State -->
      <div v-else-if="orders.length === 0" class="empty-state py-5">
        <div class="empty-state-icon mx-auto"><PhPackage size="36" color="var(--text-muted)" /></div>
        <div class="empty-state-title">Không có đơn hàng nào</div>
        <p class="empty-state-text">{{ activeTab ? 'Không có đơn nào với trạng thái này' : 'Chưa có đơn hàng nào trong hệ thống' }}</p>
      </div>

      <!-- Data Table -->
      <div v-else class="table-responsive">
        <table class="admin-table">
          <thead>
            <tr>
              <th class="sortable" @click="toggleSort('orderCode')">
                Mã đơn <PhCaretUpDown size="12" class="sort-icon" />
              </th>
              <th>Khách hàng</th>
              <th class="sortable" @click="toggleSort('totalAmount')">
                Tổng tiền <PhCaretUpDown size="12" class="sort-icon" />
              </th>
              <th>Trạng thái</th>
              <th class="sortable" @click="toggleSort('createdAt')">
                Ngày đặt <PhCaretUpDown size="12" class="sort-icon" />
              </th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in sortedOrders" :key="o.orderId" class="order-row">
              <td class="fw-semibold cursor-pointer order-link" @click="openOrderDetail(o)">#{{ o.orderCode }}</td>
              <td>
                <div class="d-flex flex-column">
                  <span class="fw-semibold" style="color: var(--text-main)">{{ o.receiverName || 'Khách vãng lai' }}</span>
                  <span class="small" style="color: var(--text-sub)">{{ o.receiverPhone || '' }}</span>
                </div>
              </td>
              <td class="fw-bold" style="color: var(--text-main)">{{ formatPrice(o.totalAmount) }}</td>
              <td><OrderStatusBadge :status="o.status" /></td>
              <td class="small" style="color: var(--text-sub)">{{ formatDate(o.createdAt) }}</td>
              <td>
                <!-- Action Button: NEXT STEP only -->
                <div class="action-group">
                  <!-- PENDING → confirm -->
                  <button v-if="o.status === 'PENDING'" class="action-btn action-btn--confirm" @click="updateStatus(o.orderId, 'CONFIRMED')">
                    <PhCheckCircle weight="bold" size="15" /> Xác nhận
                  </button>
                  <!-- CONFIRMED → preparing -->
                  <button v-else-if="o.status === 'CONFIRMED'" class="action-btn action-btn--prepare" @click="updateStatus(o.orderId, 'PREPARING')">
                    <PhCookingPot weight="bold" size="15" /> Chuẩn bị
                  </button>
                  <!-- PREPARING → shipped -->
                  <button v-else-if="o.status === 'PREPARING'" class="action-btn action-btn--ship" @click="updateStatus(o.orderId, 'SHIPPED')">
                    <PhTruck weight="bold" size="15" /> Giao vận chuyển
                  </button>
                  <!-- SHIPPED → delivering -->
                  <button v-else-if="o.status === 'SHIPPED'" class="action-btn action-btn--deliver" @click="updateStatus(o.orderId, 'DELIVERING')">
                    <PhPath weight="bold" size="15" /> Đang giao
                  </button>
                  <!-- DELIVERING → completed -->
                  <button v-else-if="o.status === 'DELIVERING'" class="action-btn action-btn--complete" @click="updateStatus(o.orderId, 'COMPLETED')">
                    <PhSealCheck weight="bold" size="15" /> Hoàn thành
                  </button>
                  <!-- COMPLETED / CANCELLED → no action -->
                  <span v-else class="action-done">
                    <PhCheckCircle size="14" weight="fill" /> Hoàn tất
                  </span>

                  <!-- Cancel Button (available for PENDING to DELIVERING) -->
                  <button v-if="canCancel(o.status)" class="action-btn action-btn--cancel" @click="openCancelDialog(o)" title="Huỷ đơn">
                    <PhXCircle weight="bold" size="15" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="mt-3"><Pagination v-model="page" :total-pages="totalPages" @update:model-value="fetchOrders" /></div>
    </div>

    <!-- Cancel Dialog -->
    <Teleport to="body">
      <div v-if="showCancelDialog" class="modal-backdrop" @click.self="showCancelDialog = false">
        <div class="cancel-dialog">
          <div class="d-flex align-items-center gap-3 mb-4">
            <div class="kpi-icon-wrap" style="width:44px;height:44px;background:var(--color-danger-light);color:var(--color-danger)">
              <PhWarningCircle size="24" weight="bold" />
            </div>
            <div>
              <h6 class="fw-bold mb-1" style="color:var(--text-main)">Huỷ đơn hàng</h6>
              <p class="small mb-0" style="color:var(--text-sub)">Mã đơn: #{{ cancellingOrder?.orderCode }}</p>
            </div>
          </div>
          <label class="form-label fw-semibold small" style="color:var(--text-main)">Lý do huỷ <span style="color:var(--color-danger)">*</span></label>
          <textarea v-model="cancelReason" class="form-control mb-3" rows="3" placeholder="Nhập lý do huỷ đơn hàng..." style="background:var(--bg-muted);border:1px solid var(--border-color);color:var(--text-main)"></textarea>
          <div class="d-flex gap-2 justify-content-end">
            <button class="btn btn-sm" style="background:var(--bg-muted);color:var(--text-sub)" @click="showCancelDialog = false">Đóng</button>
            <button class="btn btn-sm text-white" style="background:var(--color-danger)" @click="confirmCancel" :disabled="!cancelReason.trim()">
              <PhXCircle size="15" weight="bold" /> Xác nhận huỷ
            </button>
          </div>
        </div>
      </div>
      
      <!-- Order Detail Modal -->
      <div v-if="showOrderModal" class="modal-backdrop" @click.self="showOrderModal = false">
        <div class="cancel-dialog" style="max-width: 600px;">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="fw-bold mb-0" style="color: var(--text-main)">Chi tiết đơn hàng #{{ selectedOrder?.orderCode }}</h5>
            <button class="btn btn-sm border-0 bg-transparent text-muted" @click="showOrderModal = false">
              <PhXCircle size="24" />
            </button>
          </div>
          <div style="max-height: 60vh; overflow-y: auto; padding-right: 10px;" class="chat-messages">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <OrderStatusBadge :status="selectedOrder?.status" />
              <span class="small" style="color: var(--text-sub)">{{ formatDate(selectedOrder?.createdAt) }}</span>
            </div>
            
            <div class="mb-3 p-3 rounded-3" style="background: var(--bg-muted)">
              <h6 class="fw-bold" style="color: var(--text-main)">Thông tin giao hàng</h6>
              <div class="small" style="color: var(--text-sub)">
                <p class="mb-1"><strong>Người nhận:</strong> {{ selectedOrder?.receiverName || 'Khách vãng lai' }}</p>
                <p class="mb-1"><strong>SĐT:</strong> {{ selectedOrder?.receiverPhone || 'Chưa cập nhật' }}</p>
                <p class="mb-0"><strong>Địa chỉ:</strong> {{ selectedOrder?.shippingAddress || 'Mua tại cửa hàng' }}</p>
              </div>
            </div>

            <h6 class="fw-bold mb-2" style="color: var(--text-main)">Sản phẩm</h6>
            <div class="d-flex flex-column gap-2 mb-3">
              <div v-for="item in selectedOrder?.orderDetails" :key="item.orderDetailId" class="d-flex align-items-center p-2 rounded-3 border" style="background: var(--bg-surface);">
                <img :src="item.primaryImageUrl || 'https://placehold.co/60x60?text=No+Image'" class="rounded me-3" style="width: 50px; height: 50px; object-fit: cover; border: 1px solid var(--border-color);" />
                <div class="flex-grow-1">
                  <div class="fw-semibold" style="color: var(--text-main); font-size: 0.95rem;">{{ item.productName }}</div>
                  <div class="small" style="color: var(--text-sub)">Size {{ item.productSize || 'Mặc định' }} x {{ item.quantity }}</div>
                </div>
                <div class="fw-bold text-nowrap" style="color: var(--primary)">{{ formatPrice(item.subTotal || item.price * item.quantity) }}</div>
              </div>
              <div v-if="!selectedOrder?.orderDetails || selectedOrder.orderDetails.length === 0" class="text-center text-muted small py-2">
                Không có chi tiết sản phẩm
              </div>
            </div>

            <div class="d-flex justify-content-between align-items-center pt-2 pb-2" v-if="selectedOrder?.discountAmount > 0">
              <span class="fw-semibold text-muted">Giảm giá (Voucher)</span>
              <span class="fw-semibold" style="color: var(--color-danger)">-{{ formatPrice(selectedOrder?.discountAmount) }}</span>
            </div>

            <div class="d-flex justify-content-between align-items-center pt-2 border-top">
              <span class="fw-semibold" style="color: var(--text-main)">Tổng thanh toán</span>
              <span class="fw-bold fs-5" style="color: var(--color-danger)">{{ formatPrice(selectedOrder?.totalAmount) }}</span>
            </div>
            <div class="d-flex justify-content-between align-items-center mt-2 small">
              <span style="color: var(--text-sub)">Phương thức</span>
              <span class="fw-semibold text-uppercase" style="color: var(--text-main)">{{ selectedOrder?.paymentMethod || 'COD' }} ({{ selectedOrder?.paymentStatus || 'PENDING' }})</span>
            </div>
          </div>
          <div class="mt-4 text-end">
             <button class="btn btn-bakery btn-sm" @click="showOrderModal = false">Đóng</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '@/api/admin.api'
import { toast } from 'vue3-toastify'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import Pagination from '@/components/Pagination.vue'
import {
  PhCheckCircle, PhTruck, PhPackage, PhXCircle, PhWarningCircle, PhDownloadSimple,
  PhCaretUpDown, PhClock, PhCookingPot, PhPath, PhSealCheck, PhListChecks
} from '@phosphor-icons/vue'
import dayjs from 'dayjs'

const orders = ref([])
const page = ref(0)
const totalPages = ref(0)
const activeTab = ref('')
const exporting = ref(false)
const loading = ref(true)
const error = ref(null)
const sortKey = ref('')
const sortDirection = ref('desc')
const searchCode = ref('')
const startDate = ref('')
const endDate = ref('')

const showOrderModal = ref(false)
const selectedOrder = ref(null)

// Cancel dialog
const showCancelDialog = ref(false)
const cancellingOrder = ref(null)
const cancelReason = ref('')

const statusTabs = [
  { value: '', label: 'Tất cả', icon: PhListChecks },
  { value: 'PENDING', label: 'Chờ xác nhận', icon: PhClock },
  { value: 'CONFIRMED', label: 'Đã xác nhận', icon: PhCheckCircle },
  { value: 'PREPARING', label: 'Đang chuẩn bị', icon: PhCookingPot },
  { value: 'SHIPPED', label: 'Đã giao VC', icon: PhTruck },
  { value: 'DELIVERING', label: 'Đang giao', icon: PhPath },
  { value: 'COMPLETED', label: 'Hoàn thành', icon: PhSealCheck },
  { value: 'CANCELLED', label: 'Đã huỷ', icon: PhXCircle },
]

function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }
function formatDate(d) { return dayjs(d).format('DD/MM/YYYY HH:mm') }

function canCancel(status) {
  return ['PENDING', 'CONFIRMED', 'PREPARING', 'SHIPPED', 'DELIVERING'].includes(status)
}

function toggleSort(key) {
  if (sortKey.value === key) { sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc' }
  else { sortKey.value = key; sortDirection.value = 'desc' }
}

const sortedOrders = computed(() => {
  if (!sortKey.value) return orders.value
  const items = [...orders.value]
  const dir = sortDirection.value === 'asc' ? 1 : -1
  return items.sort((a, b) => {
    if (sortKey.value === 'orderCode') return dir * String(a.orderCode).localeCompare(String(b.orderCode))
    if (sortKey.value === 'totalAmount') return dir * ((a.totalAmount || 0) - (b.totalAmount || 0))
    if (sortKey.value === 'createdAt') return dir * (new Date(a.createdAt) - new Date(b.createdAt))
    return 0
  })
})

async function fetchOrders() {
  loading.value = true
  error.value = null
  try {
    const params = { page: page.value, size: 15 }
    if (activeTab.value) params.status = activeTab.value
    if (searchCode.value) params.orderCode = searchCode.value
    if (startDate.value) params.fromDate = startDate.value + 'T00:00:00'
    if (endDate.value) params.toDate = endDate.value + 'T23:59:59'
    const { data } = await adminApi.getOrders(params)
    orders.value = data.data?.content || []
    totalPages.value = data.data?.totalPages || 0
  } catch (err) {
    error.value = err.response?.data?.message || 'Không thể tải danh sách đơn hàng'
    orders.value = []
  } finally { loading.value = false }
}

function openOrderDetail(order) {
  selectedOrder.value = order
  showOrderModal.value = true
}

async function updateStatus(orderId, newStatus) {
  try {
    await adminApi.updateOrderStatus(orderId, newStatus)
    toast.success('Đã chuyển trạng thái thành công')
    fetchOrders()
  } catch (err) { toast.error(err.response?.data?.message || 'Cập nhật thất bại') }
}

function openCancelDialog(order) {
  cancellingOrder.value = order
  cancelReason.value = ''
  showCancelDialog.value = true
}

async function confirmCancel() {
  if (!cancelReason.value.trim()) return
  try {
    await adminApi.updateOrderStatus(cancellingOrder.value.orderId, 'CANCELLED')
    toast.success('Đã huỷ đơn hàng')
    showCancelDialog.value = false
    fetchOrders()
  } catch (err) { toast.error(err.response?.data?.message || 'Huỷ đơn thất bại') }
}

async function exportExcel() {
  exporting.value = true
  try {
    const params = {}
    if (activeTab.value) params.status = activeTab.value
    if (searchCode.value) params.orderCode = searchCode.value
    if (startDate.value) params.fromDate = startDate.value + 'T00:00:00'
    if (endDate.value) params.toDate = endDate.value + 'T23:59:59'
    const { data } = await adminApi.exportOrders(params)
    const url = URL.createObjectURL(data)
    const a = document.createElement('a')
    a.href = url; a.download = `orders-${Date.now()}.xlsx`; a.click()
    URL.revokeObjectURL(url)
    toast.success('Đã xuất file Excel')
  } catch { toast.error('Xuất Excel thất bại') }
  finally { exporting.value = false }
}

onMounted(fetchOrders)
</script>

<style scoped>
/* Status Tabs */
.status-tabs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  padding: 4px;
  background: var(--bg-muted);
  border-radius: var(--radius-card);
}
.status-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border: none;
  border-radius: var(--radius-btn);
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  background: transparent;
  color: var(--text-sub);
  transition: all 0.25s ease;
  white-space: nowrap;
}
.status-tab:hover { color: var(--text-main); background: var(--bg-surface); }
.status-tab.active {
  background: var(--bg-surface);
  color: var(--primary);
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  font-size: 0.7rem;
  font-weight: 700;
  background: var(--color-danger);
  color: white;
}

/* Order Row */
.order-row { transition: background 0.2s ease; }
.order-row:hover { background: var(--bg-muted); }

.order-link {
  color: var(--primary);
  text-decoration: none;
  transition: opacity 0.2s ease;
}
.order-link:hover {
  opacity: 0.75;
}

/* Action Buttons */
.action-group {
  display: flex;
  align-items: center;
  gap: 6px;
}
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  border: none;
  border-radius: var(--radius-btn);
  font-size: 0.78rem;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.25s ease;
}
.action-btn:hover { transform: translateY(-1px); }
.action-btn:active { transform: scale(0.97); }

.action-btn--confirm  { background: #E8DAEF; color: #6C3483; }
.action-btn--confirm:hover { background: #D2B4DE; }

.action-btn--prepare  { background: #FDEBD0; color: #CA6F1E; }
.action-btn--prepare:hover { background: #FAD7A0; }

.action-btn--ship     { background: #D4E6F1; color: #2874A6; }
.action-btn--ship:hover { background: #AED6F1; }

.action-btn--deliver  { background: #D5F5E3; color: #1E8449; }
.action-btn--deliver:hover { background: #ABEBC6; }

.action-btn--complete { background: #D4EFDF; color: #196F3D; }
.action-btn--complete:hover { background: #A9DFBF; }

.action-btn--cancel {
  width: 32px;
  height: 32px;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background: var(--color-danger-light);
  color: var(--color-danger);
  border: 1px solid var(--color-danger);
}
.action-btn--cancel:hover { background: var(--color-danger); color: #fff; }

.action-done {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--text-muted);
}

/* Dark mode action overrides */
[data-theme="dark"] .action-btn--confirm  { background: rgba(232, 218, 239, 0.15); color: #BB8FCE; }
[data-theme="dark"] .action-btn--prepare  { background: rgba(253, 235, 208, 0.15); color: #F0B27A; }
[data-theme="dark"] .action-btn--ship     { background: rgba(212, 230, 241, 0.15); color: #5DADE2; }
[data-theme="dark"] .action-btn--deliver  { background: rgba(213, 245, 227, 0.15); color: #58D68D; }
[data-theme="dark"] .action-btn--complete { background: rgba(212, 239, 223, 0.15); color: #2ECC71; }

/* Cancel Dialog */
.modal-backdrop {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s ease;
}
.cancel-dialog {
  background: var(--bg-surface);
  border-radius: var(--radius-card);
  padding: 24px;
  width: 100%;
  max-width: 440px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  animation: slideUp 0.3s ease;
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

/* Responsive */
@media (max-width: 768px) {
  .status-tabs { gap: 4px; }
  .status-tab { padding: 5px 10px; font-size: 0.75rem; }
  .action-btn { padding: 4px 8px; font-size: 0.72rem; }
}
</style>
