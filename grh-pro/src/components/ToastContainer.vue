<template>
  <div class="fixed bottom-4 right-4 z-50 space-y-2">
    <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="[
        'flex items-center gap-3 px-4 py-3 rounded-lg shadow-lg animate-slide-in min-w-[280px] max-w-[400px]',
        toast.type === 'success' && 'bg-success text-white',
        toast.type === 'error' && 'bg-danger text-white',
        toast.type === 'warning' && 'bg-warning text-white',
        toast.type === 'info' && 'bg-info text-white'
      ]"
    >
      <CheckCircleIcon v-if="toast.type === 'success'" class="w-5 h-5" />
      <XCircleIcon v-else-if="toast.type === 'error'" class="w-5 h-5" />
      <ExclamationTriangleIcon v-else-if="toast.type === 'warning'" class="w-5 h-5" />
      <InformationCircleIcon v-else class="w-5 h-5" />
      <span class="flex-1 text-sm">{{ toast.message }}</span>
      <button @click="removeToast(toast.id)" class="opacity-70 hover:opacity-100">
        <XMarkIcon class="w-4 h-4" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { useToast } from '@/composable/useToast'
import {
  CheckCircleIcon,
  XCircleIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  XMarkIcon
} from '@heroicons/vue/24/outline'

const { toasts, removeToast } = useToast()
</script>