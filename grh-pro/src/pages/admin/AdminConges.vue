<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-800">Gestion des congés</h1>
      <p class="text-gray-500 mt-1">Juin 2026</p>
    </div>

    <!-- Filtres -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
      <div class="flex flex-col sm:flex-row gap-4">
        <div class="flex-1 relative">
          <MagnifyingGlassIcon class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
              v-model="searchQuery"
              type="text"
              placeholder="Rechercher par employé..."
              class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>
        <select v-model="filtreStatut" class="px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 sm:w-40">
          <option value="">Tous statuts</option>
          <option value="Validé">Validé</option>
          <option value="En attente">En attente</option>
          <option value="Refusé">Refusé</option>
        </select>
        <select v-model="filtreMotif" class="px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 sm:w-40">
          <option value="">Tous motifs</option>
          <option value="Vacances">Vacances</option>
          <option value="Formation">Formation</option>
          <option value="Maladie">Maladie</option>
          <option value="Personnel">Personnel</option>
        </select>
      </div>
    </div>

    <!-- Stats rapides -->
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 text-center">
        <p class="text-2xl font-bold text-blue-600">{{ congesStats.total }}</p>
        <p class="text-sm text-gray-500">Total demandes</p>
      </div>
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 text-center">
        <p class="text-2xl font-bold text-orange-600">{{ congesStats.enAttente }}</p>
        <p class="text-sm text-gray-500">En attente</p>
      </div>
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 text-center">
        <p class="text-2xl font-bold text-green-600">{{ congesStats.valides }}</p>
        <p class="text-sm text-gray-500">Validés</p>
      </div>
    </div>

    <!-- Table -->
    <DataTable
        :columns="columns"
        :data="congesFiltres"
        :actions="actions"
    >
      <template #column-statut="{ row }">
        <StatusBadge :status="row.statut" />
      </template>
      <template #column-periode="{ row }">
        {{ formatDate(row.dateDebut) }} → {{ formatDate(row.dateFin) }}
      </template>
    </DataTable>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useDataStore } from '@/stores/dataStore'
import { useToast } from '@/composable/useToast'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { MagnifyingGlassIcon, CheckIcon, XMarkIcon, EyeIcon } from '@heroicons/vue/24/outline'
import dayjs from 'dayjs'

const dataStore = useDataStore()
const { success, error } = useToast()

const searchQuery = ref('')
const filtreStatut = ref('')
const filtreMotif = ref('')

const columns = [
  { key: 'employeNom', label: 'Employé' },
  { key: 'periode', label: 'Période' },
  { key: 'nbJours', label: 'Jours' },
  { key: 'motif', label: 'Motif' },
  { key: 'statut', label: 'Statut', type: 'badge' }
]

const actions = [
  {
    label: 'Voir détails',
    icon: EyeIcon,
    buttonClass: 'text-blue-600 hover:text-blue-800',
    onClick: (row) => voirDetails(row)
  },
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

const congesFiltres = computed(() => {
  let result = [...dataStore.conges]

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(c => c.employeNom.toLowerCase().includes(query))
  }

  if (filtreStatut.value) {
    result = result.filter(c => c.statut === filtreStatut.value)
  }

  if (filtreMotif.value) {
    result = result.filter(c => c.motif === filtreMotif.value)
  }

  return result
})

const congesStats = computed(() => {
  const total = dataStore.conges.length
  const enAttente = dataStore.conges.filter(c => c.statut === 'En attente').length
  const valides = dataStore.conges.filter(c => c.statut === 'Validé').length
  return { total, enAttente, valides }
})

const formatDate = (date) => dayjs(date).format('DD/MM/YY')

const voirDetails = (row) => {
  success(`Détails du congé de ${row.employeNom}: ${row.nbJours} jours du ${formatDate(row.dateDebut)} au ${formatDate(row.dateFin)}`)
}

const validerConge = (row) => {
  dataStore.validerConge(row.id)
  success(`Congé de ${row.employeNom} validé`)
}

const refuserConge = (row) => {
  dataStore.refuserConge(row.id)
  error(`Congé de ${row.employeNom} refusé`)
}
</script>