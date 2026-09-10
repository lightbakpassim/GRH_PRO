<template>
  <aside
      :class="[
      'fixed inset-y-0 left-0 z-40 h-full bg-teal-800 transition-transform duration-300 shrink-0',
      'lg:relative lg:inset-auto lg:z-auto lg:translate-x-0',
      isOpen ? 'translate-x-0 w-64' : '-translate-x-full w-64 lg:translate-x-0',
      isCollapsed ? 'lg:w-20' : 'lg:w-auto lg:min-w-50'
    ]"
  >
    <div class="flex flex-col h-full">
      <!-- Logo -->
      <div class="h-16 flex items-center justify-center border-b border-primary-700 px-3">
        <AppLogo
            v-if="!isCollapsed"
            size="sm"
            wordmark-class="text-white text-xl"
            accent-class="text-accent"
        />
        <AppLogo v-else size="sm" :show-wordmark="false" />
      </div>

      <!-- Toggle button desktop -->
      <button
          @click="toggleCollapse"
          class="hidden lg:flex absolute -right-3 top-20 bg-white rounded-full p-1 shadow-md hover:shadow-lg transition-all"
      >
        <ChevronLeftIcon v-if="!isCollapsed" class="w-4 h-4 text-primary" />
        <ChevronRightIcon v-else class="w-4 h-4 text-primary" />
      </button>

      <!-- Navigation -->
      <nav class="flex-1 px-3 py-4 space-y-1">
        <router-link
            v-for="item in menuItems"
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-3 py-2.5 rounded-md text-white/80 hover:bg-white/10 hover:text-white transition-all duration-200 group"
            active-class="bg-white/20 text-white"
            @click="closeOnMobile"
        >
          <component :is="item.icon" class="w-5 h-5 flex-shrink-0" />
          <span v-if="!isCollapsed" class="text-sm font-medium">{{ item.name }}</span>
          <span v-if="isCollapsed" class="absolute left-full ml-2 px-2 py-1 bg-primary-dark text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap z-50">
            {{ item.name }}
          </span>
        </router-link>
      </nav>

      <div class="p-3 border-t border-primary-700 space-y-1">
        <button
            @click="openPassword"
            class="flex items-center gap-3 px-3 py-2.5 w-full rounded-md text-white/80 hover:bg-white/10 hover:text-white transition-all duration-200"
        >
          <KeyIcon class="w-5 h-5" />
          <span v-if="!isCollapsed" class="text-sm font-medium">Mot de passe</span>
        </button>
        <button
            @click="handleLogout"
            class="flex items-center gap-3 px-3 py-2.5 w-full rounded-md text-white/80 hover:bg-danger/20 hover:text-danger transition-all duration-200"
        >
          <ArrowRightStartOnRectangleIcon class="w-5 h-5" />
          <span v-if="!isCollapsed" class="text-sm font-medium">Déconnexion</span>
        </button>
      </div>
    </div>
    <ChangePasswordModal :open="showPwd" @close="showPwd = false" />
  </aside>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/data.js'
import { useToast } from '@/composable/useToast'
import ChangePasswordModal from '@/components/ChangePasswordModal.vue'
import AppLogo from '@/components/AppLogo.vue'
import {
  HomeIcon,
  UsersIcon,
  ClockIcon,
  CalendarIcon,
  DocumentTextIcon,
  CreditCardIcon,
  BuildingOffice2Icon,
  ChevronLeftIcon,
  ChevronRightIcon,
  ArrowRightStartOnRectangleIcon,
  KeyIcon
} from '@heroicons/vue/24/outline'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle', 'close'])

const router = useRouter()
const authStore = useAuthStore()
const { success } = useToast()
const isCollapsed = ref(false)
const showPwd = ref(false)

const menuItems = [
  { name: 'Dashboard', path: '/admin/dashboard', icon: HomeIcon },
  { name: 'Départements', path: '/admin/departements', icon: BuildingOffice2Icon },
  { name: 'Employés', path: '/admin/employes', icon: UsersIcon },
  { name: 'Pointages', path: '/admin/pointages', icon: ClockIcon },
  { name: 'Congés', path: '/admin/conges', icon: CalendarIcon },
  { name: 'Paiement', path: '/admin/paiement', icon: CreditCardIcon },
  { name: 'Rapports', path: '/admin/rapports', icon: DocumentTextIcon }
]

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

const closeOnMobile = () => {
  if (window.innerWidth < 1024) emit('close')
}

const openPassword = () => {
  closeOnMobile()
  showPwd.value = true
}

const handleLogout = () => {
  authStore.logout()
  success('Déconnexion réussie')
  router.push('/login')
}
</script>