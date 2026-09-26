<template>
  <div
      class="card card-hover p-5 cursor-pointer"
      @click="handleClick"
  >
    <div class="flex items-center justify-between">
      <div>
        <p class="text-sm text-gray-500 font-medium">{{ title }}</p>
        <p class="text-2xl font-bold text-gray-800 mt-1">{{ value }}</p>
      </div>
      <div :class="[
        'w-12 h-12 rounded-full flex items-center justify-center',
        iconBgClass
      ]">
        <component :is="icon" :class="['w-6 h-6', iconColorClass]" />
      </div>
    </div>
    <div v-if="subtitle" class="mt-3 pt-2 border-t border-primary-50">
      <p class="text-xs text-gray-400">{{ subtitle }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  icon: {
    type: Object,
    required: true
  },
  iconBg: {
    type: String,
    default: 'primary'
  },
  subtitle: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['click'])

const iconBgClass = computed(() => {
  const classes = {
    primary: 'bg-primary-100',
    success: 'bg-success/10',
    warning: 'bg-warning/10',
    danger: 'bg-danger/10',
    accent: 'bg-accent/10'
  }
  return classes[props.iconBg] || classes.primary
})

const iconColorClass = computed(() => {
  const classes = {
    primary: 'text-primary',
    success: 'text-success',
    warning: 'text-warning',
    danger: 'text-danger',
    accent: 'text-accent'
  }
  return classes[props.iconBg] || classes.primary
})

const handleClick = () => {
  emit('click')
}
</script>