<template>
  <div class="flex h-screen bg-gray-100 overflow-hidden">
    <!-- Sidebar -->
    <EmployeSidebar :is-open="sidebarOpen" @toggle="toggleSidebar" />

    <!-- Overlay mobile -->
    <div
        v-if="sidebarOpen && isMobile"
        class="fixed inset-0 bg-black/50 z-20 lg:hidden"
        @click="toggleSidebar"
    />

    <!-- Contenu principal -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- Header mobile -->
      <header class="bg-white shadow-sm lg:hidden">
        <div class="px-4 py-3 flex items-center justify-between">
          <button
              @click="toggleSidebar"
              class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <Bars3Icon class="w-6 h-6 text-gray-600" />
          </button>
          <h1 class="text-lg font-semibold text-blue-600">GRH Pro</h1>
          <div class="w-8"></div>
        </div>
      </header>

      <!-- Main content -->
      <main class="flex-1 overflow-y-auto p-4 md:p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Bars3Icon } from '@heroicons/vue/24/outline'
import EmployeSidebar from '@/components/EmployeSiderbar.vue'

const sidebarOpen = ref(false)
const isMobile = ref(window.innerWidth < 1024)

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const handleResize = () => {
  isMobile.value = window.innerWidth < 1024
  if (!isMobile.value) {
    sidebarOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>