<template>
  <div :class="['flex items-center', gapClass, alignClass]">
    <img
        src="/logo-grh.png"
        alt="GRH Pro"
        :class="[sizeClass, 'object-contain shrink-0', rounded && 'rounded-lg']"
    />
    <span
        v-if="showWordmark"
        :class="[wordmarkClass, 'font-semibold tracking-tight leading-none']"
    >
      GRH<span :class="accentClass">Pro</span>
      <span v-if="suffix" :class="suffixClass">{{ suffix }}</span>
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** sm | md | lg | xl */
  size: { type: String, default: 'md' },
  showWordmark: { type: Boolean, default: true },
  /** Couleur du wordmark (ex. text-white) */
  wordmarkClass: { type: String, default: 'text-white text-lg' },
  accentClass: { type: String, default: 'text-accent' },
  suffix: { type: String, default: '' },
  suffixClass: { type: String, default: 'text-amber-300' },
  rounded: { type: Boolean, default: true },
  stack: { type: Boolean, default: false }
})

const sizeClass = computed(() => {
  const map = {
    sm: 'h-8 w-8',
    md: 'h-10 w-10',
    lg: 'h-14 w-14',
    xl: 'h-20 w-20'
  }
  return map[props.size] || map.md
})

const gapClass = computed(() => (props.stack ? 'flex-col gap-2' : 'gap-2.5'))
const alignClass = computed(() => (props.stack ? 'items-center text-center' : 'items-center'))
</script>
