<template>
  <div class="relative w-full" :style="{ height: height + 'px' }">
    <Bar v-if="hasData" :data="chartData" :options="options" />
    <p v-else class="absolute inset-0 flex items-center justify-center text-sm text-gray-400">
      Pas encore de données
    </p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const props = defineProps({
  labels: { type: Array, default: () => [] },
  datasets: {
    type: Array,
    default: () => []
    // [{ label, data, color }]
  },
  height: { type: Number, default: 280 },
  yTitle: { type: String, default: '' },
  /** Afficher même si toutes les valeurs sont à 0 */
  showEmpty: { type: Boolean, default: true }
})

const hasData = computed(() => {
  if (!props.labels.length) return false
  if (props.showEmpty) return true
  return props.datasets.some(ds => (ds.data || []).some(v => Number(v) > 0))
})

const chartData = computed(() => ({
  labels: props.labels,
  datasets: props.datasets.map(ds => ({
    label: ds.label,
    data: (ds.data || []).map(Number),
    backgroundColor: ds.color || '#0f766e',
    borderRadius: 6,
    borderSkipped: false,
    maxBarThickness: 42
  }))
}))

const options = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  interaction: { mode: 'index', intersect: false },
  plugins: {
    legend: {
      display: props.datasets.length > 1,
      position: 'bottom',
      labels: {
        boxWidth: 12,
        padding: 14,
        font: { size: 12 },
        color: '#475569'
      }
    },
    tooltip: {
      backgroundColor: '#0f172a',
      padding: 10
    }
  },
  scales: {
    x: {
      grid: { display: false },
      ticks: {
        color: '#64748b',
        font: { size: 11 },
        maxRotation: 45,
        minRotation: 0,
        callback(value) {
          const label = this.getLabelForValue(value)
          return label.length > 14 ? label.slice(0, 12) + '…' : label
        }
      }
    },
    y: {
      beginAtZero: true,
      title: props.yTitle ? { display: true, text: props.yTitle, color: '#64748b' } : undefined,
      grid: { color: '#e2e8f0' },
      ticks: {
        color: '#64748b',
        font: { size: 11 },
        precision: 0
      }
    }
  }
}))
</script>
