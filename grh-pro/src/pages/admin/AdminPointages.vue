<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-800">Gestion des pointages</h1>
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
        <input
            type="date"
            v-model="filtreDate"
            class="px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 sm:w-48"
        />
        <button
            @click="resetFilters"
            class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition-colors"
        >
          Réinitialiser
        </button>
      </div>
    </div>

    <!-- Table -->
    <DataTable
        :columns="columns"
        :data="pointagesFiltres"
        :actions="actions"
    >
      <template #column-statut="{ row }">
        <StatusBadge :status="row.statut" />
      </template>
      <template #column-heuresTrav="{ row }">
        {{ row.heuresTrav.toFixed(2) }} h
      </template>
      <template #column-heuresSupp="{ row }">
        <span :class="row.heuresSupp > 0 ? 'text-orange-600 font-medium' : ''">
          {{ row.heuresSupp.toFixed(2) }} h
        </span>
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
import { MagnifyingGlassIcon, CheckIcon, XMarkIcon } from '@heroicons/vue/24/outline'

const dataStore = useDataStore()
const { success, error } = useToast()

const searchQuery = ref('')
const filtreStatut = ref('')
const filtreDate = ref('')

const columns = [
  { key: 'employeNom', label: 'Employé' },
  { key: 'date', label: 'Date', type: 'date' },
  { key: 'entree', label: 'Entrée' },
  { key: 'sortie', label: 'Sortie' },
  { key: 'heuresTrav', label: 'H. Trav' },
  { key: 'heuresSupp', label: 'H. Supp' },
  { key: 'statut', label: 'Statut', type: 'badge' }
]

const actions = [
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

const pointagesFiltres = computed(() => {
  let result = [...dataStore.pointages]

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(p => p.employeNom.toLowerCase().includes(query))
  }

  if (filtreStatut.value) {
    result = result.filter(p => p.statut === filtreStatut.value)
  }

  if (filtreDate.value) {
    result = result.filter(p => p.date === filtreDate.value)
  }

  return result
})

const resetFilters = () => {
  searchQuery.value = ''
  filtreStatut.value = ''
  filtreDate.value = ''
}

const validerPointage = (row) => {
  dataStore.validerPointage(row.id)
  success(`Pointage de ${row.employeNom} validé`)
}

const refuserPointage = (row) => {
  dataStore.refuserPointage(row.id)
  error(`Pointage de ${row.employeNom} refusé`)
}
</script>