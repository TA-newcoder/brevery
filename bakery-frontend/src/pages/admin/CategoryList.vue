<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4 class="fw-bold mb-0">Quản lý danh mục</h4>
      <button class="btn btn-bakery btn-sm d-flex align-items-center gap-1" @click="openModal()">
        <PhPlus weight="bold" size="18" /> Thêm danh mục
      </button>
    </div>

    <div class="bakery-card">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead><tr>
            <th>#</th><th>Tên</th><th>Mô tả</th><th>Trạng thái</th><th>Hành động</th>
          </tr></thead>
          <tbody>
            <tr v-for="c in categories" :key="c.categoryId">
              <td>{{ c.categoryId }}</td>
              <td class="fw-semibold">{{ c.name }}</td>
              <td class="text-sub small">{{ c.description || '—' }}</td>
              <td>
                <span :class="c.isActive !== false ? 'badge bg-success' : 'badge bg-secondary'">
                  {{ c.isActive !== false ? 'Hiện' : 'Ẩn' }}
                </span>
              </td>
              <td>
                <div class="d-flex gap-1">
                  <button class="btn btn-sm btn-bakery-outline" @click="openModal(c)" title="Sửa">✏️</button>
                  <button class="btn btn-sm btn-outline-secondary" @click="toggleCat(c)" title="Ẩn/Hiện">
                    {{ c.isActive !== false ? '🙈' : '👁️' }}
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteCat(c)" title="Xóa">🗑️</button>
                </div>
              </td>
            </tr>
            <tr v-if="!categories.length"><td colspan="5" class="text-center text-sub py-4">Chưa có danh mục nào</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="bakery-card modal-box">
        <h5 class="fw-bold mb-3">{{ editing ? 'Sửa danh mục' : 'Thêm danh mục' }}</h5>
        <form @submit.prevent="saveCategory">
          <div class="mb-3">
            <label class="form-label small fw-semibold">Tên danh mục *</label>
            <input v-model="form.name" class="bakery-input" required />
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">Mô tả</label>
            <textarea v-model="form.description" class="bakery-input" rows="3"></textarea>
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">URL hình ảnh</label>
            <input v-model="form.imageUrl" class="bakery-input" placeholder="https://..." />
          </div>
          <div class="d-flex gap-2 justify-content-end">
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

const categories = ref([])
const showModal = ref(false)
const editing = ref(null)
const saving = ref(false)
const form = ref({ name: '', description: '', imageUrl: '' })

async function fetchCategories() {
  try {
    const { data } = await adminApi.getCategories()
    categories.value = data.data || []
  } catch { categories.value = [] }
}

function openModal(cat = null) {
  editing.value = cat
  form.value = cat
    ? { name: cat.name, description: cat.description || '', imageUrl: cat.imageUrl || '' }
    : { name: '', description: '', imageUrl: '' }
  showModal.value = true
}

async function saveCategory() {
  saving.value = true
  try {
    if (editing.value) {
      await adminApi.updateCategory(editing.value.categoryId, form.value)
      toast.success('Cập nhật thành công')
    } else {
      await adminApi.createCategory(form.value)
      toast.success('Thêm danh mục thành công')
    }
    showModal.value = false
    await fetchCategories()
  } catch (e) { toast.error(e.response?.data?.message || 'Lỗi') }
  finally { saving.value = false }
}

async function toggleCat(cat) {
  try {
    await adminApi.toggleCategory(cat.categoryId)
    await fetchCategories()
    toast.success('Đã cập nhật trạng thái')
  } catch (e) { toast.error(e.response?.data?.message || 'Lỗi') }
}

async function deleteCat(cat) {
  if (!confirm(`Xóa danh mục "${cat.name}"?`)) return
  try {
    await adminApi.deleteCategory(cat.categoryId)
    await fetchCategories()
    toast.success('Đã xóa danh mục')
  } catch (e) { toast.error(e.response?.data?.message || 'Không thể xóa (có sản phẩm liên kết)') }
}

onMounted(fetchCategories)
</script>

<style scoped>
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.4); z-index: 1050; display: flex; align-items: center; justify-content: center; }
.modal-box { width: 100%; max-width: 500px; }
</style>
