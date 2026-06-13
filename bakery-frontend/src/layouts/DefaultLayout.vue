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
          <!-- Dark Mode Toggle -->
          <button class="header-action d-flex align-items-center justify-content-center" @click="toggleDarkMode" :title="isDarkMode ? 'Chế độ sáng' : 'Chế độ tối'" id="user-dark-mode-toggle">
            <PhSun v-if="isDarkMode" size="18" weight="bold" color="var(--bakery-primary)" />
            <PhMoon v-else size="18" weight="bold" color="var(--bakery-primary)" />
          </button>

          <!-- Cart -->
          <a href="#" @click.prevent="openCart" class="header-action d-flex align-items-center justify-content-center" id="header-cart-btn">
            <PhShoppingCart size="20" weight="bold" color="var(--bakery-primary)" />
            <span v-if="cartStore.totalItems > 0" class="cart-badge">{{ cartStore.totalItems }}</span>
          </a>

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
        <transition name="page-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- CART DRAWER FOR MOBILE -->
    <div class="cart-drawer-overlay" :class="{ 'open': cartDrawerOpen }" @click.self="cartDrawerOpen = false">
      <div class="cart-drawer" :class="{ 'open': cartDrawerOpen }">
        <div class="cart-drawer-header d-flex justify-content-between align-items-center p-3 border-bottom">
          <h5 class="fw-bold mb-0" style="color: var(--text-main)">🛒 Giỏ hàng</h5>
          <button class="btn btn-sm text-dark fs-4 p-0 border-0 bg-transparent" @click="cartDrawerOpen = false">✕</button>
        </div>
        
        <div class="cart-drawer-content p-3 flex-grow-1 overflow-y-auto">
          <div v-if="cartStore.items.length === 0" class="text-center py-5">
            <span style="font-size: 3rem">🛒</span>
            <p class="text-sub mt-2">Giỏ hàng trống</p>
            <button class="btn btn-bakery btn-sm mt-2" @click="cartDrawerOpen = false; $router.push('/products')">Mua sắm ngay</button>
          </div>
          <div v-else>
            <div v-for="item in cartStore.items" :key="item.cartItemId || item.variantId" class="d-flex gap-3 align-items-center mb-3 pb-3 border-bottom">
              <img :src="item.imageUrl || item.primaryImageUrl || '/images/cake.png'" style="width:60px; height:60px; object-fit:cover; border-radius: 10px" />
              <div class="flex-grow-1 min-w-0">
                <h6 class="fw-bold mb-0 text-truncate text-main" style="font-size: 0.9rem; color: var(--text-main)">{{ item.productName }}</h6>
                <span class="text-sub small d-block mb-1">{{ item.variantName || 'Mặc định' }}</span>
                <div class="d-flex align-items-center gap-2 mt-1">
                  <button class="btn btn-light btn-xs p-1" style="line-height:1" @click="cartStore.updateQuantity(item.cartItemId || item.variantId, Math.max(1, item.quantity - 1))">−</button>
                  <span class="fw-bold small" style="color: var(--text-main)">{{ item.quantity }}</span>
                  <button class="btn btn-light btn-xs p-1" style="line-height:1" @click="cartStore.updateQuantity(item.cartItemId || item.variantId, item.quantity + 1)">+</button>
                </div>
              </div>
              <div class="text-end flex-shrink-0">
                <div class="fw-bold text-bakery small">{{ formatPrice(item.price * item.quantity) }}</div>
                <button class="btn btn-link text-danger p-0 mt-1 small border-0 bg-transparent text-decoration-none" @click="cartStore.removeItem(item.cartItemId || item.variantId)">Xóa</button>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="cartStore.items.length > 0" class="cart-drawer-footer p-3 border-top bg-light">
          <div class="d-flex justify-content-between mb-2">
            <span class="text-sub">Tạm tính</span>
            <span class="fw-bold text-bakery">{{ formatPrice(cartStore.totalPrice) }}</span>
          </div>
          <button class="btn btn-bakery w-100 py-2 mt-2 fw-bold" @click="cartDrawerOpen = false; $router.push('/checkout')">TIẾN HÀNH THANH TOÁN</button>
        </div>
      </div>
    </div>

    <!-- CHAT WIDGET -->
    <ChatWidget v-if="authStore.isAuthenticated" />

    <!-- CONTACT FLOATING BUTTONS -->
    <div class="contact-floating-group d-flex flex-column gap-2" :class="{ 'mobile-expanded': contactExpanded }">
      <a href="tel:0705230644" class="contact-btn hotline shadow-sm" title="Gọi Hotline: 0705 230 644">
        <PhPhone size="20" weight="fill" style="flex-shrink: 0;" />
        <span class="contact-text">0705 230 644</span>
      </a>
      <a href="https://zalo.me/0705230644" target="_blank" class="contact-btn zalo shadow-sm" title="Chat Zalo">
        <img src="/images/zalo.svg" alt="Zalo" style="width: 22px; height: 22px; flex-shrink: 0;" />
        <span class="contact-text">Chat Zalo</span>
      </a>
      <button class="contact-toggle-btn shadow-sm d-md-none" @click="contactExpanded = !contactExpanded" title="Liên hệ">
        <span v-if="!contactExpanded">📞</span>
        <span v-else>✕</span>
      </button>
    </div>

    <!-- NEWSLETTER SECTION -->
    <div v-if="!authStore.isAuthenticated" class="container mb-5 mt-5">
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
import { PhCake, PhMagnifyingGlass, PhShoppingCart, PhUser, PhPackage, PhGear, PhSignOut, PhMapPin, PhPhone, PhEnvelope, PhX, PhMoon, PhSun } from '@phosphor-icons/vue'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()
const searchQuery = ref('')
const showAnnouncement = ref(true)
const isDarkMode = ref(false)
const contactExpanded = ref(false)
const cartDrawerOpen = ref(false)

function openCart() {
  if (window.innerWidth < 768) {
    cartDrawerOpen.value = true
  } else {
    router.push('/cart')
  }
}

function formatPrice(p) { 
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0) 
}

cartStore.fetchCart()

onMounted(() => {
  const isHidden = localStorage.getItem('hideAnnouncement')
  if (isHidden) {
    showAnnouncement.value = false
  }
  // Restore theme preference
  const saved = localStorage.getItem('brevery-theme')
  if (saved === 'dark') {
    isDarkMode.value = true
    document.documentElement.setAttribute('data-theme', 'dark')
  }
})

function toggleDarkMode() {
  isDarkMode.value = !isDarkMode.value
  document.documentElement.setAttribute('data-theme', isDarkMode.value ? 'dark' : 'light')
  localStorage.setItem('brevery-theme', isDarkMode.value ? 'dark' : 'light')
}

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
  background-color: var(--primary);
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
  background: var(--bg-surface);
  box-shadow: 0 2px 12px rgba(44, 30, 22, 0.06);
  position: sticky;
  top: 0;
  z-index: 900;
  transition: background-color var(--duration-slow) var(--ease-smooth),
              box-shadow var(--duration-slow) var(--ease-smooth);
}

/* PAGE TRANSITION */
.page-slide-enter-active,
.page-slide-leave-active {
  transition: opacity 0.3s var(--ease-smooth), transform 0.3s var(--ease-smooth);
}
.page-slide-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.page-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* BRANDING LOGO */
.logo-text-gradient {
  font-family: 'Playfair Display', serif;
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #2D1B0E 0%, var(--primary) 50%, #C8502A 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: transparent;
  line-height: 1.4;
  padding-bottom: 4px;
  display: inline-block;
  overflow: visible;
}

[data-theme="dark"] .logo-text-gradient {
  background: linear-gradient(135deg, #F0E8E0 0%, #FFA07A 50%, var(--primary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: transparent;
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
  background: var(--primary-light);
  border: none;
  border-radius: var(--radius-badge);
  padding: 8px 14px;
  color: var(--text-main);
  font-weight: 500;
  font-size: .95rem;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.header-action:hover { background: var(--bg-muted); }
.action-icon { font-size: 1.1rem; }
.user-name { font-size: .9rem; color: var(--text-main); }
.cart-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border-radius: 999px;
  background-color: var(--primary);
  color: #FFFFFF;
  font-size: 11px;
  font-weight: 700;
  line-height: 14px;
  text-align: center;
  border: 2px solid var(--bg-surface);
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
  display: flex; 
  align-items: center; 
  justify-content: center;
  overflow: visible;
}

.bakery-dropdown {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-hover);
  padding: 8px;
  background: var(--bg-surface);
}
.bakery-dropdown .dropdown-item {
  border-radius: var(--radius-sm);
  padding: 10px 16px;
  font-size: .9rem;
  color: var(--text-main);
}
.bakery-dropdown .dropdown-item:hover { background: var(--primary-light); }

.main-content { min-height: 60vh; }

/* NEWSLETTER */
.newsletter-banner {
  background: var(--gradient-primary);
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
  background: var(--text-main);
}
[data-theme="dark"] .bakery-footer {
  background: #0F0C0A;
}
.footer-links li { margin-bottom: 8px; }
.footer-links a, .footer-links li { color: rgba(255,255,255,.5); font-size: .9rem; transition: color .15s; text-decoration: none; }
.footer-links a:hover { color: #fff; }

/* CONTACT FLOATING BUTTONS */
.contact-floating-group {
  position: fixed;
  bottom: 150px;
  right: 24px;
  z-index: 998;
}
.contact-btn {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding-left: 13px;
  width: 48px;
  height: 48px;
  border-radius: 24px;
  color: white;
  text-decoration: none;
  overflow: hidden;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1), background 0.3s ease, transform 0.2s ease;
  white-space: nowrap;
}
.contact-btn:hover {
  width: 160px;
  padding-left: 16px;
  transform: translateY(-2px);
  color: white;
}
.contact-btn .contact-text {
  opacity: 0;
  margin-left: 8px;
  font-weight: 600;
  font-size: 0.9rem;
  transition: opacity 0.3s ease 0.1s;
  pointer-events: none;
}
.contact-btn:hover .contact-text {
  opacity: 1;
  pointer-events: auto;
}
.contact-btn.hotline {
  background: var(--color-danger);
}
.contact-btn.hotline:hover {
  background: #A34852;
}
.contact-btn.zalo {
  background: #0068FF;
}
.contact-btn.zalo:hover {
  background: #0054CC;
}
.contact-toggle-btn {
  display: none;
}
@media (max-width: 768px) {
  .contact-btn {
    opacity: 0;
    transform: scale(0.8) translateY(12px);
    pointer-events: none;
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    margin-bottom: 4px;
  }
  .contact-btn:hover {
    width: 48px;
    padding-left: 13px;
    justify-content: flex-start;
  }
  .contact-btn:hover .contact-text {
    display: none;
  }
  .contact-floating-group.mobile-expanded .contact-btn {
    opacity: 1;
    transform: scale(1) translateY(0);
    pointer-events: auto;
  }
  .contact-toggle-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    border-radius: 24px;
    background: var(--primary);
    color: white;
    border: none;
    cursor: pointer;
    font-size: 1.2rem;
    box-shadow: 0 4px 12px rgba(200, 90, 50, 0.3);
    transition: transform 0.2s ease;
  }
  .contact-toggle-btn:active {
    transform: scale(0.95);
  }
}

/* CART DRAWER */
.cart-drawer-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 2000;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
}
.cart-drawer-overlay.open {
  opacity: 1;
  visibility: visible;
}
.cart-drawer {
  position: fixed;
  top: 0; right: -320px; bottom: 0;
  width: 320px;
  background: var(--bg-surface);
  z-index: 2001;
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 20px rgba(0,0,0,0.15);
  transition: right 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.cart-drawer.open {
  right: 0;
}
.btn-xs {
  padding: 2px 6px;
  font-size: 0.75rem;
  border-radius: 4px;
}
</style>
