<template>
  <nav v-if="totalPages > 1" class="bakery-pagination">
    <ul class="pagination justify-content-center mb-0">
      <li :class="['page-item', { disabled: modelValue <= 0 }]">
        <a class="page-link" @click.prevent="changePage(modelValue - 1)">‹</a>
      </li>
      <li v-for="p in visiblePages" :key="p" :class="['page-item', { active: p === modelValue }]">
        <a class="page-link" @click.prevent="changePage(p)">{{ p + 1 }}</a>
      </li>
      <li :class="['page-item', { disabled: modelValue >= totalPages - 1 }]">
        <a class="page-link" @click.prevent="changePage(modelValue + 1)">›</a>
      </li>
    </ul>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ modelValue: Number, totalPages: Number })
const emit = defineEmits(['update:modelValue'])

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(0, props.modelValue - 2)
  const end = Math.min(props.totalPages - 1, props.modelValue + 2)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

function changePage(p) {
  if (p >= 0 && p < props.totalPages) emit('update:modelValue', p)
}
</script>
