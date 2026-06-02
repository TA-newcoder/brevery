<template>
  <teleport to="body">
    <transition name="fade">
      <div v-if="show" class="modal-backdrop-custom" @click.self="$emit('cancel')">
        <div class="confirm-modal bakery-card">
          <h5 class="mb-3">{{ title }}</h5>
          <p class="text-sub mb-4">{{ message }}</p>
          <div class="d-flex justify-content-end gap-2">
            <button class="btn btn-bakery-outline btn-sm" @click="$emit('cancel')">Hủy</button>
            <button :class="['btn btn-sm', danger ? 'btn-danger' : 'btn-bakery']" @click="$emit('confirm')">{{ confirmText }}</button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
defineProps({
  show: Boolean,
  title: { type: String, default: 'Xác nhận' },
  message: { type: String, default: 'Bạn có chắc chắn?' },
  confirmText: { type: String, default: 'Xác nhận' },
  danger: Boolean,
})
defineEmits(['confirm', 'cancel'])
</script>

<style scoped>
.modal-backdrop-custom {
  position: fixed; inset: 0;
  background: rgba(0,0,0,.4);
  display: flex; align-items: center; justify-content: center;
  z-index: 9999;
}
.confirm-modal { width: 420px; max-width: 90vw; }
</style>
