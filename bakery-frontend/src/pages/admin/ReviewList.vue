<template>
  <div class="admin-reviews">
    <div class="d-flex justify-content-end align-items-center mb-4 flex-wrap gap-3">
      <div class="d-flex gap-2 bg-light p-1 rounded-3" style="background: var(--bg-muted)">
        <button class="btn btn-sm border-0 fw-semibold px-3 rounded-3" :class="filters.status === '' ? 'period-active' : 'period-inactive'" @click="filters.status = ''; fetchReviews(0)">Tất cả</button>
        <button class="btn btn-sm border-0 fw-semibold px-3 rounded-3" :class="filters.status === 'APPROVED' ? 'period-active' : 'period-inactive'" @click="filters.status = 'APPROVED'; fetchReviews(0)">Đang hiển thị</button>
        <button class="btn btn-sm border-0 fw-semibold px-3 rounded-3" :class="filters.status === 'HIDDEN' ? 'period-active' : 'period-inactive'" @click="filters.status = 'HIDDEN'; fetchReviews(0)">Đã ẩn</button>
      </div>
    </div>

    <div class="bakery-card">
      <div v-if="isLoading" class="text-center py-5">
        <span class="spinner-border text-primary" role="status"></span>
      </div>
      <div v-else-if="reviews.length === 0" class="text-center py-5 text-sub">
        Không tìm thấy đánh giá nào.
      </div>
      <div v-else class="table-responsive">
        <table class="admin-table">
          <thead>
            <tr>
              <th>Sản phẩm</th>
              <th>Khách hàng</th>
              <th>Đánh giá</th>
              <th>Trạng thái</th>
              <th>Phản hồi</th>
              <th class="text-end">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="review in reviews" :key="review.reviewId">
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="fw-semibold text-main text-truncate" style="max-width: 180px;" :title="review.productName">{{ review.productName }}</div>
                </div>
              </td>
              <td>
                <div class="fw-semibold">{{ review.userName }}</div>
                <div class="small text-sub">{{ formatDate(review.createdAt) }}</div>
              </td>
              <td>
                <div class="d-flex align-items-center mb-1">
                  <PhStar v-for="i in 5" :key="i" :weight="i <= review.rating ? 'fill' : 'regular'" :color="i <= review.rating ? '#FFC107' : '#E0E0E0'" size="16" />
                </div>
                <div class="small" style="max-width: 200px;">{{ review.comment }}</div>
              </td>
              <td>
                <span class="badge" :class="getStatusBadgeClass(review.status)">{{ getStatusLabel(review.status) }}</span>
              </td>
              <td>
                <div class="small text-truncate" style="max-width: 150px; color: var(--text-sub)" :title="review.adminReply || 'Chưa phản hồi'">
                  {{ review.adminReply || '—' }}
                </div>
              </td>
              <td class="text-end">
                <button class="btn btn-sm btn-bakery-ghost px-2 me-2" @click="openReplyModal(review)">
                  <PhChatCircleText size="16" />
                </button>
                <div class="dropdown d-inline">
                  <button class="btn btn-sm btn-outline-secondary" data-bs-toggle="dropdown"><PhDotsThree size="16" weight="bold" /></button>
                  <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0">
                    <li v-if="review.status === 'HIDDEN'"><a class="dropdown-item d-flex align-items-center gap-2" href="#" @click.prevent="updateStatus(review.reviewId, 'APPROVED')"><PhCheckCircle color="var(--color-success)" /> Mở lại (Hiển thị)</a></li>
                    <li v-if="review.status !== 'HIDDEN'"><a class="dropdown-item d-flex align-items-center gap-2" href="#" @click.prevent="updateStatus(review.reviewId, 'HIDDEN')"><PhEyeSlash color="var(--color-danger)" /> Ẩn bình luận</a></li>
                  </ul>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="d-flex justify-content-between align-items-center mt-4" v-if="totalPages > 1">
        <span class="text-sub small">Hiển thị trang {{ currentPage + 1 }} / {{ totalPages }}</span>
        <div class="d-flex gap-2">
          <button class="btn btn-sm btn-outline-secondary" :disabled="currentPage === 0" @click="fetchReviews(currentPage - 1)">Trang trước</button>
          <button class="btn btn-sm btn-outline-secondary" :disabled="currentPage >= totalPages - 1" @click="fetchReviews(currentPage + 1)">Trang sau</button>
        </div>
      </div>
    </div>

    <!-- REPLY MODAL -->
    <div class="modal fade" id="replyModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bakery-card p-0 overflow-hidden">
          <div class="modal-header border-0 bg-light pb-2">
            <h5 class="modal-title fw-bold" style="color: var(--text-main)">Phản hồi Đánh giá</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body pt-3">
            <div class="mb-3 p-3 rounded-3" style="background: var(--bg-muted)">
              <div class="d-flex align-items-center gap-1 mb-2">
                <PhStar v-for="i in 5" :key="i" :weight="i <= selectedReview?.rating ? 'fill' : 'regular'" :color="i <= selectedReview?.rating ? '#FFC107' : '#E0E0E0'" size="14" />
              </div>
              <div class="small fw-semibold text-main">"{{ selectedReview?.comment }}"</div>
              <div class="small text-sub mt-1">- {{ selectedReview?.userName }}</div>
            </div>
            
            <label class="form-label text-sub fw-semibold small">Nội dung phản hồi từ Cửa hàng</label>
            <textarea class="form-control bakery-input" v-model="replyText" rows="4" placeholder="Cảm ơn bạn đã đánh giá..."></textarea>
          </div>
          <div class="modal-footer border-0">
            <button type="button" class="btn btn-outline-secondary px-4 fw-semibold" data-bs-dismiss="modal">Hủy</button>
            <button type="button" class="btn btn-primary px-4 fw-semibold" @click="submitReply" :disabled="isReplying || !replyText.trim()">
              <span v-if="isReplying" class="spinner-border spinner-border-sm me-2" role="status"></span>
              Gửi Phản Hồi
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin.api'
import { toast } from 'vue3-toastify'
import { PhStar, PhChatCircleText, PhDotsThree, PhCheckCircle, PhEyeSlash } from '@phosphor-icons/vue'

const reviews = ref([])
const isLoading = ref(false)
const currentPage = ref(0)
const totalPages = ref(1)

const filters = ref({
  status: ''
})

const selectedReview = ref(null)
const replyText = ref('')
const isReplying = ref(false)
let replyModalInstance = null

onMounted(async () => {
  const { Modal } = await import('bootstrap')
  replyModalInstance = new Modal(document.getElementById('replyModal'))
  fetchReviews(0)
})

const formatDate = (d) => {
  const date = new Date(d)
  return `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getFullYear()}`
}

const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'APPROVED': return 'bg-success-subtle text-success border border-success-subtle'
    case 'HIDDEN': return 'bg-danger-subtle text-danger border border-danger-subtle'
    default: return 'bg-success-subtle text-success border border-success-subtle'
  }
}

const getStatusLabel = (status) => {
  switch (status) {
    case 'APPROVED': return 'Đang hiển thị'
    case 'HIDDEN': return 'Đã ẩn'
    default: return 'Đang hiển thị'
  }
}

const fetchReviews = async (page = 0) => {
  isLoading.value = true
  try {
    const params = { page, size: 10 }
    if (filters.value.status) params.status = filters.value.status
    
    const res = await adminApi.getReviews(params)
    reviews.value = res.data?.data?.content || []
    currentPage.value = res.data?.data?.number || 0
    totalPages.value = res.data?.data?.totalPages || 1
  } catch (error) {
    console.error(error)
  } finally {
    isLoading.value = false
  }
}

const updateStatus = async (id, status) => {
  try {
    await adminApi.updateReviewStatus(id, status)
    toast.success('Đã cập nhật trạng thái đánh giá')
    fetchReviews(currentPage.value)
  } catch (error) {
    toast.error('Có lỗi xảy ra')
  }
}

const openReplyModal = (review) => {
  selectedReview.value = review
  replyText.value = review.adminReply || ''
  replyModalInstance?.show()
}

const submitReply = async () => {
  if (!replyText.value.trim() || !selectedReview.value) return
  
  isReplying.value = true
  try {
    await adminApi.replyToReview(selectedReview.value.reviewId, replyText.value.trim())
    toast.success('Đã gửi phản hồi thành công')
    replyModalInstance?.hide()
    fetchReviews(currentPage.value)
  } catch (error) {
    toast.error('Có lỗi xảy ra')
  } finally {
    isReplying.value = false
  }
}
</script>

<style scoped>
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
