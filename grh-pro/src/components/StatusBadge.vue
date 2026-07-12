<template>
  <span :class="[
    'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
    statusClass
  ]">
    <span :class="['w-1.5 h-1.5 rounded-full mr-1.5', dotClass]"></span>
    {{ status }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true
  }
})

const statusClass = computed(() => {
  const classes = {
    'Validé': 'bg-success/10 text-success',
    'Approuvée': 'bg-success/10 text-success',
    'Effectué': 'bg-success/10 text-success',
    'Actif': 'bg-success/10 text-success',
    'En attente': 'bg-warning/10 text-warning',
    'Attente': 'bg-warning/10 text-warning',
    'Refusé': 'bg-danger/10 text-danger',
    'Refusée': 'bg-danger/10 text-danger',
    'Inactif': 'bg-gray-100 text-gray-500'
  }
  return classes[props.status] || 'bg-gray-100 text-gray-600'
})

const dotClass = computed(() => {
  const classes = {
    'Validé': 'bg-success',
    'Approuvée': 'bg-success',
    'Effectué': 'bg-success',
    'Actif': 'bg-success',
    'En attente': 'bg-warning',
    'Attente': 'bg-warning',
    'Refusé': 'bg-danger',
    'Refusée': 'bg-danger',
    'Inactif': 'bg-gray-400'
  }
  return classes[props.status] || 'bg-gray-400'
})
</script>