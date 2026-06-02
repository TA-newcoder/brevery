import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartApi } from '@/api/cart.api'
import { useAuthStore } from './auth.store'
import { toast } from 'vue3-toastify'

const GUEST_CART_KEY = 'bakery_guest_cart'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loading = ref(false)

  const totalItems = computed(() => items.value.reduce((sum, i) => sum + i.quantity, 0))
  const totalPrice = computed(() => items.value.reduce((sum, i) => sum + (i.price * i.quantity), 0))

  // ===== Guest Cart (localStorage) =====
  function getGuestCart() {
    try {
      return JSON.parse(localStorage.getItem(GUEST_CART_KEY) || '[]')
    } catch { return [] }
  }

  function saveGuestCart(cart) {
    localStorage.setItem(GUEST_CART_KEY, JSON.stringify(cart))
  }

  // ===== Load Cart =====
  async function fetchCart() {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      items.value = getGuestCart()
      return
    }
    loading.value = true
    try {
      const { data } = await cartApi.getCart()
      items.value = data.data?.items || []
    } catch {
      items.value = []
    } finally {
      loading.value = false
    }
  }

  // ===== Add to Cart =====
  async function addToCart(product) {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      const cart = getGuestCart()
      const existing = cart.find(i => i.variantId === product.variantId)
      if (existing) {
        existing.quantity += product.quantity || 1
      } else {
        cart.push({ ...product, quantity: product.quantity || 1 })
      }
      saveGuestCart(cart)
      items.value = cart
      toast.success('Đã thêm vào giỏ hàng!')
      return
    }
    try {
      await cartApi.addToCart({ variantId: product.variantId, quantity: product.quantity || 1 })
      await fetchCart()
      toast.success('Đã thêm vào giỏ hàng!')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Thêm thất bại')
    }
  }

  // ===== Update Quantity =====
  async function updateQuantity(cartItemId, quantity) {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      const cart = getGuestCart()
      const item = cart.find(i => i.cartItemId === cartItemId || i.variantId === cartItemId)
      if (item) item.quantity = quantity
      saveGuestCart(cart)
      items.value = cart
      return
    }
    try {
      await cartApi.updateCartItem(cartItemId, { quantity })
      await fetchCart()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Cập nhật thất bại')
    }
  }

  // ===== Remove Item =====
  async function removeItem(cartItemId) {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      let cart = getGuestCart()
      cart = cart.filter(i => i.cartItemId !== cartItemId && i.variantId !== cartItemId)
      saveGuestCart(cart)
      items.value = cart
      return
    }
    try {
      await cartApi.removeCartItem(cartItemId)
      await fetchCart()
      toast.success('Đã xóa sản phẩm')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Xóa thất bại')
    }
  }

  // ===== Clear Cart =====
  async function clearCart() {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      saveGuestCart([])
      items.value = []
      return
    }
    try {
      await cartApi.clearCart()
      items.value = []
    } catch { /* ignore */ }
  }

  return {
    items, loading, totalItems, totalPrice,
    fetchCart, addToCart, updateQuantity, removeItem, clearCart,
  }
})
