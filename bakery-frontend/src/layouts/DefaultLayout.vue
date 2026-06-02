<template>
  <div class="default-layout">
    <!-- ANNOUNCEMENT BAR -->
    <div v-if="showAnnouncement" class="announcement-bar d-flex align-items-center justify-content-center position-relative">
      <span class="fs-sm fw-medium">🎉 Miễn phí giao hàng cho đơn từ 150.000đ — Nội thành HCM</span>
      <button class="btn-close-announcement" @click="closeAnnouncement">
        <PhX size="16" weight="bold" />
      </button>
    </div>

    <!-- HEADER -->
    <header class="bakery-header">
      <div class="container d-flex align-items-center justify-content-between py-2">
        <!-- Logo -->
        <router-link to="/" class="d-flex align-items-center text-decoration-none">
          <span class="logo-text-gradient">Brevery</span>
        </router-link>

        <!-- Search -->
        <div class="header-search d-none d-md-block">
          <div class="position-relative">
            <input
              v-model="searchQuery"
              type="text"
              class="bakery-input search-input"
              placeholder="Tìm kiếm bánh, đồ uống..."
              @keyup.enter="doSearch"
            />
            <span class="search-icon text-sub" @click="doSearch">
              <PhMagnifyingGlass size="20" weight="bold" />
            </span>
          </div>
        </div>

        <!-- Actions -->
        <div class="d-flex align-items-center gap-3">
          <!-- Cart -->
          <router-link to="/cart" class="header-action d-flex align-items-center justify-content-center" id="header-cart-btn">
            <PhShoppingCart size="20" weight="bold" color="var(--bakery-primary)" />
            <span v-if="cartStore.totalItems > 0" class="cart-badge">{{ cartStore.totalItems }}</span>
          </router-link>

          <!-- User Menu -->
          <div v-if="authStore.isAuthenticated" class="dropdown">
            <button class="header-action dropdown-toggle d-flex align-items-center" data-bs-toggle="dropdown" id="user-menu-btn">
              <PhUser size="20" weight="bold" color="var(--bakery-primary)" />
              <span class="d-none d-lg-inline ms-2 user-name">{{ authStore.userName }}</span>
            </button>
            <ul class="dropdown-menu dropdown-menu-end bakery-dropdown">
              <li><router-link to="/orders" class="dropdown-item d-flex align-items-center gap-2"><PhPackage size="18" weight="bold"/> Đơn hàng</router-link></li>
              <li v-if="authStore.isAdmin"><router-link to="/admin" class="dropdown-item d-flex align-items-center gap-2"><PhGear size="18" weight="bold"/> Admin Panel</router-link></li>
              <li><hr class="dropdown-divider" /></li>
              <li><a class="dropdown-item d-flex align-items-center gap-2" @click="handleLogout" style="cursor:pointer"><PhSignOut size="18" weight="bold"/> Đăng xuất</a></li>
            </ul>
          </div>
          <div v-else class="d-flex gap-2">
            <router-link to="/login" class="btn btn-bakery-outline btn-sm">Đăng nhập</router-link>
            <router-link to="/register" class="btn btn-bakery btn-sm d-none d-md-inline-block">Đăng ký</router-link>
          </div>
        </div>
      </div>

      <!-- Mobile Search -->
      <div class="container d-md-none pb-2">
        <input
          v-model="searchQuery"
          type="text"
          class="bakery-input w-100"
          placeholder="Tìm kiếm..."
          @keyup.enter="doSearch"
        />
      </div>
    </header>

    <!-- MAIN -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- CHAT WIDGET -->
    <ChatWidget v-if="authStore.isAuthenticated" />

    <!-- NEWSLETTER SECTION -->
    <div class="container mb-5 mt-5">
      <div class="newsletter-banner p-4 p-md-5 text-center shadow-lg mx-auto" style="max-width: 900px;">
        <div class="d-flex flex-column align-items-center justify-content-center">
          <div class="mb-3">
            <PhEnvelope size="40" weight="duotone" color="#fff" />
          </div>
          <h3 class="text-white fw-bold mb-2">Nhận ưu đãi độc quyền mỗi tuần!</h3>
          <p class="text-white-50 mb-4">Đăng ký để nhận voucher 20k cho đơn đầu tiên 🎁</p>
          
          <form class="newsletter-form d-flex w-100 mb-3" style="max-width: 450px;" @submit.prevent>
            <input type="email" class="form-control border-0 px-4 py-3 shadow-none" placeholder="Nhập email của bạn..." required>
            <button class="btn btn-dark px-4 fw-bold text-nowrap" type="submit">Đăng ký</button>
          </form>
          <small class="text-white-50">🔒 Cam kết không spam · Hủy bất cứ lúc nào</small>
        </div>
      </div>
    </div>

    <!-- FOOTER -->
    <footer class="bakery-footer">
      <div class="container">
        <div class="row g-4 py-5">
          <div class="col-lg-4">
            <div class="d-flex align-items-center gap-2 mb-3">
              <span class="logo-text-footer">Brevery</span>
            </div>
            <p class="text-white-50 mb-0">Bánh ngọt & đồ uống tươi mới mỗi ngày.<br/>Giao hàng tận nơi tại TP.HCM.</p>
          </div>
          <div class="col-6 col-lg-2">
            <h6 class="text-white mb-3">Liên kết</h6>
            <ul class="list-unstyled footer-links">
              <li><router-link to="/">Trang chủ</router-link></li>
              <li><router-link to="/products">Sản phẩm</router-link></li>
              <li><router-link to="/track">Tra cứu đơn</router-link></li>
            </ul>
          </div>
          <div class="col-6 col-lg-3">
            <h6 class="text-white mb-3">Liên hệ</h6>
            <ul class="list-unstyled footer-links">
              <li class="d-flex align-items-center gap-2"><PhMapPin size="18" /> 123 Nguyễn Huệ, Q.1, HCM</li>
              <li class="d-flex align-items-center gap-2"><PhPhone size="18" /> 0909 123 456</li>
              <li class="d-flex align-items-center gap-2"><PhEnvelope size="18" /> hello@brevery.vn</li>
            </ul>
          </div>
          <div class="col-lg-3">
            <h6 class="text-white mb-3">Giờ mở cửa</h6>
            <p class="text-white-50 mb-0">T2 - CN: 7:00 - 22:00</p>
          </div>
        </div>
        <div class="border-top border-secondary pt-3 pb-3 text-center text-white-50" style="font-size:.85rem">
          © 2026 Brevery. Đồ án tốt nghiệp.
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'
import ChatWidget from '@/components/ChatWidget.vue'
import { PhCake, PhMagnifyingGlass, PhShoppingCart, PhUser, PhPackage, PhGear, PhSignOut, PhMapPin, PhPhone, PhEnvelope, PhX } from '@phosphor-icons/vue'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()
const searchQuery = ref('')
const showAnnouncement = ref(true)

cartStore.fetchCart()

onMounted(() => {
  const isHidden = localStorage.getItem('hideAnnouncement')
  if (isHidden) {
    showAnnouncement.value = false
  }
})

function closeAnnouncement() {
  showAnnouncement.value = false
  localStorage.setItem('hideAnnouncement', 'true')
}

function doSearch() {
  if (searchQuery.value.trim()) {
    router.push({ name: 'products', query: { search: searchQuery.value.trim() } })
  }
}

async function handleLogout() {
  await authStore.logout()
  router.push({ name: 'home' })
}
</script>

<style scoped>
/* ANNOUNCEMENT BAR */
.announcement-bar {
  background-color: var(--bakery-primary);
  color: white;
  height: 40px;
  width: 100%;
}
.fs-sm { font-size: 0.85rem; }
.btn-close-announcement {
  position: absolute;
  right: 15px;
  background: transparent;
  border: none;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.2s;
}
.btn-close-announcement:hover { opacity: 1; }

/* HEADER */
.bakery-header {
  background: #fff;
  box-shadow: 0 2px 12px rgba(212,135,90,.06);
  position: sticky;
  top: 0;
  z-index: 900;
}

/* BRANDING LOGO */
.logo-text-gradient {
  font-family: 'Playfair Display', serif;
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: 2px;
  background: linear-gradient(90deg, #C8502A, #8B3210);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.4;
  padding-bottom: 4px;
  display: inline-block;
  overflow: visible;
}

.logo-text-footer {
  font-family: 'Playfair Display', serif;
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: 2px;
  color: #FFFFFF;
  line-height: 1;
}

.header-search { flex-grow: 1; max-width: 520px; margin: 0 2rem; }
@media (max-width: 1024px) {
  .header-search { max-width: 380px; margin: 0 1rem; }
}
.search-input { padding-right: 40px; }
.search-icon {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
  cursor: pointer; font-size: 1rem;
}

.header-action {
  position: relative;
  background: var(--bakery-primary-light);
  border: none;
  border-radius: var(--radius-badge);
  padding: 8px 14px;
  color: var(--bakery-text);
  font-weight: 500;
  font-size: .95rem;
  cursor: pointer;
  transition: all .15s;
}
.header-action:hover { background: var(--bakery-accent-light); }
.action-icon { font-size: 1.1rem; }
.user-name { font-size: .9rem; }
.cart-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border-radius: 999px;
  background-color: #C8502A;
  color: #FFFFFF;
  font-size: 11px;
  font-weight: 700;
  line-height: 14px;
  text-align: center;
  border: 2px solid #FFFFFF;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
  display: flex; 
  align-items: center; 
  justify-content: center;
  overflow: visible;
}

.bakery-dropdown {
  border: none;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-hover);
  padding: 8px;
}
.bakery-dropdown .dropdown-item {
  border-radius: var(--radius-btn);
  padding: 10px 16px;
  font-size: .9rem;
}
.bakery-dropdown .dropdown-item:hover { background: var(--bakery-primary-light); }

.main-content { min-height: 60vh; }

/* NEWSLETTER */
.newsletter-banner {
  background-color: var(--bakery-primary);
  border-radius: 20px;
}
.newsletter-form {
  border-radius: 50px;
  overflow: hidden;
  background: #fff;
}
.newsletter-form .form-control {
  border-radius: 0;
  font-size: 0.95rem;
}
.newsletter-form .btn {
  border-radius: 0;
}

/* FOOTER */
.bakery-footer {
  background: var(--bakery-text);
}
.footer-links li { margin-bottom: 8px; }
.footer-links a, .footer-links li { color: rgba(255,255,255,.5); font-size: .9rem; transition: color .15s; text-decoration: none; }
.footer-links a:hover { color: #fff; }
</style>
