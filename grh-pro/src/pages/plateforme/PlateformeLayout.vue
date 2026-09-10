<template>
  <div class="flex h-[100dvh] bg-slate-100 overflow-hidden">
    <PlateformeSidebar :is-open="sidebarOpen" @close="closeSidebar" @toggle="toggleSidebar" />

    <div
        v-if="sidebarOpen && isMobile"
        class="fixed inset-0 bg-black/50 z-30 lg:hidden"
        @click="closeSidebar"
    />

    <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <header class="bg-white shadow-sm lg:hidden sticky top-0 z-20">
        <div class="px-3 py-2.5 flex items-center justify-between gap-2">
          <button
              type="button"
              @click="toggleSidebar"
              class="p-2.5 min-h-11 min-w-11 rounded-lg hover:bg-gray-100"
              aria-label="Ouvrir le menu"
          >
            <Bars3Icon class="w-6 h-6 text-gray-600" />
          </button>
          <h1 class="text-base font-semibold text-slate-800 truncate">Plateforme GRH_PRO</h1>
          <div class="w-11 shrink-0" />
        </div>
      </header>

      <main class="flex-1 overflow-y-auto overflow-x-hidden p-3 sm:p-4 md:p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { Bars3Icon } from '@heroicons/vue/24/outline'
import PlateformeSidebar from '@/components/PlateformeSidebar.vue'

const route = useRoute()
const sidebarOpen = ref(false)
const isMobile = ref(false)

const checkMobile = () => {
  isMobile.value = window.innerWidth < 1024
  if (!isMobile.value) sidebarOpen.value = false
}
const toggleSidebar = () => { sidebarOpen.value = !sidebarOpen.value }
const closeSidebar = () => { sidebarOpen.value = false }

watch(() => route.path, () => { if (isMobile.value) closeSidebar() })
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})
onUnmounted(() => window.removeEventListener('resize', checkMobile))
</script>
