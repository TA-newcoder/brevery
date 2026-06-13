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
          :class="['sidebar-item', { active: isActiveRoute(item.to) }]">
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
      <header class="admin-topbar">
        <div class="d-flex align-items-center justify-content-between px-4 py-3">
          <div class="d-flex align-items-center gap-3">
            <h4 class="mb-0 fw-bold" style="color: var(--text-main)">{{ $route.meta.title || 'Admin Dashboard' }}</h4>
          </div>

          <div class="d-flex align-items-center gap-3">
            <!-- Dark Mode Toggle -->
            <button class="topbar-action" @click="toggleDarkMode" :title="isDarkMode ? 'Chế độ sáng' : 'Chế độ tối'" id="dark-mode-toggle">
              <PhSun v-if="isDarkMode" size="20" weight="duotone" />
              <PhMoon v-else size="20" weight="duotone" />
            </button>

            <!-- Notification Bell -->
            <div class="dropdown">
              <button class="topbar-action position-relative dropdown-toggle d-flex align-items-center" data-bs-toggle="dropdown" id="admin-notification-btn">
                <PhBell size="20" weight="duotone" />
                <span v-if="notifStore.unreadCount > 0" class="notif-badge">{{ notifStore.unreadCount }}</span>
              </button>
              <div class="dropdown-menu dropdown-menu-end notif-dropdown" style="width:380px; max-height:420px; overflow-y:auto;">
                <div class="d-flex justify-content-between align-items-center px-3 py-2">
                  <span class="fw-bold" style="color: var(--text-main)">Thông báo</span>
                  <a class="small fw-semibold" style="cursor:pointer; color: var(--primary)" @click="notifStore.markAllRead()">Đã đọc tất cả</a>
                </div>
                <hr class="my-0" style="border-color: var(--border-light)"/>
                <div v-if="notifStore.notifications.length === 0" class="empty-state py-4">
                  <div class="empty-state-icon mx-auto">
                    <PhBellSlash size="32" color="var(--text-muted)" />
                  </div>
                  <p class="empty-state-text mb-0">Chưa có thông báo mới</p>
                </div>
                <div v-for="n in notifStore.notifications" :key="n.id"
                  :class="['notif-item px-3 py-3', { 'unread': !n.read }]"
                  style="cursor:pointer">
                  <div class="d-flex align-items-start gap-3">
                    <div class="notif-icon-wrap flex-shrink-0">
                      <PhShoppingCart size="18" weight="bold" color="var(--primary)" />
                    </div>
                    <div class="flex-grow-1 min-w-0">
                      <div class="fw-semibold d-flex align-items-center gap-1" style="color: var(--text-main); font-size: 0.9rem;">
                        Đơn hàng mới #{{ n.orderId }}
                      </div>
                      <div class="small mt-1" style="color: var(--text-sub)">{{ n.customerName }} — {{ n.time }}</div>
                    </div>
                    <span v-if="!n.read" class="notif-dot flex-shrink-0"></span>
                  </div>
                </div>
              </div>
            </div>

            <!-- User Profile -->
            <div class="topbar-user d-flex align-items-center gap-2">
              <div class="topbar-avatar">
                <PhUser size="18" weight="bold" color="var(--primary)" />
              </div>
              <span class="fw-semibold d-none d-lg-inline" style="color:var(--text-main); font-size: 0.9rem">{{ authStore.userName }}</span>
            </div>
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
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useNotificationStore } from '@/stores/notification.store'
import { Client } from '@stomp/stompjs'
import {
  PhChartBar, PhCake, PhPackage, PhUsers, PhHouse, PhSignOut,
  PhBell, PhBellSlash, PhUser, PhShoppingCart, PhMagnifyingGlass,
  PhMoon, PhSun, PhGear, PhChartLine, PhClipboardText, PhStar,
  PhTag, PhTicket
} from '@phosphor-icons/vue'

const authStore = useAuthStore()
const notifStore = useNotificationStore()
const router = useRouter()
const route = useRoute()
const sidebarExpanded = ref(false)
const searchQuery = ref('')
const isDarkMode = ref(false)

const menuItems = [
  { to: '/admin', icon: PhChartBar, label: 'Dashboard' },
  { to: '/admin/products', icon: PhCake, label: 'Sản phẩm' },
  { to: '/admin/categories', icon: PhTag, label: 'Danh mục' },
  { to: '/admin/orders', icon: PhPackage, label: 'Đơn hàng' },
  { to: '/admin/users', icon: PhUsers, label: 'Người dùng' },
  { to: '/admin/coupons', icon: PhTicket, label: 'Khuyến mãi' },
  { to: '/admin/banners', icon: PhStar, label: 'Banner QC' },
  { to: '/admin/reports', icon: PhChartLine, label: 'Báo cáo' },
  { to: '/admin/reviews', icon: PhStar, label: 'Bình luận' },
  { to: '/admin/inventory', icon: PhClipboardText, label: 'Kho hàng' },
  { to: '/admin/settings', icon: PhGear, label: 'Cài đặt' },
]

function isActiveRoute(to) {
  if (to === '/admin') return route.path === '/admin'
  return route.path.startsWith(to)
}


function toggleDarkMode() {
  isDarkMode.value = !isDarkMode.value
  document.documentElement.setAttribute('data-theme', isDarkMode.value ? 'dark' : 'light')
  localStorage.setItem('brevery-theme', isDarkMode.value ? 'dark' : 'light')
}

let stompClient = null

onMounted(() => {
  // Restore theme preference
  const saved = localStorage.getItem('brevery-theme')
  if (saved === 'dark') {
    isDarkMode.value = true
    document.documentElement.setAttribute('data-theme', 'dark')
  }

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
  min-height: 100vh;
  background: var(--bg-app);
}

/* TOPBAR */
.admin-topbar {
  background: var(--bg-surface);
  backdrop-filter: blur(12px);
  position: sticky; top: 0; z-index: 800;
  border-bottom: 1px solid var(--border-light);
  transition: background-color var(--duration-slow) var(--ease-out),
              border-color var(--duration-slow) var(--ease-out);
}

/* Topbar Search */
.topbar-search { width: 280px; }
.topbar-search-input {
  width: 100%;
  padding: 8px 12px 8px 36px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--bg-muted);
  color: var(--text-main);
  font-size: 0.88rem;
  transition: all var(--duration-fast) var(--ease-out);
}
.topbar-search-input:focus {
  outline: none;
  border-color: var(--primary);
  background: var(--bg-surface);
  box-shadow: 0 0 0 3px rgba(200, 90, 50, 0.1);
}
.topbar-search-input::placeholder { color: var(--text-muted); }
.search-icon-topbar {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
  pointer-events: none;
}

/* Topbar Action buttons */
.topbar-action {
  width: 38px;
  height: 38px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  background: var(--bg-surface);
  color: var(--text-sub);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  position: relative;
}
.topbar-action:hover {
  background: var(--bg-muted);
  color: var(--primary);
  border-color: var(--primary);
}

/* Notification badge */
.notif-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background-color: var(--color-danger);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  line-height: 18px;
  text-align: center;
  border: 2px solid var(--bg-surface);
  animation: notifPulse 2s ease-in-out infinite;
}
@keyframes notifPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

/* Notification dropdown */
.notif-dropdown {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-card) !important;
  box-shadow: var(--shadow-hover);
  background: var(--bg-surface);
  padding: 0;
}
.notif-item {
  border-bottom: 1px solid var(--border-light);
  transition: background var(--duration-fast) var(--ease-out);
}
.notif-item:last-child { border-bottom: none; }
.notif-item:hover { background: var(--bg-muted); }
.notif-item.unread { background: var(--primary-soft); }
.notif-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
}
.notif-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
  margin-top: 6px;
}

/* User profile in topbar */
.topbar-user {
  padding: 4px 14px 4px 4px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  background: var(--bg-surface);
  transition: all var(--duration-fast) var(--ease-out);
}
.topbar-user:hover { background: var(--bg-muted); }
.topbar-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* SIDEBAR */
.admin-sidebar {
  background: var(--primary);
  border-radius: 0 24px 24px 0;
  width: 72px;
  min-height: 100vh;
  position: fixed;
  left: 0; top: 0;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  padding: 24px 0;
}
[data-theme="dark"] .admin-sidebar {
  background: #2A1E18;
  border-right: 1px solid var(--border-color);
}
.admin-sidebar.expanded { width: 240px; }
.sidebar-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px;
  color: rgba(255,255,255,.7);
  cursor: pointer;
  transition: all .2s var(--ease-out);
  border-radius: var(--radius-sm);
  margin: 3px 12px;
  white-space: nowrap;
  overflow: hidden;
}
.sidebar-item:hover { color: #fff; background: rgba(255,255,255,.12); }
.sidebar-item.active {
  background: var(--bg-surface);
  color: var(--primary);
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.sidebar-item .icon { min-width: 24px; display: flex; align-items: center; justify-content: center; }

/* Responsive */
@media (max-width: 768px) {
  .admin-sidebar {
    width: 0;
    border-radius: 0;
    overflow: hidden;
  }
  .admin-sidebar.expanded {
    width: 240px;
  }
  .admin-main {
    margin-left: 0 !important;
  }
  .topbar-search { width: 200px; }
}
</style>
