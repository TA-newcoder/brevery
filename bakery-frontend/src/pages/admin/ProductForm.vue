<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4 class="fw-bold mb-0">{{ isEdit ? '✏️ Sửa sản phẩm' : '➕ Thêm sản phẩm' }}</h4>
      <button v-if="isEdit" class="btn btn-danger btn-sm d-flex align-items-center gap-2" @click="handleDelete" type="button">
        <PhTrash size="18" weight="bold" /> Xoá sản phẩm
      </button>
    </div>
    
    <div v-if="loadingDetail" class="text-center py-5">
      <div class="spinner-border text-bakery" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
    <form v-else @submit.prevent="handleSubmit" class="row g-4">
      <div class="col-lg-8">
        <div class="bakery-card mb-4">
          <div class="mb-3">
            <label class="form-label fw-semibold">Tên sản phẩm *</label>
            <input v-model="form.name" class="bakery-input" required />
          </div>
          <div class="mb-3">
            <label class="form-label fw-semibold">Mô tả</label>
            <textarea v-model="form.description" class="bakery-input" rows="4"></textarea>
          </div>
          <div class="row g-3 mb-3">
            <div class="col-md-6">
              <label class="form-label fw-semibold">Danh mục</label>
              <input v-model.number="form.categoryId" type="number" class="bakery-input" placeholder="ID danh mục" required />
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">Trạng thái *</label>
              <select v-model="form.status" class="bakery-input" required>
                <option value="ACTIVE">Đang bán (Active)</option>
                <option value="PAUSED">Tạm ngưng (Paused)</option>
                <option value="SOLD_OUT">Hết hàng (Sold Out)</option>
              </select>
            </div>
          </div>
        </div>
        <div class="bakery-card">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="fw-bold mb-0">Biến thể (Kích thước / Giá)</h6>
            <PhInfo size="20" class="text-sub" title="Thêm kích thước và giá tương ứng cho sản phẩm" />
          </div>
          <div class="row g-2 mb-2 px-2 text-sub small fw-semibold">
            <div class="col-4">Kích thước / Loại</div>
            <div class="col-3">Giá bán (VNĐ)</div>
            <div class="col-3">Tồn kho</div>
            <div class="col-2 text-center">Xóa</div>
          </div>
          <div v-for="(v, i) in form.variants" :key="i" class="row g-2 mb-2 align-items-center">
            <div class="col-4"><input v-model="v.size" class="bakery-input" placeholder="Tên (S, M, L)" required /></div>
            <div class="col-3"><input v-model.number="v.price" type="number" class="bakery-input" placeholder="Giá" required min="0" /></div>
            <div class="col-3"><input v-model.number="v.stock" type="number" class="bakery-input" placeholder="Tồn kho" min="0" /></div>
            <div class="col-2 text-center"><button type="button" class="btn btn-sm btn-outline-danger" @click="form.variants.splice(i,1)"><PhTrash size="16" weight="bold" /></button></div>
          </div>
          <button type="button" class="btn btn-sm btn-bakery-outline mt-2" @click="form.variants.push({ size: '', price: 0, stock: 0 })">+ Thêm biến thể</button>
        </div>
      </div>
      <div class="col-lg-4">
        <div class="bakery-card mb-4">
          <h6 class="fw-bold mb-3">Ảnh sản phẩm</h6>
          <input type="file" multiple accept="image/*" @change="onFiles" class="form-control mb-2" />
          <div class="d-flex flex-wrap gap-2">
            <div v-for="(f,i) in previewUrls" :key="i" class="position-relative">
              <img :src="f" style="width:80px; height:80px; object-fit:cover; border-radius:12px" />
            </div>
          </div>
        </div>
        <button type="submit" class="btn btn-bakery w-100 py-2" :disabled="submitting">
          {{ submitting ? 'Đang lưu...' : (isEdit ? 'Cập nhật' : 'Tạo sản phẩm') }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api/product.api'
import { adminApi } from '@/api/admin.api'
import { toast } from 'vue3-toastify'
import { PhTrash, PhInfo } from '@phosphor-icons/vue'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const submitting = ref(false)
const loadingDetail = ref(false)
const files = ref([])
const previewUrls = ref([])

const form = reactive({
  name: '', description: '', categoryId: null, status: 'ACTIVE',
  variants: [{ size: 'Mặc định', price: 0, stock: 0 }],
})

function onFiles(e) {
  files.value = Array.from(e.target.files)
  previewUrls.value = files.value.map(f => URL.createObjectURL(f))
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEdit.value) {
      await adminApi.updateProduct(route.params.id, form)
      toast.success('Đã cập nhật sản phẩm')
    } else {
      const fd = new FormData()
      fd.append('product', new Blob([JSON.stringify(form)], { type: 'application/json' }))
      files.value.forEach(f => fd.append('images', f))
      await adminApi.createProduct(fd)
      toast.success('Đã tạo sản phẩm mới')
    }
    router.push({ name: 'admin-products' })
  } catch (err) { toast.error(err.response?.data?.message || 'Lỗi') }
  finally { submitting.value = false }
}

async function handleDelete() {
  if (confirm('Bạn có chắc chắn muốn xoá sản phẩm này? Hành động này không thể hoàn tác.')) {
    try {
      await adminApi.deleteProduct(route.params.id)
      toast.success('Đã xoá sản phẩm')
      router.push({ name: 'admin-products' })
    } catch (err) {
      toast.error(err.response?.data?.message || 'Xoá sản phẩm thất bại')
    }
  }
}

onMounted(async () => {
  if (isEdit.value) {
    loadingDetail.value = true
    try {
      const { data } = await productApi.getProductDetail(route.params.id)
      const p = data.data
      Object.assign(form, { 
        name: p.name, 
        description: p.description, 
        categoryId: p.categoryId, 
        status: p.status || (p.isAvailable ? 'ACTIVE' : 'PAUSED'),
        variants: p.variants ? p.variants.map(v => ({ size: v.size, price: v.price, stock: v.stock })) : [] 
      })
      if (p.images) previewUrls.value = p.images.map(i => i.imageUrl)
    } catch (err) { 
      toast.error('Không thể tải chi tiết sản phẩm')
    } finally {
      loadingDetail.value = false
    }
  }
})
</script>
