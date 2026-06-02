<template>
  <div>
    <div class="d-flex justify-content-end mb-4">
      <button class="btn btn-bakery-outline btn-sm" @click="exportExcel" :disabled="exporting">📥 {{ exporting ? 'Đang xuất...' : 'Xuất Excel' }}</button>
    </div>

    <!-- Status Tabs -->
    <div class="d-flex gap-2 mb-4 flex-wrap">
      <button v-for="tab in statusTabs" :key="tab.value"
        :class="['btn btn-sm', activeTab === tab.value ? 'btn-bakery' : 'btn-bakery-outline']"
        @click="activeTab = tab.value; page = 0; fetchOrders()">
        {{ tab.label }}
      </button>
    </div>

    <!-- Order Table -->
    <div class="bakery-card">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead><tr><th>Mã</th><th>Khách hàng</th><th>Tổng tiền</th><th>Trạng thái</th><th>Ngày đặt</th><th>Hành động</th></tr></thead>
          <tbody>
            <tr v-for="o in orders" :key="o.orderId">
              <td class="fw-semibold">#{{ o.orderCode }}</td>
              <td>{{ o.recipientName || o.customerName }}</td>
              <td class="fw-bold text-bakery">{{ formatPrice(o.totalAmount) }}</td>
              <td><OrderStatusBadge :status="o.status" /></td>
              <td class="text-sub small">{{ formatDate(o.createdAt) }}</td>
              <td>
                <div class="d-flex gap-2">
                  <button v-if="o.status === 'PENDING'" class="btn btn-sm btn-bakery d-flex align-items-center gap-1" @click="updateStatus(o.orderId, 'CONFIRMED')"><PhCheckCircle weight="bold" /> Xác nhận</button>
                  <button v-if="o.status === 'CONFIRMED'" class="btn btn-sm btn-info text-white d-flex align-items-center gap-1" @click="updateStatus(o.orderId, 'SHIPPING')"><PhTruck weight="bold" /> Chuyển giao hàng</button>
                  <button v-if="o.status === 'SHIPPING'" class="btn btn-sm btn-success d-flex align-items-center gap-1" @click="updateStatus(o.orderId, 'DELIVERED')"><PhPackage weight="bold" /> Đã giao</button>
                  <button v-if="o.status !== 'DELIVERED' && o.status !== 'CANCELLED'" class="btn btn-sm btn-outline-danger d-flex align-items-center gap-1" @click="updateStatus(o.orderId, 'CANCELLED')"><PhXCircle weight="bold" /> Huỷ đơn</button>
                  <span v-if="o.status === 'DELIVERED' || o.status === 'CANCELLED'" class="text-sub small">—</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="mt-3"><Pagination v-model="page" :total-pages="totalPages" @update:model-value="fetchOrders" /></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin.api'
import { toast } from 'vue3-toastify'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import Pagination from '@/components/Pagination.vue'
import { PhCheckCircle, PhTruck, PhPackage, PhXCircle } from '@phosphor-icons/vue'
import dayjs from 'dayjs'

const orders = ref([])
const page = ref(0)
const totalPages = ref(0)
const activeTab = ref('')
const exporting = ref(false)

const statusTabs = [
  { value: '', label: '📋 Tất cả' },
  { value: 'PENDING', label: '⏳ Chờ xác nhận' },
  { value: 'CONFIRMED', label: '✅ Đã xác nhận' },
  { value: 'SHIPPING', label: '🚚 Đang giao' },
  { value: 'DELIVERED', label: '📬 Đã giao' },
  { value: 'CANCELLED', label: '❌ Đã hủy' },
]

function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }
function formatDate(d) { return dayjs(d).format('DD/MM/YYYY HH:mm') }

async function fetchOrders() {
  try {
    const params = { page: page.value, size: 15 }
    if (activeTab.value) params.status = activeTab.value
    const { data } = await adminApi.getOrders(params)
    orders.value = data.data?.content || []
    totalPages.value = data.data?.totalPages || 0
  } catch { orders.value = [] }
}

async function updateStatus(orderId, newStatus) {
  try {
    await adminApi.updateOrderStatus(orderId, newStatus)
    toast.success('Đã cập nhật trạng thái')
    fetchOrders()
  } catch (err) { toast.error(err.response?.data?.message || 'Thất bại') }
}

async function exportExcel() {
  exporting.value = true
  try {
    const { data } = await adminApi.exportOrders({ status: activeTab.value || undefined })
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
