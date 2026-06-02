<template>
  <span :class="badgeClass">{{ statusText }}</span>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ status: String })

const statusMap = {
  PENDING: { class: 'badge-caramel', text: 'Chờ xác nhận' },
  CONFIRMED: { class: 'badge-purple', text: 'Đã xác nhận' },
  SHIPPING: { class: 'badge-pink', text: 'Đang giao' },
  DELIVERED: { class: 'badge-green', text: 'Đã giao' },
  CANCELLED: { class: 'badge bg-secondary', text: 'Đã hủy' },
}
const badgeClass = computed(() => statusMap[props.status]?.class || 'badge bg-secondary')
const statusText = computed(() => statusMap[props.status]?.text || props.status)
</script>
