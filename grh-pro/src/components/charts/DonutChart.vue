<template>
  <div class="relative w-full" :style="{ height: height + 'px' }">
    <Doughnut v-if="hasData" :data="chartData" :options="options" />
    <p v-else class="absolute inset-0 flex items-center justify-center text-sm text-gray-400">
      Pas encore de données
    </p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Doughnut } from 'vue-chartjs'
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend
} from 'chart.js'

ChartJS.register(ArcElement, Tooltip, Legend)

const props = defineProps({
  labels: { type: Array, default: () => [] },
  values: { type: Array, default: () => [] },
  colors: {
    type: Array,
    default: () => ['#1a7a7a', '#f5a623', '#4caf7d', '#e57373', '#64748b', '#0ea5e9']
  },
  height: { type: Number, default: 260 }
})

const hasData = computed(() =>
  props.values.some(v => Number(v) > 0)
)

const chartData = computed(() => ({
  labels: props.labels,
  datasets: [{
    data: props.values.map(Number),
    backgroundColor: props.colors.slice(0, props.labels.length),
    borderWidth: 2,
    borderColor: '#ffffff',
    hoverOffset: 6
  }]
}))

const options = {
  responsive: true,
  maintainAspectRatio: false,
  cutout: '58%',
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
      callbacks: {
        label: (ctx) => {
          const total = ctx.dataset.data.reduce((a, b) => a + b, 0) || 1
          const v = ctx.parsed
          const pct = Math.round((v / total) * 100)
          return ` ${ctx.label}: ${v} (${pct}%)`
        }
      }
    }
  }
}
</script>
