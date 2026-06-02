<template>
  <div>
    <!-- KPI GRID -->
    <div class="kpi-grid mb-4">
      <div class="kpi-card" v-for="(kpi, i) in kpis" :key="i">
        <div class="d-flex justify-content-between align-items-start mb-3">
          <span class="text-sub fw-semibold" style="font-size: 0.95rem">{{ kpi.label }}</span>
          <div class="kpi-icon-wrap">
            <component :is="kpi.icon" size="24" weight="bold" />
          </div>
        </div>
        <h3 class="fw-bold mb-1" style="color: var(--text-main)">{{ kpi.value }}</h3>
        <div class="small fw-semibold mt-2" style="color: var(--primary)">{{ kpi.trend }}</div>
      </div>
    </div>

    <!-- WIDGET GRID (CHART & INSIGHT) -->
    <div class="widget-grid mb-4">
      <!-- Revenue Chart -->
      <div class="bakery-card" style="height: 400px; display: flex; flex-direction: column;">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h5 class="fw-bold mb-0">Doanh thu</h5>
          <div class="d-flex gap-2 bg-light rounded-pill p-1">
            <button v-for="p in periods" :key="p.value" 
              :class="['btn btn-sm rounded-pill border-0 fw-semibold px-3', period === p.value ? 'bg-white shadow-sm text-dark' : 'text-muted']"
              @click="period = p.value; fetchChart()">
              {{ p.label }}
            </button>
          </div>
        </div>
        <div style="flex: 1; min-height: 0;">
          <canvas ref="chartRef"></canvas>
        </div>
      </div>

      <!-- AI Insight -->
      <div class="bakery-card" style="height: 400px; display: flex; flex-direction: column; padding-right: 12px;">
        <div class="d-flex align-items-center justify-content-between mb-4">
          <div class="d-flex align-items-center gap-3">
            <div class="kpi-icon-wrap" style="width: 40px; height: 40px;">
              <PhRobot size="22" weight="bold" color="var(--primary)" />
            </div>
            <h5 class="fw-bold mb-0">AI Insight</h5>
          </div>
          <button class="btn btn-sm text-muted border-0 bg-light rounded-circle p-2" @click="triggerInsight" :disabled="insightLoading" title="Làm mới">
            <PhArrowsClockwise size="18" :class="{'spin': insightLoading}" />
          </button>
        </div>
        
        <div class="insight-content" style="flex: 1; overflow-y: auto; padding-right: 12px;">
          <div v-if="insightLoading" class="text-center text-muted py-5">Đang phân tích dữ liệu...</div>
          <div v-else-if="!insightList.length" class="text-center text-muted py-5">Chưa có phân tích nào.</div>
          <ul v-else class="list-unstyled mb-0 d-flex flex-column gap-3">
            <li v-for="(item, idx) in insightList" :key="idx" class="d-flex gap-3 align-items-start p-3 rounded-4" style="background: #FAF8F5;">
              <component :is="item.icon" size="24" :color="item.color" weight="duotone" class="flex-shrink-0 mt-1" />
              <div>
                <div class="small fw-semibold" style="color: var(--text-main); line-height: 1.4;">{{ item.text }}</div>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- BOTTOM SECTION: TOP PRODUCTS & RECENT ORDERS -->
    <div class="row g-4">
      <div class="col-lg-6">
        <div class="bakery-card h-100">
          <h5 class="fw-bold mb-4">Top sản phẩm</h5>
          <div class="table-responsive">
            <table class="table table-borderless align-middle mb-0">
              <thead class="border-bottom">
                <tr>
                  <th class="text-sub fw-semibold py-2">Sản phẩm</th>
                  <th class="text-sub fw-semibold py-2">Đã bán</th>
                  <th class="text-sub fw-semibold py-2 text-end">Doanh thu</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in topProducts" :key="p.productId">
                  <td class="fw-semibold py-3">{{ p.name }}</td>
                  <td class="py-3">{{ p.totalSold }}</td>
                  <td class="text-end fw-bold py-3" style="color: var(--primary)">{{ formatPrice(p.minPrice * p.totalSold) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-lg-6">
        <div class="bakery-card h-100">
          <h5 class="fw-bold mb-4">Đơn hàng gần đây</h5>
          <div class="d-flex flex-column gap-3">
            <div v-for="order in recentOrders" :key="order.orderId" class="d-flex justify-content-between align-items-center p-3 rounded-4" style="background: #FAF8F5;">
              <div class="d-flex align-items-center gap-3">
                <div class="kpi-icon-wrap" style="width: 40px; height: 40px; background: #fff; box-shadow: var(--shadow-soft);">
                  <PhPackage size="20" color="var(--primary)" />
                </div>
                <div>
                  <div class="fw-bold" style="color: var(--text-main)">#{{ order.orderCode }}</div>
                  <div class="text-sub small">{{ order.customerName || order.recipientName }}</div>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { adminApi } from '@/api/admin.api'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import { Chart, registerables } from 'chart.js'
import { PhMoney, PhClipboardText, PhUsers, PhPackage, PhRobot, PhArrowsClockwise, PhTrendUp, PhWarningCircle, PhInfo } from '@phosphor-icons/vue'

Chart.register(...registerables)

const summary = ref({})
const topProducts = ref([])
const recentOrders = ref([])
const insight = ref('')
const insightLoading = ref(false)
const chartRef = ref(null)
const period = ref('7days')
let chartInstance = null
let refreshTimer = null

const periods = [
  { value: '7days', label: '7 ngày' },
  { value: '30days', label: '30 ngày' },
  { value: '12months', label: '12 tháng' },
]

function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }

const kpis = computed(() => [
  { label: 'Tổng doanh thu', value: formatPrice(summary.value.totalRevenue), icon: PhMoney, trend: 'Đang cập nhật' },
  { label: 'Đơn chờ xử lý', value: summary.value.pendingOrders || 0, icon: PhClipboardText, trend: 'Cần chú ý' },
  { label: 'Tổng khách hàng', value: summary.value.totalCustomers || 0, icon: PhUsers, trend: 'Khách hàng' },
  { label: 'Tổng sản phẩm', value: summary.value.totalProducts || 0, icon: PhPackage, trend: 'Trong kho' },
])

const insightList = computed(() => {
  if (!insight.value) return []
  return insight.value.split('\n').filter(line => line.trim().length > 3).map(line => {
    let cleanLine = line.replace(/^[\*\-\d\.]+\s*/, '').trim();
    let type = 'info';
    let icon = PhInfo;
    let color = '#7B6CC8'; // Purple
    const lower = cleanLine.toLowerCase();
    
    if (lower.includes('tăng') || lower.includes('tốt') || lower.includes('nhiều')) {
      type = 'success'; icon = PhTrendUp; color = '#4A9B5C'; // Green
    } else if (lower.includes('giảm') || lower.includes('chú ý') || lower.includes('thấp') || lower.includes('cần')) {
      type = 'warning'; icon = PhWarningCircle; color = '#C85A32'; // Terracotta
    }
    
    return { text: cleanLine, icon, color }
  })
})

async function fetchAll() {
  try {
    const [s, t, o] = await Promise.all([
      adminApi.getSummary(), adminApi.getTopProducts({ limit: 5 }), adminApi.getOrders({ page: 0, size: 4 }),
    ])
    summary.value = s.data?.data || {}
    topProducts.value = t.data?.data || []
    recentOrders.value = o.data?.data?.content || []
  } catch { /* silent */ }
}

async function fetchChart() {
  try {
    const { data } = await adminApi.getRevenueChart(period.value)
    const chartData = data.data || []
    await nextTick()
    if (chartInstance) chartInstance.destroy()
    if (!chartRef.value) return
    
    const ctx = chartRef.value.getContext('2d');
    const gradient = ctx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, 'rgba(200, 90, 50, 0.2)');
    gradient.addColorStop(1, 'rgba(250, 242, 233, 0)');

    chartInstance = new Chart(chartRef.value, {
      type: 'line',
      data: {
        labels: chartData.map(d => d.date || d.month),
        datasets: [{
          label: 'Doanh thu', 
          data: chartData.map(d => d.revenue),
          borderColor: '#C85A32', 
          backgroundColor: gradient,
          fill: true, 
          tension: 0.4,
          pointRadius: 4,
          pointBackgroundColor: '#fff',
          pointBorderColor: '#C85A32',
          pointBorderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#fff',
            titleColor: '#2C1E16',
            bodyColor: '#756B64',
            borderColor: '#EAE0DA',
            borderWidth: 1,
            padding: 12,
            boxPadding: 6,
            usePointStyle: true,
            callbacks: {
              label: function(context) {
                return ' ' + formatPrice(context.raw);
              }
            }
          }
        },
        scales: {
          x: { grid: { display: false } },
          y: { 
            beginAtZero: true, 
            border: { display: false },
            grid: { color: '#F4E2D8', drawTicks: false }
          },
        },
      },
    })
  } catch { /* silent */ }
}

async function fetchInsight() {
  try { const { data } = await adminApi.getAiInsight(); insight.value = data.data || '' } catch { /* */ }
}

async function triggerInsight() {
  insightLoading.value = true
  try { await adminApi.triggerAiInsight(); await fetchInsight() } catch { /* */ }
  finally { insightLoading.value = false }
}

onMounted(() => {
  fetchAll(); fetchChart(); fetchInsight()
  refreshTimer = setInterval(fetchAll, 30000)
})
onUnmounted(() => { clearInterval(refreshTimer); if (chartInstance) chartInstance.destroy() })
</script>

<style scoped>
.kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
.kpi-card { background: var(--bg-surface); border-radius: var(--radius-card); box-shadow: var(--shadow-soft); padding: 24px; transition: transform 0.2s; }
.kpi-card:hover { transform: translateY(-2px); }
.kpi-icon-wrap { width: 48px; height: 48px; border-radius: 50%; background: var(--primary-light); display: inline-flex; align-items: center; justify-content: center; flex-shrink: 0; line-height: 1; }

.widget-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 24px; }

.insight-content::-webkit-scrollbar { width: 4px; }
.insight-content::-webkit-scrollbar-track { background: transparent; }
.insight-content::-webkit-scrollbar-thumb { background: #EAE0DA; border-radius: 10px; }
.insight-content::-webkit-scrollbar-thumb:hover { background: var(--primary-light); }

.spin { animation: spin 1s linear infinite; }
@keyframes spin { 100% { transform: rotate(360deg); } }

@media (max-width: 991px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .widget-grid { grid-template-columns: 1fr; }
}
@media (max-width: 576px) {
  .kpi-grid { grid-template-columns: 1fr; }
}
</style>
