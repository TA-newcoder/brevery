<template>
  <div>
    <!-- Error State -->
    <div v-if="error" class="error-state">
      <div class="error-state-icon mx-auto"><PhWarningCircle size="32" color="var(--color-danger)" /></div>
      <div class="error-state-title">Lỗi tải người dùng</div>
      <p class="error-state-text">{{ error }}</p>
      <button class="btn btn-bakery btn-sm" @click="error=null; fetchUsers()">Thử lại</button>
    </div>

    <div v-else class="bakery-card bakery-card--static">
      <div class="mb-3">
        <div class="position-relative" style="max-width:300px">
          <PhMagnifyingGlass size="18" weight="bold" class="position-absolute" style="left:12px;top:50%;transform:translateY(-50%);color:var(--text-muted);pointer-events:none" />
          <input v-model="search" class="bakery-input" style="padding-left:36px" placeholder="Tìm theo tên, email..." @input="debouncedFetch" />
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="table-responsive">
        <table class="admin-table">
          <thead><tr><th>Họ tên</th><th>Email</th><th>SĐT</th><th>Role</th><th>Trạng thái</th><th>Hành động</th></tr></thead>
          <tbody>
            <tr v-for="i in 5" :key="i">
              <td><div class="skeleton skeleton-text" style="width:120px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:160px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:90px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:60px;height:24px;border-radius:999px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:80px;height:24px;border-radius:999px"></div></td>
              <td><div class="skeleton skeleton-text" style="width:70px;height:30px;border-radius:8px"></div></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Empty -->
      <div v-else-if="users.length === 0" class="empty-state py-5">
        <div class="empty-state-icon mx-auto"><PhUsers size="36" color="var(--text-muted)" /></div>
        <div class="empty-state-title">Không tìm thấy người dùng</div>
        <p class="empty-state-text">{{ search ? 'Thử thay đổi từ khoá tìm kiếm' : 'Chưa có người dùng nào trong hệ thống' }}</p>
      </div>

      <!-- Data -->
      <div v-else class="table-responsive">
        <table class="admin-table">
          <thead><tr>
            <th class="sortable" @click="toggleSort('name')">Họ tên <PhCaretUpDown size="12" class="sort-icon" /></th>
            <th>Email</th>
            <th>SĐT</th>
            <th>Role</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr></thead>
          <tbody>
            <tr v-for="u in sortedUsers" :key="u.userId">
              <td class="fw-semibold" style="color: var(--text-main)">{{ u.fullName }}</td>
              <td style="color: var(--text-sub)">{{ u.email }}</td>
              <td style="color: var(--text-main)">{{ u.phone || '-' }}</td>
              <td><span :class="u.role === 'ADMIN' ? 'badge-caramel' : 'badge-purple'">{{ u.role }}</span></td>
              <td><span :class="u.isActive ? 'badge-green' : 'badge-pink'">{{ u.isActive ? 'Hoạt động' : 'Bị khóa' }}</span></td>
              <td>
                <button v-if="u.role !== 'ADMIN'" class="btn btn-sm btn-bakery-outline d-flex align-items-center gap-1" @click="toggleUser(u.userId)">
                  <component :is="u.isActive ? PhLockSimple : PhLockSimpleOpen" size="16" weight="bold" />
                  {{ u.isActive ? 'Khóa' : 'Mở' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="mt-3"><Pagination v-model="page" :total-pages="totalPages" @update:model-value="fetchUsers" /></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '@/api/admin.api'
import { toast } from 'vue3-toastify'
import Pagination from '@/components/Pagination.vue'
import { PhWarningCircle, PhUsers, PhMagnifyingGlass, PhCaretUpDown, PhLockSimple, PhLockSimpleOpen } from '@phosphor-icons/vue'

const users = ref([])
const page = ref(0)
const totalPages = ref(0)
const search = ref('')
const loading = ref(true)
const error = ref(null)
const sortKey = ref('')
const sortDir = ref('asc')

let timer = null
function debouncedFetch() { clearTimeout(timer); timer = setTimeout(() => { page.value = 0; fetchUsers() }, 300) }

function toggleSort(key) {
  if (sortKey.value === key) { sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc' }
  else { sortKey.value = key; sortDir.value = 'asc' }
}

const sortedUsers = computed(() => {
  if (!sortKey.value) return users.value
  const items = [...users.value]
  const dir = sortDir.value === 'asc' ? 1 : -1
  return items.sort((a, b) => {
    if (sortKey.value === 'name') return dir * (a.fullName || '').localeCompare(b.fullName || '')
    return 0
  })
})

async function fetchUsers() {
  loading.value = true; error.value = null
  try {
    const params = { page: page.value, size: 20 }
    if (search.value) params.search = search.value
    const { data } = await adminApi.getUsers(params)
    users.value = data.data?.content || []
    totalPages.value = data.data?.totalPages || 0
  } catch (err) {
    error.value = err.response?.data?.message || 'Không thể tải danh sách người dùng'
    users.value = []
  } finally { loading.value = false }
}

async function toggleUser(id) {
  try { await adminApi.toggleUser(id); toast.success('Đã cập nhật'); fetchUsers() }
  catch (err) { toast.error(err.response?.data?.message || 'Thất bại') }
}

onMounted(fetchUsers)
</script>
