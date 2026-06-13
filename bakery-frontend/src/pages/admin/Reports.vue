<template>
  <div class="admin-reports">
    <!-- REVENUE CHART & STATUS BREAKDOWN -->
    <div class="row g-4 mb-4">
      <div class="col-lg-8">
        <div class="bakery-card h-100">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <h6 class="fw-bold mb-0" style="color: var(--text-main)">Biểu đồ Doanh thu</h6>
            <div class="d-flex gap-1 p-1 rounded-3" style="background: var(--bg-muted)">
              <button class="btn btn-sm border-0 fw-semibold px-3 rounded-3" :class="period === '7days' ? 'period-active' : 'period-inactive'" @click="changePeriod('7days')">7 Ngày</button>
              <button class="btn btn-sm border-0 fw-semibold px-3 rounded-3" :class="period === '30days' ? 'period-active' : 'period-inactive'" @click="changePeriod('30days')">30 Ngày</button>
              <button class="btn btn-sm border-0 fw-semibold px-3 rounded-3" :class="period === '12months' ? 'period-active' : 'period-inactive'" @click="changePeriod('12months')">12 Tháng</button>
            </div>
          </div>
          <div style="height: 300px; position: relative;">
            <canvas ref="reportChartRef"></canvas>
          </div>
        </div>
      </div>
      <div class="col-lg-4">
        <div class="bakery-card h-100">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <h6 class="fw-bold mb-0" style="color: var(--text-main)">Tỷ lệ Trạng thái Đơn</h6>
          </div>
          <div style="height: 300px; display: flex; justify-content: center; position: relative;">
            <canvas ref="pieChartRef"></canvas>
          </div>
        </div>
      </div>
    </div>

    <!-- AI INSIGHTS REPORT (Full page view) -->
    <div class="bakery-card">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <div class="d-flex align-items-center gap-2">
          <PhSparkle size="24" weight="fill" color="var(--primary)" />
          <h6 class="fw-bold mb-0" style="color: var(--text-main)">Báo cáo Thông minh (AI Insights)</h6>
        </div>
        <button class="btn btn-outline-primary btn-sm d-flex align-items-center gap-2" @click="generateNewInsight" :disabled="isLoadingInsight">
          <PhArrowsClockwise size="16" weight="bold" :class="{'spin': isLoadingInsight}" />
          Cập nhật báo cáo mới
        </button>
      </div>

      <div class="ai-insight-content" v-if="!isLoadingInsight">
        <div v-if="aiInsightHtml" class="markdown-body" v-html="aiInsightHtml"></div>
        <div v-else class="text-center py-5 text-muted">
          Chưa có báo cáo AI. Hãy nhấn "Cập nhật báo cáo mới".
        </div>
      </div>
      <div v-else class="text-center py-5">
        <div class="spinner-border text-primary mb-3" role="status"></div>
        <div style="color: var(--text-sub)">AI đang phân tích dữ liệu kinh doanh... (có thể mất 10-15 giây)</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { adminApi } from '@/api/admin.api'
import { toast } from 'vue3-toastify'
import { marked } from 'marked'
import { Chart, registerables } from 'chart.js'
import { PhSparkle, PhArrowsClockwise } from '@phosphor-icons/vue'

Chart.register(...registerables)

const reportChartRef = ref(null)
const pieChartRef = ref(null)

let lineChartInstance = null
let pieChartInstance = null

const aiInsightText = ref('')
const aiInsightHtml = ref('')
const isLoadingInsight = ref(false)

const period = ref('30days')

onMounted(async () => {
  loadAiInsight()
  await loadCharts()
})

onUnmounted(() => {
  if (lineChartInstance) lineChartInstance.destroy()
  if (pieChartInstance) pieChartInstance.destroy()
})

const changePeriod = async (newPeriod) => {
  period.value = newPeriod
  await loadCharts()
}

const loadCharts = async () => {
  try {
    const [lineRes, pieRes] = await Promise.all([
      adminApi.getRevenueChart(period.value),
      adminApi.getOrderStatusBreakdown()
    ])

    const lineData = lineRes.data?.data || []
    const pieData = pieRes.data?.data || {}

    await nextTick()

    if (reportChartRef.value) {
      if (lineChartInstance) lineChartInstance.destroy()
      
      const isDark = document.documentElement.getAttribute('data-theme') === 'dark'
      const lineColor = isDark ? '#E07A54' : '#C85A32'
      const textColor = isDark ? '#A89888' : '#756B64'

      lineChartInstance = new Chart(reportChartRef.value, {
        type: 'line',
        data: {
          labels: lineData.map(d => d.date || d.month),
          datasets: [{
            label: 'Doanh thu',
            data: lineData.map(d => d.revenue),
            borderColor: lineColor,
            backgroundColor: isDark ? 'rgba(224, 122, 84, 0.1)' : 'rgba(200, 90, 50, 0.1)',
            fill: true,
            tension: 0.3
          }]
        },
        options: {
          responsive: true, maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: {
            x: { ticks: { color: textColor } },
            y: { ticks: { color: textColor } }
          }
        }
      })
    }

    // 2. Pie Chart
    if (pieChartRef.value) {
      if (pieChartInstance) pieChartInstance.destroy()
      
      const labelsMap = {
        'PENDING': 'Chờ duyệt',
        'CONFIRMED': 'Đã xác nhận',
        'PREPARING': 'Chuẩn bị',
        'SHIPPED': 'Giao VC',
        'DELIVERING': 'Đang giao',
        'COMPLETED': 'Hoàn thành',
        'CANCELLED': 'Đã huỷ'
      }
      
      const labels = Object.keys(pieData).map(k => labelsMap[k] || k)
      const data = Object.values(pieData)
      
      const isDark = document.documentElement.getAttribute('data-theme') === 'dark'
      const pieColors = isDark 
        ? ['#C85A32', '#A0522D', '#D2B48C', '#8B4513', '#CD853F', '#DEB887', '#F4A460']
        : ['#C85A32', '#E07A54', '#D2B48C', '#A0522D', '#CD853F', '#F4A460', '#DEB887']

      pieChartInstance = new Chart(pieChartRef.value, {
        type: 'doughnut',
        data: {
          labels: labels,
          datasets: [{
            data: data,
            backgroundColor: pieColors,
            borderWidth: 1,
            borderColor: isDark ? '#2A1E18' : '#fff',
            hoverOffset: 8
          }]
        },
        options: {
          responsive: true, maintainAspectRatio: false,
          cutout: '65%',
          plugins: {
            legend: { position: 'right', labels: { boxWidth: 12, font: { size: 11 }, color: isDark ? '#A89888' : '#756B64' } }
          }
        }
      })
    }

  } catch (err) {
    console.error(err)
  }
}

const loadAiInsight = async () => {
  try {
    isLoadingInsight.value = true
    const res = await adminApi.getAiInsight()
    if (res.data?.data) {
      aiInsightText.value = res.data.data
      aiInsightHtml.value = marked.parse(res.data.data)
    }
  } catch (error) {
    console.error('Lỗi tải AI insight:', error)
  } finally {
    isLoadingInsight.value = false
  }
}

const generateNewInsight = async () => {
  try {
    isLoadingInsight.value = true
    const res = await adminApi.triggerAiInsight()
    if (res.data?.data) {
      aiInsightText.value = res.data.data
      aiInsightHtml.value = marked.parse(res.data.data)
      toast.success('Đã cập nhật báo cáo AI mới nhất')
    }
  } catch (error) {
    console.error('Lỗi tạo AI insight mới:', error)
    toast.error('Có lỗi xảy ra khi yêu cầu AI')
  } finally {
    isLoadingInsight.value = false
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

.ai-insight-content {
  background: var(--bg-muted);
  border-radius: var(--radius-md);
  padding: 24px;
  border: 1px solid var(--border-light);
  max-height: 600px;
  overflow-y: auto;
}

.markdown-body {
  color: var(--text-main);
  font-size: 0.95rem;
  line-height: 1.6;
}
.markdown-body :deep(h1), .markdown-body :deep(h2), .markdown-body :deep(h3) {
  color: var(--primary);
  margin-top: 1.5rem;
  margin-bottom: 1rem;
  font-weight: 700;
}
.markdown-body :deep(h3) {
  font-size: 1.25rem;
}
.markdown-body :deep(ul), .markdown-body :deep(ol) {
  padding-left: 2rem;
  margin-bottom: 1.5rem;
  background: var(--bg-surface);
  padding: 16px 16px 16px 32px;
  border-radius: var(--radius-md);
  border-left: 4px solid var(--primary);
  box-shadow: var(--shadow-sm);
}
.markdown-body :deep(li) {
  margin-bottom: 0.8rem;
}
.markdown-body :deep(strong) {
  color: var(--primary);
  font-weight: 800;
}
.markdown-body :deep(p) {
  margin-bottom: 1.2rem;
  font-size: 1.05rem;
}
.markdown-body :deep(blockquote) {
  border-left: 4px solid var(--color-warning, #FFC107);
  background: var(--bg-surface);
  padding: 16px 20px;
  margin-bottom: 1.5rem;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  font-style: italic;
  color: var(--text-main);
}
.markdown-body :deep(hr) {
  border: 0;
  border-top: 1px solid var(--border-light);
  margin: 2rem 0;
}

/* Period Buttons styling */
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
</style>
