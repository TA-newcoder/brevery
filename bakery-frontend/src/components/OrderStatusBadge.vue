<template>
  <span :class="['order-badge', badgeClass]">
    <component :is="statusIcon" v-if="statusIcon" size="13" weight="bold" class="badge-icon" />
    {{ statusText }}
  </span>
</template>

<script setup>
import { computed } from 'vue'
import {
  PhClock, PhCheckCircle, PhCookingPot, PhTruck, PhPath, PhPackage, PhXCircle
} from '@phosphor-icons/vue'

const props = defineProps({ status: String })

const statusMap = {
  PENDING:    { class: 'badge--pending',    text: 'Chờ xác nhận',      icon: PhClock },
  CONFIRMED:  { class: 'badge--confirmed',  text: 'Đã xác nhận',       icon: PhCheckCircle },
  PREPARING:  { class: 'badge--preparing',  text: 'Đang chuẩn bị',     icon: PhCookingPot },
  SHIPPED:    { class: 'badge--shipped',     text: 'Đã giao vận chuyển', icon: PhTruck },
  DELIVERING: { class: 'badge--delivering',  text: 'Đang giao hàng',    icon: PhPath },
  COMPLETED:  { class: 'badge--completed',   text: 'Giao thành công',   icon: PhPackage },
  CANCELLED:  { class: 'badge--cancelled',   text: 'Đã huỷ',           icon: PhXCircle },
}

const badgeClass = computed(() => statusMap[props.status]?.class || 'badge--default')
const statusText = computed(() => statusMap[props.status]?.text || props.status)
const statusIcon = computed(() => statusMap[props.status]?.icon || null)
</script>

<style scoped>
.order-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 600;
  white-space: nowrap;
  letter-spacing: 0.01em;
  transition: all 0.2s ease;
}
.badge-icon { flex-shrink: 0; }

.badge--pending    { background: #FFF3CD; color: #856404; }
.badge--confirmed  { background: #E8DAEF; color: #6C3483; }
.badge--preparing  { background: #FDEBD0; color: #CA6F1E; }
.badge--shipped    { background: #D4E6F1; color: #2874A6; }
.badge--delivering { background: #D5F5E3; color: #1E8449; }
.badge--completed  { background: #D4EFDF; color: #196F3D; }
.badge--cancelled  { background: #FADBD8; color: #C0392B; }
.badge--default    { background: var(--bg-muted); color: var(--text-sub); }

[data-theme="dark"] .badge--pending    { background: rgba(255, 243, 205, 0.15); color: #F1C40F; }
[data-theme="dark"] .badge--confirmed  { background: rgba(232, 218, 239, 0.15); color: #BB8FCE; }
[data-theme="dark"] .badge--preparing  { background: rgba(253, 235, 208, 0.15); color: #F0B27A; }
[data-theme="dark"] .badge--shipped    { background: rgba(212, 230, 241, 0.15); color: #5DADE2; }
[data-theme="dark"] .badge--delivering { background: rgba(213, 245, 227, 0.15); color: #58D68D; }
[data-theme="dark"] .badge--completed  { background: rgba(212, 239, 223, 0.15); color: #2ECC71; }
[data-theme="dark"] .badge--cancelled  { background: rgba(250, 219, 216, 0.15); color: #E74C3C; }
</style>
