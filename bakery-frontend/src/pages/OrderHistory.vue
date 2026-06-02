<template>
  <div class="container py-4">
    <h2 class="fw-bold mb-4">📦 Đơn hàng của tôi</h2>
    <div v-if="loading" v-for="i in 3" :key="i" class="skeleton mb-3" style="height: 120px"></div>
    <div v-else-if="orders.length === 0" class="text-center py-5">
      <span style="font-size: 4rem">📦</span>
      <p class="text-sub mt-3">Bạn chưa có đơn hàng nào</p>
      <router-link to="/products" class="btn btn-bakery">Mua sắm ngay</router-link>
    </div>
    <div v-else>
      <div v-for="order in orders" :key="order.orderId" class="bakery-card mb-3">
        <div class="d-flex justify-content-between align-items-start mb-2">
          <div>
            <span class="fw-bold">#{{ order.orderCode }}</span>
            <span class="text-sub small ms-2">{{ formatDate(order.createdAt) }}</span>
          </div>
          <OrderStatusBadge :status="order.status" />
        </div>
        <div class="d-flex justify-content-between align-items-center">
          <span class="fw-bold text-bakery">{{ formatPrice(order.totalAmount) }}</span>
          <button v-if="order.status === 'PENDING'" class="btn btn-sm btn-outline-danger" @click="cancelOrder(order.orderId)">Hủy đơn</button>
        </div>
      </div>
      <Pagination v-model="page" :total-pages="totalPages" @update:model-value="fetchOrders" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api/order.api'
import { toast } from 'vue3-toastify'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import Pagination from '@/components/Pagination.vue'
import dayjs from 'dayjs'

const orders = ref([])
const loading = ref(true)
const page = ref(0)
const totalPages = ref(0)

function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }
function formatDate(d) { return dayjs(d).format('DD/MM/YYYY HH:mm') }

async function fetchOrders() {
  loading.value = true
  try {
    const { data } = await orderApi.getHistory({ page: page.value, size: 10 })
    orders.value = data.data?.content || []
    totalPages.value = data.data?.totalPages || 0
  } catch { orders.value = [] }
  finally { loading.value = false }
}

async function cancelOrder(id) {
  if (!confirm('Bạn chắc chắn muốn hủy?')) return
  try {
    await orderApi.cancelOrder(id)
    toast.success('Đã hủy đơn hàng')
    fetchOrders()
  } catch (err) { toast.error(err.response?.data?.message || 'Hủy thất bại') }
}

onMounted(fetchOrders)
</script>
