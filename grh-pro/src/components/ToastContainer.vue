<template>
  <div class="fixed bottom-4 left-4 right-4 sm:left-auto sm:right-4 z-50 space-y-2 sm:max-w-sm">
    <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="[
        'flex items-center gap-3 px-4 py-3 rounded-lg shadow-lg animate-slide-in w-full min-w-0',
        toast.type === 'success' && 'bg-success text-white',
        toast.type === 'error' && 'bg-danger text-white',
        toast.type === 'warning' && 'bg-warning text-white',
        toast.type === 'info' && 'bg-info text-white'
      ]"
    >
      <CheckCircleIcon v-if="toast.type === 'success'" class="w-5 h-5 shrink-0" />
      <XCircleIcon v-else-if="toast.type === 'error'" class="w-5 h-5 shrink-0" />
      <ExclamationTriangleIcon v-else-if="toast.type === 'warning'" class="w-5 h-5 shrink-0" />
      <InformationCircleIcon v-else class="w-5 h-5 shrink-0" />
      <span class="flex-1 text-sm break-words">{{ toast.message }}</span>
      <button type="button" @click="removeToast(toast.id)" class="opacity-70 hover:opacity-100 p-1 shrink-0">
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
