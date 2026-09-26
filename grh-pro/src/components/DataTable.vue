<template>
  <div class="overflow-x-auto rounded-lg border border-gray-200 bg-white">
    <table class="min-w-full divide-y divide-gray-200">
      <thead class="bg-gray-50">
      <tr>
        <th
            v-for="column in columns"
            :key="column.key"
            :style="{ width: column.width }"
            class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider"
        >
          {{ column.label }}
        </th>
        <th v-if="actions.length > 0" class="px-6 py-3 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">
          Actions
        </th>
      </tr>
      </thead>
      <tbody class="divide-y divide-gray-200">
      <tr
          v-for="row in data"
          :key="row.id"
          class="hover:bg-gray-50 transition-colors duration-150"
      >
        <td
            v-for="column in columns"
            :key="column.key"
            class="px-6 py-4 whitespace-nowrap text-sm text-gray-700"
        >
          <slot :name="`column-${column.key}`" :row="row" :value="row[column.key]">
              <span v-if="column.type === 'badge'">
                <StatusBadge :status="row[column.key]" />
              </span>
            <span v-else-if="column.type === 'date'">
                {{ formatDate(row[column.key]) }}
              </span>
            <span v-else-if="column.type === 'currency'">
                {{ formatCurrency(row[column.key]) }}
              </span>
            <span v-else>
                {{ row[column.key] }}
              </span>
          </slot>
        </td>
        <td v-if="actions.length > 0" class="px-6 py-4 whitespace-nowrap text-right">
          <div class="flex items-center justify-end gap-2">
            <button
                v-for="action in actions"
                :key="action.label"
                @click="action.onClick(row)"
                :class="action.buttonClass || 'text-gray-400 hover:text-blue-600'"
                class="p-1 rounded transition-colors"
                :title="action.label"
            >
              <component :is="action.icon" class="w-5 h-5" />
            </button>
          </div>
        </td>
      </tr>
      <tr v-if="data.length === 0">
        <td :colspan="columns.length + (actions.length > 0 ? 1 : 0)" class="px-6 py-8 text-center text-gray-500">
          <div class="flex flex-col items-center gap-2">
            <InboxIcon class="w-12 h-12 text-gray-300" />
            <p>Aucune donnée disponible</p>
          </div>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import dayjs from 'dayjs'
import StatusBadge from './StatusBadge.vue'
import { InboxIcon } from '@heroicons/vue/24/outline'

defineProps({
  columns: {
    type: Array,
    required: true
  },
  data: {
    type: Array,
    required: true
  },
  actions: {
    type: Array,
    default: () => []
  }
})

const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('DD/MM/YYYY')
}

const formatCurrency = (value) => {
  if (!value) return '0 FCFA'
  return new Intl.NumberFormat('fr-FR').format(value) + ' FCFA'
}
</script>