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

  function showCartToast(product) {
    const productName = product.name || product.productName || 'Sản phẩm'
    toast.success(`
      <div style="font-family: 'DM Sans', sans-serif;">
        <strong style="color: var(--text-main);">${productName}</strong><br/>
        <span style="font-size: 0.9rem; color: var(--text-sub);">Đã được thêm vào giỏ hàng</span>
        <div style="margin-top: 8px;">
          <a href="/cart" style="background: var(--primary); color: white; padding: 4px 12px; border-radius: 99px; text-decoration: none; font-size: 0.85rem; display: inline-block;">Xem giỏ hàng →</a>
        </div>
      </div>
    `, {
      dangerouslyHTMLString: true,
      autoClose: 2000,
      hideProgressBar: false,
      closeOnClick: false,
      pauseOnHover: true,
      icon: '✓'
    })
  }

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
      showCartToast(product)
      return
    }
    try {
      await cartApi.addToCart({ variantId: product.variantId, quantity: product.quantity || 1 })
      await fetchCart()
      showCartToast(product)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Thêm thất bại')
    }
  }

  // ===== Update Quantity =====
  async function updateQuantity(cartItemId, quantity) {
    // Optimistic Update
    const item = items.value.find(i => i.cartItemId === cartItemId || i.variantId === cartItemId)
    if (item) item.quantity = quantity

    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      saveGuestCart(items.value)
      return
    }
    try {
      await cartApi.updateCartItem(cartItemId, { quantity })
      await fetchCart()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Cập nhật thất bại')
      await fetchCart() // Revert on failure
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
      toast.success('Đã xóa sản phẩm', { autoClose: 1500, hideProgressBar: true })
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

  // ===== Change Variant =====
  async function changeVariant(oldItem, newVariant) {
    // Optimistic Update
    const index = items.value.findIndex(i => i.cartItemId === oldItem.cartItemId || i.variantId === oldItem.variantId)
    if (index !== -1) {
      items.value[index].variantId = newVariant.variantId
      items.value[index].variantName = newVariant.size
      items.value[index].price = newVariant.salePrice || newVariant.price
    }

    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      let cart = getGuestCart()
      cart = cart.filter(i => i.variantId !== oldItem.variantId)
      const existing = cart.find(i => i.variantId === newVariant.variantId)
      if (existing) {
        existing.quantity += oldItem.quantity
      } else {
        cart.push({
          productId: oldItem.productId,
          variantId: newVariant.variantId,
          quantity: oldItem.quantity,
          productName: oldItem.productName,
          variantName: newVariant.size,
          price: newVariant.salePrice || newVariant.price,
          imageUrl: oldItem.imageUrl || oldItem.primaryImageUrl,
          availableVariants: oldItem.availableVariants
        })
      }
      saveGuestCart(cart)
      items.value = cart
      return
    }
    
    try {
      await cartApi.removeCartItem(oldItem.cartItemId)
      await cartApi.addToCart({ variantId: newVariant.variantId, quantity: oldItem.quantity })
      await fetchCart()
    } catch (err) {
      toast.error('Lỗi khi đổi phân loại')
      await fetchCart() // Revert on failure
    }
  }

  return {
    items, loading, totalItems, totalPrice,
    fetchCart, addToCart, updateQuantity, removeItem, clearCart, changeVariant
  }
})
