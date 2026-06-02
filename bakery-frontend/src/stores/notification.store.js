import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)

  function addNotification(notification) {
    notifications.value.unshift({
      ...notification,
      id: Date.now(),
      read: false,
      time: new Date().toLocaleTimeString('vi-VN'),
    })
    unreadCount.value++
    // Play beep sound
    try {
      const ctx = new (window.AudioContext || window.webkitAudioContext)()
      const osc = ctx.createOscillator()
      const gain = ctx.createGain()
      osc.connect(gain)
      gain.connect(ctx.destination)
      osc.frequency.value = 800
      gain.gain.value = 0.1
      osc.start()
      osc.stop(ctx.currentTime + 0.15)
    } catch { /* ignore audio errors */ }
  }

  function markAllRead() {
    notifications.value.forEach(n => n.read = true)
    unreadCount.value = 0
  }

  function clearAll() {
    notifications.value = []
    unreadCount.value = 0
  }

  return { notifications, unreadCount, addNotification, markAllRead, clearAll }
})
