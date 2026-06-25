<template>
  <div>
    <!-- ERROR STATE -->
    <div v-if="error" class="error-state">
      <div class="error-state-icon mx-auto">
        <PhWarningCircle size="32" color="var(--color-danger)" />
      </div>
      <div class="error-state-title">Không thể tải dữ liệu</div>
      <p class="error-state-text">{{ error }}</p>
      <button class="btn btn-bakery btn-sm" @click="retryAll">Thử lại</button>
    </div>

    <template v-else>
      <!-- Removed title and button -->
      <!-- KPI GRID -->
      <div class="kpi-grid mb-4">
        <!-- Skeleton -->
        <template v-if="loading">
          <div v-for="i in 4" :key="'ks'+i" class="kpi-card">
            <div class="d-flex justify-content-between mb-3">
              <div class="skeleton skeleton-text w-50"></div>
              <div class="skeleton skeleton-avatar" style="width:48px;height:48px"></div>
            </div>
            <div class="skeleton skeleton-heading" style="width:60%"></div>
            <div class="skeleton skeleton-text w-75 mt-2" style="height:12px"></div>
          </div>
        </template>
        <!-- Real Data -->
        <template v-else>
          <div class="kpi-card" v-for="(kpi, i) in kpis" :key="i" :style="{ '--kpi-accent': kpi.color }">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <span class="text-sub fw-semibold" style="font-size: 0.95rem">{{ kpi.label }}</span>
              <div class="kpi-icon-wrap" :style="{ background: kpi.bg, color: kpi.color }">
                <component :is="kpi.icon" size="24" weight="bold" />
              </div>
            </div>
            <h3 class="fw-bold mb-1" style="color: var(--text-main)">{{ kpi.value }}</h3>
            <div class="kpi-trend small fw-semibold mt-2 d-inline-flex align-items-center gap-1" :style="{ color: kpi.trendColor || kpi.color, background: kpi.trendBg || 'transparent' }">
              <component :is="kpi.trendIcon" v-if="kpi.trendIcon" size="14" weight="bold" />
              {{ kpi.trend }}
            </div>
          </div>
        </template>
      </div>

      <!-- WIDGET GRID (CHART & INSIGHT) -->
      <div class="widget-grid mb-4">
        <!-- Revenue Chart -->
        <div class="bakery-card bakery-card--static" style="height: 400px; display: flex; flex-direction: column;">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <h5 class="fw-bold mb-0" style="color: var(--text-main)">Doanh thu</h5>
            <div class="d-flex gap-3 align-items-center">
              <button class="btn-premium-purple" style="padding: 0.3rem 0.8rem; font-size: 0.85rem;" @click="triggerReport">
                <PhFilePdf size="16" weight="fill" class="action-icon" />
                <span class="text_button">Tạo báo cáo</span>
              </button>
              <div class="d-flex gap-1 p-1 rounded-3" style="background: var(--bg-muted)">
                <button v-for="p in periods" :key="p.value"
                  :class="['btn btn-sm border-0 fw-semibold px-3 rounded-3', period === p.value ? 'period-active' : 'period-inactive']"
                  @click="period = p.value; fetchChart()">
                  {{ p.label }}
                </button>
              </div>
            </div>
          </div>
          <!-- Chart Skeleton -->
          <div v-if="chartLoading" class="flex-grow-1 skeleton skeleton-chart"></div>
          <div v-else style="flex: 1; min-height: 0;">
            <canvas ref="chartRef"></canvas>
          </div>
        </div>

        <!-- Yuni Admin Chat -->
        <div class="bakery-card bakery-card--static" style="height: 400px; display: flex; flex-direction: column;">
          <div class="d-flex align-items-center justify-content-between mb-3">
            <div class="d-flex align-items-center gap-3">
              <div class="yuni-avatar-wrap">
                <img src="/images/logoAI.png" alt="Yuni" class="yuni-avatar" />
                <span class="yuni-status-dot"></span>
              </div>
              <div>
                <h6 class="fw-bold mb-0" style="color: var(--text-main); font-size: 0.95rem;">Yuni Admin</h6>
                <span class="small" style="color: var(--color-success); font-weight: 600;">● Đang hoạt động</span>
              </div>
            </div>
            <div class="d-flex gap-2">
              <button class="btn btn-sm border-0 p-2 rounded-3" style="background: var(--bg-muted); color: var(--text-sub)" @click="refreshAnalysis" title="Phân tích lại" :disabled="adminChatLoading">
                <PhArrowsClockwise size="16" :class="{ 'spin': adminChatLoading && adminChatMessages.length <= 1 }" />
              </button>
            </div>
          </div>

          <div class="chat-messages" ref="adminChatRef" style="flex: 1; overflow-y: auto; padding-right: 8px; margin-bottom: 10px; display: flex; flex-direction: column; gap: 10px;">
            <!-- Auto Analysis Loading -->
            <div v-if="adminChatLoading && adminChatMessages.length === 0" class="d-flex gap-2 align-items-start">
              <img src="/images/logoAI.png" alt="Yuni" class="chat-avatar" />
              <div class="msg-bubble msg-bot p-2 px-3 rounded-3">
                <div class="d-flex align-items-center gap-2">
                  <span class="typing-dots"><span></span><span></span><span></span></span>
                  <span class="small" style="color: var(--text-sub)">Yuni đang phân tích dữ liệu kinh doanh...</span>
                </div>
              </div>
            </div>
            <!-- Chat messages -->
            <div v-for="(msg, idx) in adminChatMessages" :key="idx" class="d-flex gap-2" :class="msg.role === 'user' ? 'justify-content-end' : 'align-items-start'">
              <img v-if="msg.role === 'bot'" src="/images/logoAI.png" alt="Yuni" class="chat-avatar" />
              <div class="msg-bubble p-2 px-3 rounded-3" :class="msg.role === 'user' ? 'msg-user' : 'msg-bot'" v-html="msg.content"></div>
            </div>
            <!-- Quick prompts after initial analysis -->
            <div v-if="adminChatMessages.length === 1 && !adminChatLoading" class="d-flex gap-2 align-items-start">
              <div style="width: 28px; flex-shrink: 0;"></div>
              <div class="quick-prompts">
                <button v-for="q in quickPrompts" :key="q" class="quick-btn" @click="sendQuickMsg(q)">{{ q }}</button>
              </div>
            </div>
            <!-- Typing indicator -->
            <div v-if="adminChatLoading && adminChatMessages.length > 0" class="d-flex gap-2 align-items-start">
              <img src="/images/logoAI.png" alt="Yuni" class="chat-avatar" />
              <div class="msg-bubble msg-bot p-2 px-3 rounded-3">
                <span class="typing-dots"><span></span><span></span><span></span></span>
              </div>
            </div>
          </div>
          
          <div class="d-flex gap-2">
            <input v-model="adminChatInput" @keyup.enter="sendAdminMsg" class="form-control form-control-sm border-0" style="background: var(--bg-muted); box-shadow: none; color: var(--text-main);" placeholder="Hỏi Yuni thêm về kinh doanh, chiến lược..." :disabled="adminChatLoading" />
            <button class="btn btn-sm text-white px-3" style="background: var(--primary); border-radius: var(--radius-btn);" @click="sendAdminMsg" :disabled="adminChatLoading || !adminChatInput.trim()">Gửi</button>
          </div>
        </div>
      </div>

      <!-- BOTTOM SECTION: TOP PRODUCTS & RECENT ORDERS -->
      <div class="row g-4">
        <div class="col-lg-6">
          <div class="bakery-card bakery-card--static h-100">
            <h5 class="fw-bold mb-4" style="color: var(--text-main)">Top sản phẩm</h5>
            <!-- Skeleton -->
            <div v-if="loading" class="d-flex flex-column gap-2">
              <div v-for="i in 5" :key="i" class="skeleton-row">
                <div class="skeleton-cell skeleton" style="flex:2"></div>
                <div class="skeleton-cell skeleton" style="flex:1"></div>
                <div class="skeleton-cell skeleton" style="flex:1"></div>
              </div>
            </div>
            <!-- Empty -->
            <div v-else-if="topProducts.length === 0" class="empty-state py-4">
              <div class="empty-state-icon mx-auto"><PhPackage size="32" color="var(--text-muted)" /></div>
              <p class="empty-state-text mb-0">Chưa có dữ liệu sản phẩm bán chạy</p>
            </div>
            <!-- Data -->
            <div v-else class="table-responsive">
              <table class="admin-table">
                <thead>
                  <tr>
                    <th class="sortable" :class="{ sorted: sortField === 'name' }" @click="sortBy('name')">
                      Sản phẩm <PhCaretUpDown size="12" class="sort-icon" />
                    </th>
                    <th class="sortable" :class="{ sorted: sortField === 'sold' }" @click="sortBy('sold')">
                      Đã bán <PhCaretUpDown size="12" class="sort-icon" />
                    </th>
                    <th class="sortable text-end" :class="{ sorted: sortField === 'revenue' }" @click="sortBy('revenue')">
                      Doanh thu <PhCaretUpDown size="12" class="sort-icon" />
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="p in sortedTopProducts" :key="p.productId">
                    <td class="fw-semibold">{{ p.name }}</td>
                    <td>{{ p.totalSold }}</td>
                    <td class="text-end fw-bold" style="color: var(--primary)">{{ formatPrice(p.minPrice * p.totalSold) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div class="col-lg-6">
          <div class="bakery-card bakery-card--static h-100">
            <div class="d-flex justify-content-between align-items-center mb-4">
              <h5 class="fw-bold mb-0" style="color: var(--text-main)">Đơn hàng gần đây</h5>
              <router-link to="/admin/orders" class="btn btn-bakery-ghost btn-sm fw-semibold d-flex align-items-center gap-1">
                Xem tất cả <PhArrowRight size="14" weight="bold" />
              </router-link>
            </div>
            <!-- Skeleton -->
            <div v-if="loading" class="d-flex flex-column gap-3">
              <div v-for="i in 4" :key="i" class="d-flex gap-3 p-3 rounded-4" style="background: var(--bg-muted)">
                <div class="skeleton skeleton-avatar" style="width:40px;height:40px"></div>
                <div class="flex-grow-1">
                  <div class="skeleton skeleton-text w-50"></div>
                  <div class="skeleton skeleton-text w-75" style="height:12px"></div>
                </div>
              </div>
            </div>
            <!-- Empty -->
            <div v-else-if="recentOrders.length === 0" class="empty-state py-4">
              <div class="empty-state-icon mx-auto"><PhClipboardText size="32" color="var(--text-muted)" /></div>
              <p class="empty-state-text mb-0">Chưa có đơn hàng nào</p>
            </div>
            <!-- Data -->
            <div v-else class="d-flex flex-column gap-3">
              <div v-for="(order, idx) in recentOrders" :key="order.orderId" class="d-flex justify-content-between align-items-center p-3 rounded-4 order-item stagger-item" :style="{ animationDelay: idx * 60 + 'ms', background: 'var(--bg-muted)' }">
                <div class="d-flex align-items-center gap-3">
                  <div class="kpi-icon-wrap" style="width: 40px; height: 40px; background: var(--bg-surface); box-shadow: var(--shadow-soft);">
                    <PhPackage size="20" color="var(--primary)" />
                  </div>
                  <div>
                    <div class="fw-bold" style="color: var(--text-main)">#{{ order.orderCode }}</div>
                    <div class="small" style="color: var(--text-sub)">{{ order.customerName || order.recipientName }}</div>
                  </div>
                </div>
                <div class="d-flex flex-column align-items-end gap-1">
                  <span class="fw-bold" style="color: var(--primary)">{{ formatPrice(order.totalAmount) }}</span>
                  <OrderStatusBadge :status="order.status" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { adminApi } from '@/api/admin.api'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import { Chart, registerables } from 'chart.js'
import { toast } from 'vue3-toastify'
import {
  PhMoney, PhClipboardText, PhUsers, PhPackage, PhRobot, PhArrowsClockwise,
  PhTrendUp, PhTrendDown, PhWarningCircle, PhInfo, PhCaretUpDown, PhArrowRight, PhFilePdf
} from '@phosphor-icons/vue'

function triggerReport() {
  toast.success('Đang tạo báo cáo chi tiết... 📊')
  setTimeout(() => {
    toast.success('Đã xuất báo cáo thành công! ✅', { autoClose: 3000 })
    window.open('/api/v1/admin/analytics/export?period=7days', '_blank')
  }, 1000)
}

Chart.register(...registerables)

const summary = ref({})
const topProducts = ref([])
const recentOrders = ref([])
const adminChatMessages = ref([])
const adminChatInput = ref('')
const adminChatLoading = ref(false)
const adminChatRef = ref(null)
const chartRef = ref(null)
const period = ref('7days')
const loading = ref(true)
const chartLoading = ref(true)
const error = ref(null)
const sortField = ref('sold')
const sortDir = ref('desc')
let chartInstance = null
let refreshTimer = null

const periods = [
  { value: '7days', label: '7 ngày' },
  { value: '30days', label: '30 ngày' },
  { value: '12months', label: '12 tháng' },
]

function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }

const kpis = computed(() => [
  { label: 'Tổng doanh thu', value: formatPrice(summary.value.totalRevenue), icon: PhMoney, trend: summary.value.totalRevenue > 0 ? 'Đang hoạt động' : 'Chưa có dữ liệu', trendIcon: PhTrendUp, trendColor: 'var(--color-success)', trendBg: 'var(--color-success-light)', color: '#4A9B5C', bg: 'var(--accent-sage)' },
  { label: 'Đơn chờ xử lý', value: summary.value.pendingOrders || 0, icon: PhClipboardText, trend: (summary.value.pendingOrders || 0) > 0 ? 'Cần chú ý' : 'Tất cả đã xử lý', trendIcon: summary.value.pendingOrders > 0 ? PhWarningCircle : PhTrendUp, trendColor: (summary.value.pendingOrders || 0) > 0 ? 'var(--color-warning)' : 'var(--color-success)', trendBg: (summary.value.pendingOrders || 0) > 0 ? 'var(--color-warning-light)' : 'var(--color-success-light)', color: '#B9851F', bg: 'var(--accent-honey)' },
  { label: 'Tổng khách hàng', value: summary.value.totalCustomers || 0, icon: PhUsers, trend: 'Khách hàng', trendIcon: PhTrendUp, trendColor: 'var(--color-info)', trendBg: 'var(--color-info-light)', color: '#7B6CC8', bg: 'var(--accent-plum)' },
  { label: 'Tổng sản phẩm', value: summary.value.totalProducts || 0, icon: PhPackage, trend: 'Trong kho', trendIcon: PhInfo, trendColor: 'var(--primary)', trendBg: 'var(--primary-light)', color: 'var(--primary)', bg: 'var(--primary-light)' },
])

function sortBy(field) {
  if (sortField.value === field) { sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc' }
  else { sortField.value = field; sortDir.value = 'desc' }
}

const sortedTopProducts = computed(() => {
  const items = [...topProducts.value]
  const dir = sortDir.value === 'asc' ? 1 : -1
  return items.sort((a, b) => {
    if (sortField.value === 'name') return dir * a.name.localeCompare(b.name)
    if (sortField.value === 'sold') return dir * ((a.totalSold || 0) - (b.totalSold || 0))
    if (sortField.value === 'revenue') return dir * ((a.minPrice * a.totalSold) - (b.minPrice * b.totalSold))
    return 0
  })
})

async function fetchAll() {
  try {
    const [s, t, o] = await Promise.all([
      adminApi.getSummary(), adminApi.getTopProducts({ limit: 5 }), adminApi.getOrders({ page: 0, size: 5 }),
    ])
    summary.value = s.data?.data || {}
    topProducts.value = t.data?.data || []
    recentOrders.value = o.data?.data?.content || []
    error.value = null
  } catch (err) {
    error.value = 'Không thể kết nối đến máy chủ. Vui lòng thử lại.'
  } finally { loading.value = false }
}

async function fetchChart() {
  chartLoading.value = true
  try {
    const { data } = await adminApi.getRevenueChart(period.value)
    const chartData = data.data || []
    chartLoading.value = false
    await nextTick()
    if (chartInstance) chartInstance.destroy()
    if (!chartRef.value) return

    const isDark = document.documentElement.getAttribute('data-theme') === 'dark'
    const ctx = chartRef.value.getContext('2d');
    const gradient = ctx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, isDark ? 'rgba(224, 122, 84, 0.2)' : 'rgba(200, 90, 50, 0.15)');
    gradient.addColorStop(1, isDark ? 'rgba(26, 20, 18, 0)' : 'rgba(250, 242, 233, 0)');

    const lineColor = isDark ? '#E07A54' : '#C85A32'
    const gridColor = isDark ? 'rgba(58, 50, 44, 0.5)' : '#F4E2D8'
    const textColor = isDark ? '#A89888' : '#756B64'

    chartInstance = new Chart(chartRef.value, {
      type: 'line',
      data: {
        labels: chartData.map(d => d.date || d.month),
        datasets: [{
          label: 'Doanh thu',
          data: chartData.map(d => d.revenue),
          borderColor: lineColor,
          backgroundColor: gradient,
          fill: true,
          tension: 0.4,
          pointRadius: 4,
          pointHoverRadius: 7,
          pointBackgroundColor: isDark ? '#241E1A' : '#fff',
          pointBorderColor: lineColor,
          pointBorderWidth: 2,
          borderWidth: 2.5
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: isDark ? '#2E2622' : '#fff',
            titleColor: isDark ? '#F0E8E0' : '#2C1E16',
            bodyColor: isDark ? '#A89888' : '#756B64',
            borderColor: isDark ? '#3A322C' : '#EAE0DA',
            borderWidth: 1,
            padding: 12,
            boxPadding: 6,
            usePointStyle: true,
            callbacks: {
              label: function(context) { return ' ' + formatPrice(context.raw); }
            }
          }
        },
        scales: {
          x: { grid: { display: false }, ticks: { color: textColor } },
          y: {
            beginAtZero: true,
            border: { display: false },
            grid: { color: gridColor, drawTicks: false },
            ticks: { color: textColor }
          },
        },
      },
    })
  } catch { /* silent */ }
  finally { chartLoading.value = false }
}

import api from '@/api/axiosInstance'

const quickPrompts = [
  '💡 Gợi ý chiến lược tăng doanh thu',
  '📦 Phân tích đơn hàng bị huỷ',
  '🏆 Sản phẩm nào nên đẩy mạnh?',
  '⚠️ Rủi ro cần xử lý gấp',
]

function sendQuickMsg(msg) {
  adminChatInput.value = msg
  sendAdminMsg()
}

async function fetchAutoAnalysis() {
  adminChatLoading.value = true
  try {
    const { data } = await api.post('/ai/chat', { message: 'Hãy tự động phân tích toàn bộ tình hình kinh doanh hiện tại của cửa hàng Brevery. Đưa ra tóm tắt doanh thu, đơn hàng, tồn kho, đánh giá khách hàng. Sau đó đưa ra 3 lời khuyên cụ thể và hành động cần làm ngay hôm nay.', admin: true })
    const reply = data.data?.reply || data.data || 'Chào sếp! Em chưa lấy được dữ liệu. Sếp thử bấm nút làm mới nhé!'
    const htmlReply = formatAiReply(reply)
    adminChatMessages.value.push({ role: 'bot', content: htmlReply })
  } catch (err) {
    adminChatMessages.value.push({ role: 'bot', content: 'Chào sếp! 📊 Hiện tại em chưa kết nối được bộ não AI. Sếp có thể hỏi em trực tiếp bên dưới nhé!' })
  } finally {
    adminChatLoading.value = false
    scrollToBottom()
  }
}

function formatAiReply(reply) {
  return reply
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/^### (.+)$/gm, '<h6 class="fw-bold mt-2 mb-1" style="color: var(--primary)">$1</h6>')
    .replace(/^## (.+)$/gm, '<h5 class="fw-bold mt-3 mb-1" style="color: var(--primary)">$1</h5>')
    .replace(/^- (.+)$/gm, '<span class="d-block ps-2" style="border-left: 2px solid var(--primary); margin-bottom: 4px;">$1</span>')
    .replace(/\n/g, '<br>')
}

async function sendAdminMsg() {
  if (!adminChatInput.value?.trim() || adminChatLoading.value) return
  const msg = adminChatInput.value.trim()
  adminChatInput.value = ''
  adminChatMessages.value.push({ role: 'user', content: msg })
  scrollToBottom()

  adminChatLoading.value = true
  try {
    const { data } = await api.post('/ai/chat', { message: msg, admin: true })
    const reply = data.data?.reply || data.data || 'Xin lỗi sếp, em gặp sự cố.'
    const htmlReply = formatAiReply(reply)
    adminChatMessages.value.push({ role: 'bot', content: htmlReply })
  } catch (err) {
    adminChatMessages.value.push({ role: 'bot', content: 'Rất tiếc sếp, có lỗi kết nối. Vui lòng thử lại.' })
  } finally {
    adminChatLoading.value = false
    scrollToBottom()
  }
}

function refreshAnalysis() {
  adminChatMessages.value = []
  fetchAutoAnalysis()
}

function scrollToBottom() {
  nextTick(() => { if (adminChatRef.value) adminChatRef.value.scrollTop = adminChatRef.value.scrollHeight })
}

function retryAll() {
  loading.value = true; error.value = null
  fetchAll(); fetchChart();
}

onMounted(() => {
  fetchAll(); fetchChart();
  fetchAutoAnalysis()
  refreshTimer = setInterval(fetchAll, 30000)
})
onUnmounted(() => { clearInterval(refreshTimer); if (chartInstance) chartInstance.destroy() })
</script>

<style scoped>
.kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.kpi-card {
  position: relative;
  background: var(--bg-surface);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-soft);
  border: 1px solid var(--border-light);
  padding: 24px;
  overflow: hidden;
  transition: transform var(--duration-base) var(--ease-out), box-shadow var(--duration-base) var(--ease-out);
}
.kpi-card::before { content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 4px; background: var(--kpi-accent, var(--primary)); opacity: 0.9; }
.kpi-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-hover); }
.kpi-icon-wrap { width: 48px; height: 48px; border-radius: 50%; background: var(--primary-light); display: inline-flex; align-items: center; justify-content: center; flex-shrink: 0; }
.kpi-trend {
  padding: 3px 10px;
  border-radius: var(--radius-badge);
  font-size: 0.78rem;
}

.widget-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }

/* Period toggle */
.period-active {
  background: var(--bg-surface) !important;
  color: var(--primary) !important;
  box-shadow: var(--shadow-sm);
}
.period-inactive {
  background: transparent !important;
  color: var(--text-sub) !important;
}
.period-inactive:hover {
  color: var(--text-main) !important;
}

.sortable { cursor: pointer; user-select: none; transition: color 0.2s; }
.sortable:hover { color: var(--primary); }
.sortable.sorted { color: var(--primary); }
.sort-icon { margin-left: 4px; opacity: 0.5; transition: 0.2s; }

.chat-messages::-webkit-scrollbar { width: 4px; }
.chat-messages::-webkit-scrollbar-track { background: transparent; }
.chat-messages::-webkit-scrollbar-thumb { background: var(--border-color); border-radius: 10px; }

/* Yuni Avatar */
.yuni-avatar-wrap { position: relative; width: 40px; height: 40px; flex-shrink: 0; }
.yuni-avatar { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; border: 2px solid var(--primary); }
.yuni-status-dot { position: absolute; bottom: 1px; right: 1px; width: 10px; height: 10px; border-radius: 50%; background: var(--color-success); border: 2px solid var(--bg-surface); }
.chat-avatar { width: 28px; height: 28px; border-radius: 50%; object-fit: cover; flex-shrink: 0; }

/* Message Bubbles */
.msg-bubble {
  max-width: 85%;
  word-break: break-word;
  font-size: 0.88rem;
  line-height: 1.5;
}
.msg-bot {
  background: var(--bg-muted);
  color: var(--text-main);
  border-top-left-radius: 4px !important;
}
.msg-user {
  background: var(--primary);
  color: white;
  border-top-right-radius: 4px !important;
}

/* Quick Prompts */
.quick-prompts { display: flex; flex-wrap: wrap; gap: 6px; }
.quick-btn {
  display: inline-flex;
  align-items: center;
  padding: 5px 12px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  background: var(--bg-surface);
  color: var(--text-sub);
  transition: all 0.2s ease;
  white-space: nowrap;
}
.quick-btn:hover { border-color: var(--primary); color: var(--primary); background: var(--primary-light); }

/* Typing animation */
.typing-dots { display: inline-flex; align-items: center; gap: 4px; padding: 4px 0; }
.typing-dots span {
  width: 6px; height: 6px; border-radius: 50%; background: var(--text-sub);
  animation: typingBounce 1.4s infinite ease-in-out;
}
.typing-dots span:nth-child(2) { animation-delay: 0.16s; }
.typing-dots span:nth-child(3) { animation-delay: 0.32s; }
@keyframes typingBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.order-item { transition: background var(--duration-fast) var(--ease-out); }
.order-item:hover { background: var(--primary-light) !important; }

.spin { animation: spin 1s linear infinite; }
@keyframes spin { 100% { transform: rotate(360deg); } }
@keyframes staggerIn { to { opacity: 1; transform: translateY(0); } }

@media (max-width: 991px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .widget-grid { grid-template-columns: 1fr; }
}
@media (max-width: 576px) {
  .kpi-grid { grid-template-columns: 1fr; }
}

/* PREMIUM PURPLE ACTION BUTTON */
.btn-premium-purple {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(135deg, #A855F7 0%, #7E22CE 100%);
  color: #fff;
  border-radius: var(--radius-btn);
  font-weight: 700;
  font-size: 0.95rem;
  padding: 0.6rem 1.4rem;
  text-decoration: none;
  border: none;
  position: relative;
  overflow: hidden;
  transition: all 0.35s var(--ease-spring);
  box-shadow: 0 4px 12px rgba(168, 85, 247, 0.25);
}
.btn-premium-purple::before {
  content: '';
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: linear-gradient(180deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}
.btn-premium-purple:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px -6px rgba(168, 85, 247, 0.4);
  color: #fff;
}
.btn-premium-purple:hover::before {
  opacity: 1;
}
.btn-premium-purple:active {
  transform: scale(0.97) translateY(0);
}
.action-icon {
  animation: premium-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
@keyframes premium-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.15); opacity: 0.8; }
}
</style>
