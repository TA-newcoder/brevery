import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const routes = [
  // ===== PUBLIC / USER PAGES =====
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('@/pages/Home.vue'), meta: { title: 'Trang chủ' } },
      { path: 'products', name: 'products', component: () => import('@/pages/Products.vue'), meta: { title: 'Sản phẩm' } },
      { path: 'products/:id', name: 'product-detail', component: () => import('@/pages/ProductDetail.vue'), meta: { title: 'Chi tiết sản phẩm' } },
      { path: 'cart', name: 'cart', component: () => import('@/pages/Cart.vue'), meta: { title: 'Giỏ hàng' } },
      { path: 'checkout', name: 'checkout', component: () => import('@/pages/Checkout.vue'), meta: { title: 'Thanh toán', requiresAuth: true } },
      { path: 'orders', name: 'orders', component: () => import('@/pages/OrderHistory.vue'), meta: { title: 'Đơn hàng', requiresAuth: true } },
      { path: 'track', name: 'track-order', component: () => import('@/pages/OrderTracking.vue'), meta: { title: 'Tra cứu đơn hàng' } },
    ],
  },
  // ===== AUTH PAGES =====
  {
    path: '/login', name: 'login', component: () => import('@/pages/Login.vue'),
    meta: { title: 'Đăng nhập', hideIfAuth: true },
  },
  {
    path: '/register', name: 'register', component: () => import('@/pages/Register.vue'),
    meta: { title: 'Đăng ký', hideIfAuth: true },
  },
  {
    path: '/forgot-password', name: 'forgot-password', component: () => import('@/pages/ForgotPassword.vue'),
    meta: { title: 'Quên mật khẩu', hideIfAuth: true },
  },
  // ===== ADMIN PAGES =====
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'admin-dashboard', component: () => import('@/pages/admin/Dashboard.vue'), meta: { title: 'Dashboard' } },
      { path: 'products', name: 'admin-products', component: () => import('@/pages/admin/ProductList.vue'), meta: { title: 'Quản lý sản phẩm' } },
      { path: 'products/new', name: 'admin-product-new', component: () => import('@/pages/admin/ProductForm.vue'), meta: { title: 'Thêm sản phẩm' } },
      { path: 'products/:id/edit', name: 'admin-product-edit', component: () => import('@/pages/admin/ProductForm.vue'), meta: { title: 'Sửa sản phẩm' } },
      { path: 'orders', name: 'admin-orders', component: () => import('@/pages/admin/OrderList.vue'), meta: { title: 'Quản lý đơn hàng' } },
      { path: 'reports', name: 'admin-reports', component: () => import('@/pages/admin/Reports.vue'), meta: { title: 'Báo cáo & Thống kê' } },
      { path: 'reviews', name: 'admin-reviews', component: () => import('@/pages/admin/ReviewList.vue'), meta: { title: 'Quản lý Bình luận' } },
      { path: 'settings', name: 'admin-settings', component: () => import('@/pages/admin/Settings.vue'), meta: { title: 'Cài đặt hệ thống' } },
      { path: 'inventory', name: 'admin-inventory', component: () => import('@/pages/admin/Inventory.vue'), meta: { title: 'Quản lý kho hàng' } },
      { path: 'users', name: 'admin-users', component: () => import('@/pages/admin/UserList.vue'), meta: { title: 'Quản lý người dùng' } },
    ],
  },
  // 404
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/pages/NotFound.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() { return { top: 0 } },
})

// ===== NAVIGATION GUARDS =====
router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  // Restore session on first load
  if (!authStore.isAuthenticated && !authStore._initialized) {
    authStore._initialized = true
    await authStore.fetchMe()
  }

  document.title = `${to.meta.title || 'Brevery'} | Bakery & Beverage`

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { name: 'home' }
  }
  if (to.meta.hideIfAuth && authStore.isAuthenticated) {
    return { name: 'home' }
  }
  return true
})

export default router
