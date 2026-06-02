<template>
  <div>
    <div class="bakery-card">
      <div class="mb-3">
        <input v-model="search" class="bakery-input" style="max-width:300px" placeholder="Tìm theo tên, email..." @input="debouncedFetch" />
      </div>
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead><tr><th>Họ tên</th><th>Email</th><th>SĐT</th><th>Role</th><th>Trạng thái</th><th>Hành động</th></tr></thead>
          <tbody>
            <tr v-for="u in users" :key="u.userId">
              <td class="fw-semibold">{{ u.fullName }}</td>
              <td class="text-sub">{{ u.email }}</td>
              <td>{{ u.phone || '-' }}</td>
              <td><span :class="u.role === 'ADMIN' ? 'badge-caramel' : 'badge-purple'">{{ u.role }}</span></td>
              <td><span :class="u.isActive ? 'badge-green' : 'badge bg-secondary'">{{ u.isActive ? 'Hoạt động' : 'Bị khóa' }}</span></td>
              <td>
                <button v-if="u.role !== 'ADMIN'" class="btn btn-sm btn-bakery-outline" @click="toggleUser(u.userId)">
                  {{ u.isActive ? '🔒 Khóa' : '🔓 Mở' }}
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
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin.api'
import { toast } from 'vue3-toastify'
import Pagination from '@/components/Pagination.vue'

const users = ref([])
const page = ref(0)
const totalPages = ref(0)
const search = ref('')

let timer = null
function debouncedFetch() { clearTimeout(timer); timer = setTimeout(() => { page.value = 0; fetchUsers() }, 300) }

async function fetchUsers() {
  try {
    const params = { page: page.value, size: 20 }
    if (search.value) params.search = search.value
    const { data } = await adminApi.getUsers(params)
    users.value = data.data?.content || []
    totalPages.value = data.data?.totalPages || 0
  } catch { users.value = [] }
}

async function toggleUser(id) {
  try { await adminApi.toggleUser(id); toast.success('Đã cập nhật'); fetchUsers() }
  catch (err) { toast.error(err.response?.data?.message || 'Thất bại') }
}

onMounted(fetchUsers)
</script>
