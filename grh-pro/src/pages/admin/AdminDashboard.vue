<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">Tableau de bord</h1>
        <p class="text-gray-500 mt-1">Juin 2026</p>
      </div>
      <button
          @click="refreshData"
          class="bg-gray-200 text-gray-700 px-4 py-2 rounded-md font-medium transition-all duration-200 hover:bg-gray-300 active:scale-95"
      >
        <ArrowPathIcon class="w-4 h-4 inline mr-1" />
        Actualiser
      </button>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard
          title="Total employés"
          :value="stats.totalEmployes"
          :icon="UsersIcon"
          icon-bg="blue"
          subtitle="Employés actifs"
          @click="goToEmployes"
      />
      <StatCard
          title="Heures supp."
          :value="stats.heuresSupp + ' h'"
          :icon="ClockIcon"
          icon-bg="orange"
          subtitle="En attente de validation"
          @click="scrollToHeuresSupp"
      />
      <StatCard
          title="Congés à traiter"
          :value="stats.congesAttente"
          :icon="CalendarIcon"
          icon-bg="blue"
          subtitle="Demandes en attente"
          @click="scrollToConges"
      />
      <StatCard
          title="Paiement juin"
          :value="stats.paiementMois"
          :icon="CreditCardIcon"
          icon-bg="green"
          subtitle="Paiements effectués"
      />
    </div>

    <!-- Heures supplémentaires en attente -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
        <ClockIcon class="w-5 h-5 text-orange-500" />
        Heures supplémentaires - En attente
      </h2>
      <DataTable
          :columns="heuresSuppColumns"
          :data="heuresSuppAttente"
          :actions="heuresSuppActions"
      />
    </div>

    <!-- Demandes de congé en attente -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
        <CalendarIcon class="w-5 h-5 text-blue-500" />
        Demandes de congé - En attente
      </h2>
      <DataTable
          :columns="congesColumns"
          :data="congesAttente"
          :actions="congesActions"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDataStore } from '@/stores/dataStore'
import { useToast } from '@/composable/useToast'
import StatCard from '@/components/StatCard.vue'
import DataTable from '@/components/DataTable.vue'
import {
  UsersIcon,
  ClockIcon,
  CalendarIcon,
  CreditCardIcon,
  ArrowPathIcon,
  CheckIcon,
  XMarkIcon
} from '@heroicons/vue/24/outline'
import dayjs from 'dayjs'

const router = useRouter()
const dataStore = useDataStore()
const { success, error } = useToast()

const stats = ref({
  totalEmployes: 0,
  heuresSupp: 0,
  congesAttente: 0,
  paiementMois: 0
})

const heuresSuppColumns = [
  { key: 'employeNom', label: 'Employé' },
  { key: 'date', label: 'Date', type: 'date' },
  { key: 'heuresTrav', label: 'H. STD' },
  { key: 'heuresSupp', label: 'H. SUPP' }
]

const congesColumns = [
  { key: 'employeNom', label: 'Employé' },
  { key: 'dateDebut', label: 'Début', type: 'date' },
  { key: 'dateFin', label: 'Fin', type: 'date' },
  { key: 'nbJours', label: 'Jours' },
  { key: 'motif', label: 'Motif' }
]

const heuresSuppActions = [
  {
    label: 'Valider',
    icon: CheckIcon,
    buttonClass: 'text-green-600 hover:text-green-800',
    onClick: (row) => validerPointage(row)
  },
  {
    label: 'Refuser',
    icon: XMarkIcon,
    buttonClass: 'text-red-600 hover:text-red-800',
    onClick: (row) => refuserPointage(row)
  }
]

const congesActions = [
  {
    label: 'Valider',
    icon: CheckIcon,
    buttonClass: 'text-green-600 hover:text-green-800',
    onClick: (row) => validerConge(row)
  },
  {
    label: 'Refuser',
    icon: XMarkIcon,
    buttonClass: 'text-red-600 hover:text-red-800',
    onClick: (row) => refuserConge(row)
  }
]

const heuresSuppAttente = computed(() =>
    dataStore.pointages.filter(p => p.statut === 'En attente')
)

const congesAttente = computed(() =>
    dataStore.conges.filter(c => c.statut === 'En attente')
)

const refreshData = () => {
  stats.value = dataStore.getAdminStats()
  success('Données actualisées')
}

const goToEmployes = () => {
  router.push('/admin/employes')
}

const scrollToHeuresSupp = () => {
  const element = document.querySelector('.bg-white:has(.text-orange-500)')
  if (element) element.scrollIntoView({ behavior: 'smooth' })
}

const scrollToConges = () => {
  const elements = document.querySelectorAll('.bg-white')
  if (elements[2]) elements[2].scrollIntoView({ behavior: 'smooth' })
}

const validerPointage = (row) => {
  dataStore.validerPointage(row.id)
  success(`Pointage de ${row.employeNom} validé`)
}

const refuserPointage = (row) => {
  dataStore.refuserPointage(row.id)
  error(`Pointage de ${row.employeNom} refusé`)
}

const validerConge = (row) => {
  dataStore.validerConge(row.id)
  success(`Congé de ${row.employeNom} validé`)
}

const refuserConge = (row) => {
  dataStore.refuserConge(row.id)
  error(`Congé de ${row.employeNom} refusé`)
}

onMounted(() => {
  stats.value = dataStore.getAdminStats()
})
</script>