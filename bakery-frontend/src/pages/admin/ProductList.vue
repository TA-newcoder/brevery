<template>
  <div>
    <div class="d-flex justify-content-end mb-4">
      <router-link to="/admin/products/new" class="btn btn-bakery">+ Thêm sản phẩm</router-link>
    </div>
    <div class="bakery-card">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead><tr><th>Ảnh</th><th>Tên</th><th>Danh mục</th><th>Giá</th><th>Tồn kho</th><th>Trạng thái</th><th>Hành động</th></tr></thead>
          <tbody>
            <tr v-for="p in products" :key="p.productId">
              <td><img :src="p.primaryImageUrl || '/images/cake.png'" style="width:50px;height:50px;object-fit:cover;border-radius:12px"/></td>
              <td class="fw-semibold">{{ p.name }}</td>
              <td><span class="badge-purple">{{ p.categoryName || '-' }}</span></td>
              <td>{{ formatPrice(p.minPrice) }}</td>
              <td>
                <span :class="p.totalStock > 20 ? 'text-success' : p.totalStock > 10 ? 'text-warning' : 'text-danger'" class="fw-bold">
                  {{ p.totalStock ?? '-' }}
                </span>
              </td>
              <td><span :class="p.isAvailable !== false ? 'badge-green' : 'badge bg-secondary'">{{ p.isAvailable !== false ? 'Đang bán' : 'Ẩn' }}</span></td>
              <td>
                <div class="d-flex gap-1">
                  <router-link :to="`/admin/products/${p.productId}/edit`" class="btn btn-sm btn-bakery-outline" title="Sửa"><PhPencilSimple size="18" /></router-link>
                  <button class="btn btn-sm btn-bakery-outline" @click="toggleProduct(p.productId)" title="Ẩn/Hiện">
                    <component :is="p.isAvailable !== false ? PhEye : PhEyeClosed" size="18" />
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteProduct(p.productId)" title="Xoá">
                    <PhTrash size="18" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="mt-3"><Pagination v-model="page" :total-pages="totalPages" @update:model-value="fetchProducts" /></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { productApi } from '@/api/product.api'
import { adminApi } from '@/api/admin.api'
import { toast } from 'vue3-toastify'
import Pagination from '@/components/Pagination.vue'
import { PhPencilSimple, PhEye, PhEyeClosed, PhTrash } from '@phosphor-icons/vue'

const products = ref([])
const page = ref(0)
const totalPages = ref(0)

function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }

async function fetchProducts() {
  try {
    const { data } = await productApi.getProducts({ page: page.value, size: 20 })
    products.value = data.data?.content || []
    totalPages.value = data.data?.totalPages || 0
  } catch { products.value = [] }
}

async function toggleProduct(id) {
  try { await adminApi.toggleProduct(id); toast.success('Đã cập nhật'); fetchProducts() }
  catch (err) { toast.error(err.response?.data?.message || 'Thất bại') }
}

async function deleteProduct(id) {
  if (confirm('Bạn có chắc chắn muốn xoá sản phẩm này? Hành động này không thể hoàn tác.')) {
    try {
      await adminApi.deleteProduct(id)
      toast.success('Đã xoá sản phẩm')
      fetchProducts()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Xoá sản phẩm thất bại')
    }
  }
}

onMounted(fetchProducts)
</script>
