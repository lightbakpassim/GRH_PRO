<template>
  <div class="flex h-[100dvh] bg-gray-100 overflow-hidden">
    <EmployeSidebar
        :is-open="sidebarOpen"
        @close="closeSidebar"
        @toggle="toggleSidebar"
        @change-password="showPwd = true"
    />

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
          <h1 class="text-base sm:text-lg font-semibold text-primary flex items-center gap-2 truncate">
            <img src="/logo-grh.png" alt="" class="h-7 w-7 rounded object-contain shrink-0" />
            GRH Pro
          </h1>
          <div class="w-11 shrink-0" />
        </div>
      </header>

      <main class="flex-1 overflow-y-auto overflow-x-hidden p-3 sm:p-4 md:p-6">
        <router-view />
      </main>
    </div>

    <ChangePasswordModal :open="showPwd" @close="showPwd = false" />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { Bars3Icon } from '@heroicons/vue/24/outline'
import EmployeSidebar from '@/components/EmployeSidebar.vue'
import ChangePasswordModal from '@/components/ChangePasswordModal.vue'

const route = useRoute()
const sidebarOpen = ref(false)
const showPwd = ref(false)
const isMobile = ref(typeof window !== 'undefined' ? window.innerWidth < 1024 : true)

const toggleSidebar = () => { sidebarOpen.value = !sidebarOpen.value }
const closeSidebar = () => { sidebarOpen.value = false }

const handleResize = () => {
  isMobile.value = window.innerWidth < 1024
  if (!isMobile.value) sidebarOpen.value = false
}

watch(() => route.fullPath, closeSidebar)
watch(sidebarOpen, (open) => {
  document.body.style.overflow = open && isMobile.value ? 'hidden' : ''
})

onMounted(() => window.addEventListener('resize', handleResize))
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  document.body.style.overflow = ''
})
</script>
