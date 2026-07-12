<template>
  <aside
      :class="[
      'fixed lg:relative z-30 h-full bg-gradient-to-b from-primary-700 to-primary-900 transition-all duration-300',
      isOpen ? 'w-64 translate-x-0' : 'w-64 -translate-x-full lg:translate-x-0 lg:w-20'
    ]"
  >
    <div class="flex flex-col h-full">
      <!-- Logo -->
      <div class="h-16 flex items-center justify-center border-b border-primary-600">
        <div v-if="!isCollapsed" class="text-white font-bold text-xl">
          GRH<span class="text-primary-300">Pro</span>
        </div>
        <div v-else class="text-white font-bold text-2xl">
          G<span class="text-primary-300">R</span>
        </div>
      </div>

      <!-- Toggle button (desktop) -->
      <button
          @click="toggleCollapse"
          class="hidden lg:flex absolute -right-3 top-20 bg-white rounded-full p-1 shadow-md hover:shadow-lg transition-all"
      >
        <ChevronLeftIcon v-if="!isCollapsed" class="w-4 h-4 text-primary-600" />
        <ChevronRightIcon v-else class="w-4 h-4 text-primary-600" />
      </button>

      <!-- Navigation -->
      <nav class="flex-1 px-3 py-4 space-y-1">
        <router-link
            v-for="item in filteredMenuItems"
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-primary-100 hover:bg-primary-600 transition-all duration-200 group"
            active-class="bg-primary-600 text-white shadow-md"
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
      <div class="p-3 border-t border-primary-600">
        <button
            @click="handleLogout"
            class="flex items-center gap-3 px-3 py-2.5 w-full rounded-lg text-primary-100 hover:bg-danger/20 transition-all duration-200"
        >
          <ArrowRightStartOnRectangleIcon class="w-5 h-5" />
          <span v-if="!isCollapsed" class="text-sm font-medium">Déconnexion</span>
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useToast } from '@/composables/useToast'
import {
  HomeIcon,
  UsersIcon,
  ClockIcon,
  CalendarIcon,
  CreditCardIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
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

const isAdmin = computed(() => authStore.user?.role === 'Admin')

const menuItems = computed(() => {
  if (isAdmin.value) {
    return [
      { name: 'Dashboard', path: '/admin', icon: HomeIcon },
      { name: 'Employés', path: '/admin/employes', icon: UsersIcon },
      { name: 'Pointages', path: '/admin/pointages', icon: ClockIcon },
      { name: 'Congés', path: '/admin/conges', icon: CalendarIcon },
      { name: 'Paiement', path: '/admin/paiements', icon: CreditCardIcon }
    ]
  }
  return [
    { name: 'Dashboard', path: '/employe', icon: HomeIcon },
    { name: 'Pointages', path: '/employe/pointages', icon: ClockIcon },
    { name: 'Congés', path: '/employe/conges', icon: CalendarIcon },
    { name: 'Paiement', path: '/employe/paiements', icon: CreditCardIcon }
  ]
})

const filteredMenuItems = computed(() => menuItems.value)

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