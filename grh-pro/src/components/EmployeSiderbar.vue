<template>
  <aside
      :class="[
      'fixed lg:relative z-30 h-full bg-gray-800 transition-all duration-300', shrink-0,
      isOpen ? 'w-64' : 'w-64 -translate-x-full lg:translate-x-0',
      isCollapsed ? 'lg:w-20' : 'lg:w-auto lg:min-w-50'
    ]"
      style="background-color: #1a1a1a"
  >
    <div class="flex flex-col h-full">
      <!-- Logo -->
      <div class="h-16 flex items-center justify-center border-b border-gray-700">
        <div v-if="!isCollapsed" class="text-white font-bold text-xl">
          GRH<span class="text-blue-400">Pro</span>
        </div>
        <div v-else class="text-white font-bold text-2xl">
          G<span class="text-blue-400">R</span>
        </div>
      </div>

      <!-- User info -->
      <div class="p-4 border-b border-gray-700 text-center">
        <div class="w-12 h-12 mx-auto bg-blue-600 rounded-full flex items-center justify-center mb-2">
          <span class="text-white font-bold text-lg">{{ userInitials }}</span>
        </div>
        <p v-if="!isCollapsed" class="text-white text-sm font-medium">{{ userName }}</p>
        <p v-if="!isCollapsed" class="text-gray-400 text-xs">{{ userEmail }}</p>
      </div>

      <!-- Navigation -->
      <nav class="flex-1 px-3 py-4 space-y-1">
        <router-link
            v-for="item in menuItems"
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-3 py-2.5 rounded-md text-gray-300 hover:bg-gray-700 hover:text-white transition-all duration-200 group"
            active-class="bg-blue-600 text-white"
            @click="closeOnMobile"
        >
          <component :is="item.icon" class="w-5 h-5 flex-shrink-0" />
          <span v-if="!isCollapsed" class="text-sm font-medium">{{ item.name }}</span>
          <span v-if="isCollapsed" class="absolute left-full ml-2 px-2 py-1 bg-gray-900 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap z-50">
            {{ item.name }}
          </span>
        </router-link>
      </nav>

      <!-- Footer -->
      <div class="p-3 border-t border-gray-700">
        <button
            @click="handleLogout"
            class="flex items-center gap-3 px-3 py-2.5 w-full rounded-md text-gray-300 hover:bg-red-500/20 hover:text-red-400 transition-all duration-200"
        >
          <ArrowRightStartOnRectangleIcon class="w-5 h-5" />
          <span v-if="!isCollapsed" class="text-sm font-medium">Déconnexion</span>
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/data.js'
import { useToast } from '@/composable/useToast'
import {
  HomeIcon,
  ClockIcon,
  CalendarIcon,
  CreditCardIcon,
  ArrowRightStartOnRectangleIcon
} from '@heroicons/vue/24/outline'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle'])

const router = useRouter()
const authStore = useAuthStore()
const { success } = useToast()
const isCollapsed = ref(false)

const userInitials = computed(() => {
  const name = authStore.user?.name || 'Employé'
  return name.split(' ').map(n => n[0]).join('').toUpperCase()
})

const userName = computed(() => authStore.user?.name || 'Employé')
const userEmail = computed(() => authStore.user?.email || 'employe@grh.tg')

const menuItems = [
  { name: 'Dashboard', path: '/employe/dashboard', icon: HomeIcon },
  { name: 'Pointages', path: '/employe/pointage', icon: ClockIcon },
  { name: 'Congés', path: '/employe/conges', icon: CalendarIcon },
  { name: 'Paiement', path: '/employe/paiement', icon: CreditCardIcon }
]

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

const closeOnMobile = () => {
  if (window.innerWidth < 1024) {
    emit('toggle')
  }
}

const handleLogout = () => {
  authStore.logout()
  success('Déconnexion réussie')
  router.push('/login')
}
</script>