<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4 class="fw-bold mb-0">Quản lý Banner</h4>
      <button class="btn btn-bakery btn-sm d-flex align-items-center gap-1" @click="openModal()">
        <PhPlus weight="bold" size="18" /> Thêm Banner
      </button>
    </div>

    <div class="bakery-card">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead><tr>
            <th>Hình ảnh</th><th>Tiêu đề</th><th>Link URL</th><th>Thứ tự</th><th>Trạng thái</th><th>Hành động</th>
          </tr></thead>
          <tbody>
            <tr v-for="b in banners" :key="b.bannerId" :class="{ 'table-secondary': !b.isActive }">
              <td>
                <img :src="b.imageUrl" alt="banner" style="height: 50px; width: 120px; object-fit: cover; border-radius: 4px;" />
              </td>
              <td class="fw-semibold">{{ b.title }}</td>
              <td class="small text-sub" style="max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                <a :href="b.link" target="_blank" v-if="b.link">{{ b.link }}</a>
                <span v-else>—</span>
              </td>
              <td>{{ b.position }}</td>
              <td>
                <span :class="b.isActive ? 'badge bg-success' : 'badge bg-secondary'">{{ b.isActive ? 'Hiện' : 'Ẩn' }}</span>
              </td>
              <td>
                <div class="d-flex gap-1">
                  <button class="btn btn-sm btn-bakery-outline" @click="openModal(b)">✏️</button>
                  <button class="btn btn-sm btn-outline-secondary" @click="toggleBanner(b)">
                    {{ b.isActive ? '🙈' : '👁️' }}
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteBanner(b)">🗑️</button>
                </div>
              </td>
            </tr>
            <tr v-if="!banners.length"><td colspan="6" class="text-center text-sub py-4">Chưa có banner nào</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="bakery-card modal-box">
        <h5 class="fw-bold mb-3">{{ editing ? 'Sửa Banner' : 'Thêm Banner' }}</h5>
        <form @submit.prevent="saveBanner">
          <div class="mb-3">
            <label class="form-label small fw-semibold">Tiêu đề *</label>
            <input v-model="form.title" class="bakery-input" required />
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">URL Hình ảnh *</label>
            <input v-model="form.imageUrl" class="bakery-input" required placeholder="https://..." />
            <div v-if="form.imageUrl" class="mt-2 text-center">
               <img :src="form.imageUrl" alt="preview" style="max-height: 100px; max-width: 100%; border-radius: 8px; object-fit: cover;" />
            </div>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">Link chuyển hướng (Tùy chọn)</label>
            <input v-model="form.link" class="bakery-input" placeholder="VD: /products?category=1" />
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">Thứ tự ưu tiên (Số nhỏ xếp trước)</label>
            <input v-model.number="form.position" type="number" class="bakery-input" required />
          </div>
          <div class="d-flex gap-2 justify-content-end mt-4">
            <button type="button" class="btn btn-bakery-outline btn-sm" @click="showModal = false">Hủy</button>
            <button type="submit" class="btn btn-bakery btn-sm" :disabled="saving">
              {{ saving ? 'Đang lưu...' : 'Lưu' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin.api'
import { PhPlus } from '@phosphor-icons/vue'
import { toast } from 'vue3-toastify'

const banners = ref([])
const showModal = ref(false)
const editing = ref(null)
const saving = ref(false)
const form = ref({ title: '', imageUrl: '', link: '', position: 0 })

async function fetchBanners() {
  try {
    const { data } = await adminApi.getBanners()
    banners.value = data.data || []
  } catch { banners.value = [] }
}

function openModal(b = null) {
  editing.value = b
  if (b) {
    form.value = { title: b.title, imageUrl: b.imageUrl, link: b.link || '', position: b.position }
  } else {
    form.value = { title: '', imageUrl: '', link: '', position: banners.value.length }
  }
  showModal.value = true
}

async function saveBanner() {
  saving.value = true
  try {
    if (editing.value) {
      await adminApi.updateBanner(editing.value.bannerId, form.value)
      toast.success('Cập nhật thành công')
    } else {
      await adminApi.createBanner(form.value)
      toast.success('Thêm banner thành công')
    }
    showModal.value = false
    await fetchBanners()
  } catch (e) { toast.error(e.response?.data?.message || 'Lỗi') }
  finally { saving.value = false }
}

async function toggleBanner(b) {
  try {
    await adminApi.toggleBanner(b.bannerId)
    await fetchBanners()
  } catch (e) { toast.error('Lỗi') }
}

async function deleteBanner(b) {
  if (!confirm(`Xóa banner "${b.title}"?`)) return
  try {
    await adminApi.deleteBanner(b.bannerId)
    await fetchBanners()
    toast.success('Đã xóa')
  } catch (e) { toast.error('Lỗi') }
}

onMounted(fetchBanners)
</script>

<style scoped>
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.4); z-index: 1050; display: flex; align-items: center; justify-content: center; }
.modal-box { width: 100%; max-width: 600px; }
</style>
