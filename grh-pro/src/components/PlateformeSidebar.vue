<template>
  <aside
      :class="[
      'fixed inset-y-0 left-0 z-40 h-full bg-slate-900 transition-transform duration-300 shrink-0',
      'lg:relative lg:inset-auto lg:z-auto lg:translate-x-0',
      isOpen ? 'translate-x-0 w-64' : '-translate-x-full w-64 lg:translate-x-0',
      isCollapsed ? 'lg:w-20' : 'lg:w-auto lg:min-w-50'
    ]"
  >
    <div class="flex flex-col h-full">
      <div class="h-16 flex items-center justify-center border-b border-slate-700 px-3">
        <AppLogo
            v-if="!isCollapsed"
            size="sm"
            suffix="Plateforme"
            wordmark-class="text-white text-lg"
            accent-class="text-teal-300"
            suffix-class="text-teal-200/80 text-xs ml-1"
        />
        <AppLogo v-else size="sm" :show-wordmark="false" />
      </div>

      <p v-if="!isCollapsed" class="px-4 pt-3 text-[10px] uppercase tracking-wider text-slate-500">
        Console plateforme
      </p>

      <nav class="flex-1 px-3 py-3 space-y-1">
        <router-link
            v-for="item in menuItems"
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-3 py-2.5 rounded-md text-slate-300 hover:bg-white/10 hover:text-white transition-all"
            active-class="bg-teal-700/40 text-white"
            @click="closeOnMobile"
        >
          <component :is="item.icon" class="w-5 h-5 shrink-0" />
          <span v-if="!isCollapsed" class="text-sm font-medium">{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="p-3 border-t border-slate-700 space-y-1">
        <router-link
            to="/plateforme/compte"
            class="w-full flex items-center gap-3 px-3 py-2.5 rounded-md text-slate-300 hover:bg-white/10 hover:text-white transition"
            active-class="bg-teal-700/40 text-white"
            @click="closeOnMobile"
        >
          <UserCircleIcon class="w-5 h-5" />
          <span v-if="!isCollapsed" class="text-sm">Mon compte</span>
        </router-link>
        <button
            type="button"
            class="w-full flex items-center gap-3 px-3 py-2.5 rounded-md text-slate-300 hover:bg-red-500/20 hover:text-red-200 transition"
            @click="logout"
        >
          <ArrowRightOnRectangleIcon class="w-5 h-5" />
          <span v-if="!isCollapsed" class="text-sm">Déconnexion</span>
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/data.js'
import AppLogo from '@/components/AppLogo.vue'
import {
  ChartBarIcon,
  BuildingOffice2Icon,
  ArrowRightOnRectangleIcon,
  UserCircleIcon
} from '@heroicons/vue/24/outline'

defineProps({
  isOpen: { type: Boolean, default: false },
  isCollapsed: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'toggle'])

const router = useRouter()
const authStore = useAuthStore()

const menuItems = computed(() => [
  { path: '/plateforme/dashboard', label: 'Dashboard', icon: ChartBarIcon },
  { path: '/plateforme/entreprises', label: 'Entreprises', icon: BuildingOffice2Icon }
])

const closeOnMobile = () => emit('close')

const logout = () => {
  authStore.logout()
  router.push('/login')
}
</script>
