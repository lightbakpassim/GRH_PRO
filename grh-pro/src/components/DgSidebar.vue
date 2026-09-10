<template>
  <aside
      :class="[
      'fixed inset-y-0 left-0 z-40 h-full w-64 bg-slate-900 transition-transform duration-300 shrink-0',
      'lg:relative lg:inset-auto lg:z-auto lg:translate-x-0',
      isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
    ]"
  >
    <div class="flex flex-col h-full">
      <div class="h-16 flex items-center justify-center border-b border-white/10 px-3">
        <AppLogo
            size="sm"
            wordmark-class="text-white text-xl"
            accent-class="text-amber-400"
            suffix=" DG"
            suffix-class="text-amber-400"
        />
      </div>
      <nav class="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        <router-link
            v-for="item in menuItems"
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-3 py-2.5 min-h-11 rounded-md text-white/80 hover:bg-white/10 hover:text-white"
            active-class="bg-white/20 text-white"
            @click="closeOnMobile"
        >
          <component :is="item.icon" class="w-5 h-5 shrink-0" />
          <span class="text-sm font-medium">{{ item.name }}</span>
        </router-link>
        <button
            type="button"
            @click="onChangePassword"
            class="flex items-center gap-3 px-3 py-2.5 min-h-11 w-full rounded-md text-white/80 hover:bg-white/10"
        >
          <KeyIcon class="w-5 h-5 shrink-0" />
          <span class="text-sm font-medium">Mot de passe</span>
        </button>
      </nav>
      <div class="p-3 border-t border-white/10">
        <button
            type="button"
            @click="handleLogout"
            class="flex items-center gap-3 px-3 py-2.5 min-h-11 w-full rounded-md text-white/80 hover:bg-red-500/20"
        >
          <ArrowRightStartOnRectangleIcon class="w-5 h-5 shrink-0" />
          <span class="text-sm font-medium">Déconnexion</span>
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/data.js'
import { useToast } from '@/composable/useToast'
import AppLogo from '@/components/AppLogo.vue'
import { DocumentTextIcon, ClockIcon, ArrowRightStartOnRectangleIcon, KeyIcon, HomeIcon } from '@heroicons/vue/24/outline'

defineProps({ isOpen: { type: Boolean, default: false } })
const emit = defineEmits(['toggle', 'close', 'change-password'])

const router = useRouter()
const authStore = useAuthStore()
const { success } = useToast()

const menuItems = [
  { name: 'Dashboard', path: '/dg/dashboard', icon: HomeIcon },
  { name: 'Rapports', path: '/dg/rapports', icon: DocumentTextIcon },
  { name: 'Historique', path: '/dg/historique', icon: ClockIcon }
]

const closeOnMobile = () => {
  if (window.innerWidth < 1024) emit('close')
}

const onChangePassword = () => {
  emit('change-password')
}

const handleLogout = () => {
  authStore.logout()
  success('Déconnexion réussie')
  router.push('/login')
}
</script>
