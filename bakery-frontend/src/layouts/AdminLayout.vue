<template>
  <div class="admin-layout d-flex">
    <!-- SIDEBAR -->
    <aside :class="['admin-sidebar', { expanded: sidebarExpanded }]" @mouseenter="sidebarExpanded = true" @mouseleave="sidebarExpanded = false">
      <div class="text-center mb-4 px-2 pt-2">
        <PhCake weight="duotone" size="36" color="#fff" />
        <transition name="fade">
          <span v-if="sidebarExpanded" class="text-white fw-bold ms-2" style="font-size:1.1rem">Admin</span>
        </transition>
      </div>

      <nav class="flex-grow-1">
        <router-link v-for="item in menuItems" :key="item.to" :to="item.to"
          :class="['sidebar-item', { active: $route.path === item.to }]">
          <span class="icon d-flex align-items-center justify-content-center"><component :is="item.icon" size="24" weight="duotone" /></span>
          <transition name="fade">
            <span v-if="sidebarExpanded">{{ item.label }}</span>
          </transition>
        </router-link>
      </nav>

      <div class="mt-auto">
        <router-link to="/" class="sidebar-item">
          <span class="icon d-flex align-items-center justify-content-center"><PhHouse size="24" weight="duotone" /></span>
          <transition name="fade"><span v-if="sidebarExpanded">Về trang chủ</span></transition>
        </router-link>
        <a class="sidebar-item" @click="handleLogout" style="cursor:pointer">
          <span class="icon d-flex align-items-center justify-content-center"><PhSignOut size="24" weight="duotone" /></span>
          <transition name="fade"><span v-if="sidebarExpanded">Đăng xuất</span></transition>
        </a>
      </div>
    </aside>

    <!-- MAIN -->
    <div class="admin-main" :style="{ marginLeft: sidebarExpanded ? '240px' : '72px' }">
      <!-- TOPBAR -->
      <header class="admin-topbar d-flex align-items-center justify-content-between px-4 py-3">
        <h4 class="mb-0 fw-bold" style="color: var(--text-main)">{{ $route.meta.title || 'Admin Dashboard' }}</h4>
        <div class="d-flex align-items-center gap-4">
          <!-- Notification Bell -->
          <div class="dropdown">
            <button class="header-action position-relative dropdown-toggle d-flex align-items-center" data-bs-toggle="dropdown" id="admin-notification-btn" style="background:var(--bg-surface); border:none; padding:10px; border-radius:var(--radius-btn); box-shadow:var(--shadow-soft);">
              <PhBell size="22" weight="duotone" color="var(--primary)" />
              <span v-if="notifStore.unreadCount > 0" class="cart-badge">{{ notifStore.unreadCount }}</span>
            </button>
            <div class="dropdown-menu dropdown-menu-end bakery-dropdown" style="width:360px; max-height:400px; overflow-y:auto; border-radius:var(--radius-card); border:none; box-shadow:var(--shadow-hover);">
              <div class="d-flex justify-content-between px-3 py-2">
                <span class="fw-bold">Thông báo</span>
                <a class="text-bakery small" style="cursor:pointer" @click="notifStore.markAllRead()">Đã đọc tất cả</a>
              </div>
              <hr class="my-0"/>
              <div v-if="notifStore.notifications.length === 0" class="text-center text-muted py-4">
                Chưa có thông báo
              </div>
              <div v-for="n in notifStore.notifications" :key="n.id"
                :class="['px-3 py-2 border-bottom', { 'bg-bakery-light': !n.read }]"
                style="font-size:.9rem; cursor:pointer">
                <div class="fw-semibold d-flex align-items-center gap-2"><PhShoppingCart size="18" weight="bold" color="var(--primary)" /> Đơn hàng mới #{{ n.orderId }}</div>
                <div class="text-sub small">{{ n.customerName }} — {{ n.time }}</div>
              </div>
            </div>
          </div>
          <!-- User Profile -->
          <div class="d-flex align-items-center gap-2" style="background:var(--bg-surface); padding:6px 16px 6px 6px; border-radius:var(--radius-btn); box-shadow:var(--shadow-soft);">
            <div style="width:36px; height:36px; border-radius:50%; background:var(--primary-light); display:flex; align-items:center; justify-content:center;">
              <PhUser size="20" weight="bold" color="var(--primary)" />
            </div>
            <span class="fw-semibold" style="color:var(--text-main)">{{ authStore.userName }}</span>
          </div>
        </div>
      </header>

      <!-- CONTENT -->
      <div class="admin-content" style="padding: 0 32px 32px 32px;">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useNotificationStore } from '@/stores/notification.store'
import { Client } from '@stomp/stompjs'
import { PhChartBar, PhCake, PhPackage, PhUsers, PhHouse, PhSignOut, PhBell, PhUser, PhShoppingCart } from '@phosphor-icons/vue'

const authStore = useAuthStore()
const notifStore = useNotificationStore()
const router = useRouter()
const sidebarExpanded = ref(false)

const menuItems = [
  { to: '/admin', icon: PhChartBar, label: 'Dashboard' },
  { to: '/admin/products', icon: PhCake, label: 'Sản phẩm' },
  { to: '/admin/orders', icon: PhPackage, label: 'Đơn hàng' },
  { to: '/admin/users', icon: PhUsers, label: 'Người dùng' },
]

let stompClient = null

onMounted(() => {
  // WebSocket connection for real-time order notifications
  try {
    stompClient = new Client({
      brokerURL: `ws://${window.location.host}/ws/websocket`,
      reconnectDelay: 5000,
      onConnect: () => {
        stompClient.subscribe('/topic/admin/orders', (message) => {
          const order = JSON.parse(message.body)
          notifStore.addNotification({
            orderId: order.orderId,
            customerName: order.customerName || order.recipientName || 'Khách',
            totalAmount: order.totalAmount,
          })
        })
      },
    })
    stompClient.activate()
  } catch { /* WebSocket not available */ }
})

onUnmounted(() => {
  if (stompClient) stompClient.deactivate()
})

async function handleLogout() {
  await authStore.logout()
  router.push({ name: 'login' })
}
</script>

<style scoped>
.admin-main {
  flex: 1;
  transition: margin-left .3s ease;
  min-height: 100vh;
  background: var(--bg-app);
}
.admin-topbar {
  background: rgba(250, 242, 233, 0.9);
  backdrop-filter: blur(8px);
  position: sticky; top: 0; z-index: 800;
  border-bottom: 1px solid rgba(0,0,0,0.02);
}
.admin-sidebar {
  background: var(--primary);
  border-radius: 0 24px 24px 0;
  width: 72px;
  transition: width .3s ease;
  min-height: 100vh;
  position: fixed;
  left: 0; top: 0;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  padding: 24px 0;
}
.admin-sidebar.expanded { width: 240px; }
.sidebar-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px;
  color: rgba(255,255,255,.7);
  cursor: pointer;
  transition: all .2s ease;
  border-radius: var(--radius-btn); /* Pill-shaped */
  margin: 4px 12px;
  white-space: nowrap;
  overflow: hidden;
}
.sidebar-item:hover { color: #fff; background: rgba(255,255,255,.12); }
.sidebar-item.active {
  background: var(--bg-surface);
  color: var(--primary);
  font-weight: 600;
  box-shadow: 4px 0 16px rgba(0,0,0,0.1);
}
.sidebar-item .icon { min-width: 24px; display: flex; align-items: center; justify-content: center; }
</style>
