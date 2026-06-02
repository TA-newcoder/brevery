<template>
  <div class="container py-4">
    <button v-if="step === 'checkout' || step === 'confirm'" class="btn btn-sm text-decoration-none text-dark p-0 mb-3 d-flex align-items-center gap-2" @click="goBack">
      <PhArrowLeft size="18" weight="bold" /> Quay lại
    </button>

    <h2 class="fw-bold mb-4" v-if="step !== 'success' && step !== 'vnpay'">💳 Thanh toán</h2>

    <!-- BƯỚC 1: ĐIỀN THÔNG TIN (CHECKOUT) -->
    <div v-if="step === 'checkout'" class="row g-4">
      <div class="col-lg-7">
        <div class="bakery-card mb-4">
          <h5 class="fw-bold mb-3">Thông tin giao hàng</h5>
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label small fw-semibold">Họ tên người nhận *</label>
              <input v-model="form.recipientName" class="bakery-input" :class="{'border-danger': errors.recipientName}" required placeholder="Họ và tên" />
              <div v-if="errors.recipientName" class="text-danger small mt-1">Vui lòng nhập họ tên</div>
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">Số điện thoại *</label>
              <input v-model="form.recipientPhone" class="bakery-input" :class="{'border-danger': errors.recipientPhone}" required placeholder="0909..." />
              <div v-if="errors.recipientPhone" class="text-danger small mt-1">Vui lòng nhập số điện thoại</div>
            </div>
            
            <div class="col-md-4">
              <label class="form-label small fw-semibold">Tỉnh/TP *</label>
              <input v-model="form.city" class="bakery-input text-sub bg-light" disabled />
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">Quận/Huyện *</label>
              <select v-model="form.district" class="bakery-input" :class="{'border-danger': errors.district}" @change="onDistrictChange">
                <option value="">-- Chọn Quận/Huyện --</option>
                <option v-for="d in districts" :key="d.code" :value="d.name">{{ d.name }}</option>
              </select>
              <div v-if="errors.district" class="text-danger small mt-1">Chọn Quận/Huyện</div>
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">Phường/Xã *</label>
              <select v-model="form.ward" class="bakery-input" :class="{'border-danger': errors.ward}" :disabled="!form.district">
                <option value="">-- Chọn Phường/Xã --</option>
                <option v-for="w in wards" :key="w.code" :value="w.name">{{ w.name }}</option>
              </select>
              <div v-if="errors.ward" class="text-danger small mt-1">Chọn Phường/Xã</div>
            </div>

            <div class="col-12">
              <label class="form-label small fw-semibold">Số nhà, Tên đường *</label>
              <input v-model="form.address" class="bakery-input" :class="{'border-danger': errors.address}" placeholder="VD: 123 Nguyễn Huệ" required />
              <div v-if="errors.address" class="text-danger small mt-1">Vui lòng nhập địa chỉ cụ thể</div>
            </div>

            <div class="col-md-6">
              <label class="form-label small fw-semibold">Tòa nhà / Tầng / Phòng (Tuỳ chọn)</label>
              <input v-model="form.building" class="bakery-input" placeholder="VD: Tòa A, Tầng 5, P.502" />
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">Thời gian giao mong muốn</label>
              <select v-model="form.deliveryTime" class="bakery-input">
                <option value="Càng sớm càng tốt">Càng sớm càng tốt</option>
                <option value="Buổi sáng (7h–12h)">Buổi sáng (7h–12h)</option>
                <option value="Buổi chiều (12h–18h)">Buổi chiều (12h–18h)</option>
                <option value="Buổi tối (18h–22h)">Buổi tối (18h–22h)</option>
              </select>
            </div>

            <div class="col-12">
              <label class="form-label small fw-semibold">Ghi chú cho shipper</label>
              <textarea v-model="form.note" class="bakery-input" rows="2" placeholder="VD: Gọi trước khi đến, để hàng trước cửa, không bấm chuông..."></textarea>
            </div>
          </div>
        </div>

        <div class="bakery-card mb-4">
          <h5 class="fw-bold mb-3">Phương thức thanh toán</h5>
          <div class="d-flex flex-column gap-3">
            <label class="pm-option" :class="{active: form.paymentMethod === 'COD'}">
              <input type="radio" v-model="form.paymentMethod" value="COD" class="d-none">
              <div class="d-flex align-items-center gap-3 w-100">
                <div class="pm-icon bg-light text-success fs-4 rounded-circle d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">💵</div>
                <div>
                  <h6 class="mb-0 fw-bold">Thanh toán khi nhận hàng (COD)</h6>
                  <span class="small text-sub">Thanh toán bằng tiền mặt khi shipper giao hàng</span>
                </div>
                <PhCheckCircle v-if="form.paymentMethod === 'COD'" weight="fill" color="var(--bakery-primary)" size="24" class="ms-auto" />
              </div>
            </label>

            <label class="pm-option" :class="{active: form.paymentMethod === 'VNPAY'}">
              <input type="radio" v-model="form.paymentMethod" value="VNPAY" class="d-none">
              <div class="d-flex align-items-center gap-3 w-100">
                <div class="pm-icon bg-light text-primary fs-4 rounded-circle d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">🏦</div>
                <div>
                  <h6 class="mb-0 fw-bold">Chuyển khoản VNPay</h6>
                  <span class="small text-sub">Quét mã QR để thanh toán nhanh chóng</span>
                </div>
                <PhCheckCircle v-if="form.paymentMethod === 'VNPAY'" weight="fill" color="var(--bakery-primary)" size="24" class="ms-auto" />
              </div>
            </label>
          </div>
        </div>
      </div>

      <div class="col-lg-5">
        <div class="bakery-card" style="position: sticky; top: 80px">
          <h5 class="fw-bold mb-3">Đơn hàng ({{ cartStore.totalItems }} SP)</h5>
          <div v-for="item in cartStore.items" :key="item.variantId" class="d-flex justify-content-between mb-2 small">
            <span>{{ item.productName }} × {{ item.quantity }}</span>
            <span class="fw-semibold">{{ formatPrice(item.price * item.quantity) }}</span>
          </div>
          <hr />
          <div class="d-flex justify-content-between mb-2"><span class="text-sub">Tạm tính</span><span>{{ formatPrice(cartStore.totalPrice) }}</span></div>
          
          <div class="d-flex justify-content-between mb-2 align-items-center">
            <span class="text-sub d-flex align-items-center gap-1">
              Phí giao hàng 
              <span v-if="loadingShipping" class="spinner-border spinner-border-sm text-bakery ms-2" role="status"></span>
            </span>
            <span>
              <span v-if="cartStore.totalPrice >= 200000" class="text-success">Miễn phí</span>
              <span v-else-if="shippingFee > 0">{{ formatPrice(shippingFee) }}</span>
              <span v-else-if="outOfRange" class="text-warning small text-end" style="max-width: 150px">Liên hệ báo phí</span>
              <span v-else>---</span>
            </span>
          </div>
          
          <div v-if="outOfRange" class="alert alert-warning small py-2 mt-2 border-0 bg-warning bg-opacity-10 text-warning-emphasis">
            <PhWarningCircle weight="fill" /> Khu vực ngoại thành (>20km). Vui lòng liên hệ Zalo Shop để được báo giá ship. Bạn vẫn có thể đặt hàng.
          </div>
          <div v-if="distance > 0 && !outOfRange" class="small text-sub text-end mb-2">Khoảng cách: ~{{ distance.toFixed(1) }} km</div>

          <div v-if="discount > 0" class="d-flex justify-content-between mb-2 text-success"><span>Giảm giá</span><span>-{{ formatPrice(discount) }}</span></div>
          <hr />
          <div class="d-flex justify-content-between mb-3"><span class="fw-bold fs-5">Tổng</span><span class="fw-bold fs-5 text-bakery">{{ formatPrice(total) }}</span></div>
          
          <button class="btn btn-bakery w-100 py-2 fw-bold" @click="goToConfirm" :disabled="loadingShipping">
            Tiếp tục
          </button>
        </div>
      </div>
    </div>

    <!-- BƯỚC 2: XÁC NHẬN ĐƠN HÀNG (CONFIRM) -->
    <div v-else-if="step === 'confirm'" class="row justify-content-center">
      <div class="col-lg-8">
        <div class="bakery-card">
          <h4 class="fw-bold text-center mb-4">Xác nhận đơn hàng</h4>
          
          <div class="bg-light p-3 rounded-3 mb-4">
            <h6 class="fw-bold mb-3">Thông tin nhận hàng</h6>
            <div class="row g-2 small">
              <div class="col-4 text-sub">Người nhận:</div><div class="col-8 fw-semibold">{{ form.recipientName }} - {{ form.recipientPhone }}</div>
              <div class="col-4 text-sub">Địa chỉ:</div>
              <div class="col-8">
                {{ form.address }}<br/>
                <span v-if="form.building">{{ form.building }}<br/></span>
                {{ form.ward }}, {{ form.district }}, {{ form.city }}
              </div>
              <div class="col-4 text-sub">Thời gian giao:</div><div class="col-8">{{ form.deliveryTime }}</div>
              <div class="col-4 text-sub" v-if="form.note">Ghi chú:</div><div class="col-8" v-if="form.note">{{ form.note }}</div>
            </div>
          </div>

          <div class="mb-4">
            <h6 class="fw-bold mb-3">Sản phẩm</h6>
            <div v-for="item in cartStore.items" :key="item.variantId" class="d-flex justify-content-between mb-2 pb-2 border-bottom border-light">
              <div class="d-flex gap-3 align-items-center">
                <img :src="item.imageUrl || '/images/cake.png'" style="width:50px; height:50px; object-fit:cover; border-radius: 8px" />
                <div>
                  <div class="fw-semibold">{{ item.productName }}</div>
                  <div class="small text-sub">{{ item.variantName }}</div>
                </div>
              </div>
              <div class="text-end">
                <div class="small text-sub">x{{ item.quantity }}</div>
                <div class="fw-bold">{{ formatPrice(item.price * item.quantity) }}</div>
              </div>
            </div>
          </div>

          <div class="bg-light p-3 rounded-3 mb-4">
            <div class="d-flex justify-content-between mb-2"><span class="text-sub">Tạm tính</span><span>{{ formatPrice(cartStore.totalPrice) }}</span></div>
            <div class="d-flex justify-content-between mb-2"><span class="text-sub">Phí ship ({{ distance > 0 ? distance.toFixed(1) + 'km' : '' }})</span><span>{{ cartStore.totalPrice >= 200000 ? '0 ₫' : (outOfRange ? 'Chờ xác nhận' : formatPrice(shippingFee)) }}</span></div>
            <div class="d-flex justify-content-between mb-2"><span class="text-sub">Thanh toán</span><span class="fw-semibold">{{ form.paymentMethod === 'COD' ? 'Tiền mặt (COD)' : 'Chuyển khoản (VNPay)' }}</span></div>
            <hr class="my-2"/>
            <div class="d-flex justify-content-between align-items-center"><span class="fw-bold fs-5">Tổng cộng</span><span class="fw-bold fs-4 text-bakery">{{ formatPrice(total) }}</span></div>
          </div>

          <div class="d-flex gap-3">
            <button class="btn btn-bakery-outline flex-grow-1" @click="step = 'checkout'" :disabled="submitting">Chỉnh sửa</button>
            <button class="btn btn-bakery flex-grow-1 fw-bold" @click="submitOrder" :disabled="submitting">
              {{ submitting ? 'Đang xử lý...' : 'Xác nhận & Đặt hàng' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- BƯỚC 3a: VNPay QR CODE -->
    <div v-else-if="step === 'vnpay'" class="row justify-content-center">
      <div class="col-lg-6">
        <div class="bakery-card text-center">
          <h4 class="fw-bold mb-2">Thanh toán chuyển khoản VNPay</h4>
          <p class="text-sub mb-4">Vui lòng quét mã QR dưới đây bằng ứng dụng ngân hàng của bạn.</p>

          <div class="qr-container mb-4 mx-auto p-3 border rounded-4 bg-light d-inline-block">
            <img :src="qrUrl" alt="QR Code" class="img-fluid" style="width: 250px; height: 250px; object-fit: contain;" />
          </div>

          <div class="bg-light p-3 rounded-3 mb-4 text-start">
            <div class="row g-2 small">
              <div class="col-5 text-sub">Ngân hàng:</div><div class="col-7 fw-bold">Vietcombank</div>
              <div class="col-5 text-sub">Chủ tài khoản:</div><div class="col-7 fw-bold">BREVERY SHOP</div>
              <div class="col-5 text-sub">Số tài khoản:</div><div class="col-7 fw-bold">0909123456</div>
              <div class="col-5 text-sub">Số tiền:</div><div class="col-7 fw-bold text-bakery fs-6">{{ formatPrice(total) }}</div>
              <div class="col-5 text-sub">Nội dung CK:</div><div class="col-7 fw-bold">{{ pendingOrderCode }}</div>
            </div>
          </div>

          <div class="mb-4">
            <p class="mb-1 text-sub">Thời gian còn lại để thanh toán:</p>
            <h2 class="fw-bold font-monospace" :class="{'text-danger': timer <= 60, 'text-primary': timer > 60}">
              {{ formatTimer(timer) }}
            </h2>
            <div class="small text-sub mt-2"><PhCircleNotch class="spinner" /> Đang chờ thanh toán...</div>
          </div>

          <div class="d-flex flex-column gap-2">
            <button class="btn btn-bakery fw-bold w-100" @click="mockVerifyPayment">Tôi đã chuyển khoản</button>
            <button class="btn btn-outline-secondary w-100" @click="cancelPayment">Huỷ đơn hàng</button>
          </div>
        </div>
      </div>
    </div>

    <!-- BƯỚC 3b: SUCCESS COD / FINISHED -->
    <div v-else-if="step === 'success'" class="row justify-content-center">
      <div class="col-lg-6">
        <div class="bakery-card text-center py-5">
          <PhCheckCircle weight="fill" color="#2ecc71" size="80" class="mb-3" />
          <h3 class="fw-bold mb-2">Đặt hàng thành công!</h3>
          <p class="text-sub mb-4">Cảm ơn bạn đã mua sắm tại Brevery. Mã đơn hàng của bạn là <span class="fw-bold text-dark">#{{ pendingOrderCode }}</span></p>
          
          <div class="bg-light p-3 rounded-3 mb-4 text-start">
            <div class="d-flex justify-content-between mb-2"><span class="text-sub">Tổng tiền:</span><span class="fw-bold">{{ formatPrice(total) }}</span></div>
            <div class="d-flex justify-content-between"><span class="text-sub">Phương thức:</span><span class="fw-semibold">{{ form.paymentMethod === 'COD' ? 'Thanh toán khi nhận hàng' : 'Đã thanh toán (VNPay)' }}</span></div>
          </div>

          <p class="small text-sub mb-4">Chúng tôi sẽ liên hệ xác nhận và giao hàng trong thời gian sớm nhất.</p>

          <div class="d-flex gap-3 justify-content-center">
            <router-link to="/" class="btn btn-bakery-outline">Về trang chủ</router-link>
            <router-link to="/orders" class="btn btn-bakery">Xem đơn hàng</router-link>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart.store'
import { orderApi } from '@/api/order.api'
import { toast } from 'vue3-toastify'
import { watchDebounced } from '@vueuse/core'
import { PhArrowLeft, PhCheckCircle, PhWarningCircle, PhCircleNotch } from '@phosphor-icons/vue'

const cartStore = useCartStore()
const router = useRouter()
const step = ref('checkout') // checkout -> confirm -> success | vnpay
const submitting = ref(false)
const discount = ref(0)

const districts = ref([])
const wards = ref([])
const shippingFee = ref(30000)
const loadingShipping = ref(false)
const distance = ref(0)
const outOfRange = ref(false)
const pendingOrderCode = ref('')
const qrUrl = ref('')

// Countdown Timer
const timer = ref(240)
let timerInterval = null

const form = reactive({
  recipientName: '', recipientPhone: '', address: '', building: '', ward: '', district: '', city: 'TP. Hồ Chí Minh',
  paymentMethod: 'COD', couponCode: '', note: '', deliveryTime: 'Càng sớm càng tốt'
})
const errors = reactive({ recipientName: false, recipientPhone: false, address: false, district: false, ward: false })

const total = computed(() => {
  let ship = cartStore.totalPrice >= 200000 ? 0 : (outOfRange.value ? 0 : shippingFee.value)
  return cartStore.totalPrice + ship - discount.value
})

function formatPrice(p) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) }
function formatTimer(s) {
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${sec.toString().padStart(2, '0')}`
}

function goBack() {
  if (step.value === 'confirm') step.value = 'checkout'
  else router.back()
}

// Fetch Provinces
async function fetchDistricts() {
  try {
    const res = await fetch('https://provinces.open-api.vn/api/p/79?depth=3')
    const data = await res.json()
    districts.value = data.districts || []
  } catch (e) { console.error('Fetch provinces error', e) }
}

function onDistrictChange() {
  form.ward = ''
  const found = districts.value.find(d => d.name === form.district)
  wards.value = found ? found.wards : []
}

// Haversine
function haversineDistance(lat1, lon1, lat2, lon2) {
  const R = 6371; // km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}

watchDebounced([() => form.district, () => form.ward, () => form.address], async ([d, w, a]) => {
  if (d && w) {
    calculateShipping(d, w, a)
  }
}, { debounce: 1000 })

async function calculateShipping(dist, ward, addr) {
  loadingShipping.value = true
  outOfRange.value = false
  distance.value = 0
  
  try {
    const queryStr = `${addr ? addr + ', ' : ''}${ward}, ${dist}, TP. Hồ Chí Minh`
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(queryStr)}`
    const res = await fetch(url)
    const data = await res.json()
    
    if (data && data.length > 0) {
      const targetLat = parseFloat(data[0].lat)
      const targetLon = parseFloat(data[0].lon)
      const shopLat = 10.7735 // 123 Nguyễn Huệ
      const shopLon = 106.7028
      
      const distKm = haversineDistance(shopLat, shopLon, targetLat, targetLon)
      distance.value = distKm
      
      if (distKm <= 3) shippingFee.value = 15000
      else if (distKm <= 7) shippingFee.value = 25000
      else if (distKm <= 15) shippingFee.value = 35000
      else if (distKm <= 20) shippingFee.value = 50000
      else {
        shippingFee.value = 0
        outOfRange.value = true
      }
    } else {
      // Fallback
      shippingFee.value = 30000 
    }
  } catch (e) {
    shippingFee.value = 30000
  } finally {
    loadingShipping.value = false
  }
}

function validateForm() {
  errors.recipientName = !form.recipientName.trim()
  errors.recipientPhone = !form.recipientPhone.trim()
  errors.address = !form.address.trim()
  errors.district = !form.district
  errors.ward = !form.ward
  return !Object.values(errors).some(v => v)
}

function goToConfirm() {
  if (!validateForm()) {
    toast.error('Vui lòng điền đầy đủ thông tin giao hàng')
    return
  }
  step.value = 'confirm'
}

async function submitOrder() {
  submitting.value = true
  try {
    const orderPayload = { ...form }
    if (form.building) orderPayload.address = `${form.building}, ${form.address}`
    
    const { data } = await orderApi.checkout(orderPayload)
    pendingOrderCode.value = data.data?.orderCode || `BRV-${Math.floor(Math.random()*1000000)}`
    
    if (form.paymentMethod === 'VNPAY') {
      // Generate QR
      qrUrl.value = `https://img.vietqr.io/image/970436-0909123456-compact2.png?amount=${total.value}&addInfo=${pendingOrderCode.value}&accountName=BREVERY%20SHOP`
      step.value = 'vnpay'
      startTimer()
    } else {
      step.value = 'success'
      await cartStore.clearCart()
    }
  } catch (err) {
    toast.error(err.response?.data?.message || 'Đặt hàng thất bại')
  } finally { submitting.value = false }
}

function startTimer() {
  timer.value = 240
  if (timerInterval) clearInterval(timerInterval)
  timerInterval = setInterval(() => {
    if (timer.value > 0) timer.value--
    else {
      clearInterval(timerInterval)
      toast.error('Đơn hàng đã bị huỷ do quá thời gian thanh toán')
      step.value = 'checkout'
    }
  }, 1000)
}

async function mockVerifyPayment() {
  toast.info('Đang xác minh thanh toán...')
  setTimeout(async () => {
    clearInterval(timerInterval)
    step.value = 'success'
    await cartStore.clearCart()
    toast.success('Thanh toán thành công!')
  }, 1500)
}

function cancelPayment() {
  clearInterval(timerInterval)
  toast.info('Đã huỷ thanh toán')
  step.value = 'checkout'
}

onMounted(() => {
  fetchDistricts()
  if (cartStore.items.length === 0) {
    router.replace('/cart')
  }
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})
</script>

<style scoped>
.pm-option {
  border: 2px solid transparent;
  border-radius: var(--radius-card);
  padding: 16px;
  cursor: pointer;
  background: white;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
  transition: all 0.2s ease;
}
.pm-option:hover {
  box-shadow: 0 4px 15px rgba(212,135,90,0.1);
}
.pm-option.active {
  border-color: var(--bakery-primary);
  background: var(--bakery-primary-light);
}

.spinner {
  animation: spin 1s linear infinite;
  display: inline-block;
}
@keyframes spin { 100% { transform: rotate(360deg); } }
</style>
