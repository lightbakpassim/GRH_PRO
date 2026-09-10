<template>
  <div class="relative w-full" :style="{ height: height + 'px' }">
    <Line v-if="hasData" :data="chartData" :options="options" />
    <p v-else class="absolute inset-0 flex items-center justify-center text-sm text-gray-400">
      Pas encore de données
    </p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler)

const props = defineProps({
  labels: { type: Array, default: () => [] },
  datasets: {
    type: Array,
    default: () => []
    // [{ label, data, color }]
  },
  height: { type: Number, default: 260 },
  yTitle: { type: String, default: '' },
  showEmpty: { type: Boolean, default: false }
})

const hasData = computed(() => {
  if (props.showEmpty && props.labels.length) return true
  return props.datasets.some(ds => (ds.data || []).some(v => Number(v) > 0))
})

const chartData = computed(() => ({
  labels: props.labels,
  datasets: props.datasets.map(ds => ({
    label: ds.label,
    data: (ds.data || []).map(Number),
    borderColor: ds.color || '#1a7a7a',
    backgroundColor: (ds.color || '#1a7a7a') + '22',
    borderWidth: 2.5,
    tension: 0.35,
    fill: true,
    pointRadius: 4,
    pointHoverRadius: 6,
    pointBackgroundColor: ds.color || '#1a7a7a'
  }))
}))

const options = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  interaction: { mode: 'index', intersect: false },
  plugins: {
    legend: {
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
      ticks: { color: '#64748b', font: { size: 11 } }
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
