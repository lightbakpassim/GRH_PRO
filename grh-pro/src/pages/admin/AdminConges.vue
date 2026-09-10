<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-800">Gestion des congés</h1>
      <p class="text-gray-500 mt-1">Validation des demandes</p>
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
      <div class="flex flex-col sm:flex-row gap-4">
        <div class="flex-1 relative">
          <MagnifyingGlassIcon class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
              v-model="searchQuery"
              type="text"
              placeholder="Rechercher par employé..."
              class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <select v-model="filtreStatut" class="px-4 py-2 border border-gray-300 rounded-md sm:w-40">
          <option value="">Tous statuts</option>
          <option value="En attente">En attente</option>
          <option value="Approuvée">Approuvée</option>
          <option value="Refusée">Refusée</option>
        </select>
      </div>
    </div>

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
        <p class="text-sm text-gray-500">Approuvées</p>
      </div>
    </div>

    <DataTable :columns="columns" :data="congesFiltres" :actions="actions">
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
import { ref, computed, onMounted } from 'vue'
import { congesAPI } from '@/API/conges'
import { mapConge } from '@/utils/mappers'
import { unwrapList, ALL_PAGE } from '@/utils/api'
import { useToast } from '@/composable/useToast'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { MagnifyingGlassIcon, CheckIcon, XMarkIcon, EyeIcon } from '@heroicons/vue/24/outline'
import dayjs from 'dayjs'

const { success, error } = useToast()
const conges = ref([])
const searchQuery = ref('')
const filtreStatut = ref('')

const columns = [
  { key: 'employeNom', label: 'Employé' },
  { key: 'periode', label: 'Période', hideOnMobile: true },
  { key: 'nbJours', label: 'Jours' },
  { key: 'motif', label: 'Motif', hideOnMobile: true, wrap: true },
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
  let result = [...conges.value]
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(c => c.employeNom?.toLowerCase().includes(q))
  }
  if (filtreStatut.value) {
    result = result.filter(c => c.statut === filtreStatut.value)
  }
  return result
})

const congesStats = computed(() => ({
  total: conges.value.length,
  enAttente: conges.value.filter(c => c.statut === 'En attente').length,
  valides: conges.value.filter(c => c.statut === 'Approuvée').length
}))

const formatDate = (date) => dayjs(date).format('DD/MM/YY')

const charger = async () => {
  try {
    const { data } = await congesAPI.getAll(ALL_PAGE)
    conges.value = unwrapList(data).map(mapConge)
  } catch (err) {
    error(err.response?.data?.message || 'Erreur chargement congés')
  }
}

const voirDetails = (row) => {
  success(`${row.employeNom}: ${row.nbJours} j du ${formatDate(row.dateDebut)} au ${formatDate(row.dateFin)}`)
}

const validerConge = async (row) => {
  try {
    await congesAPI.validate(row.id)
    success(`Congé de ${row.employeNom} approuvé`)
    await charger()
  } catch (err) {
    error(err.response?.data?.message || 'Erreur validation')
  }
}

const refuserConge = async (row) => {
  try {
    await congesAPI.reject(row.id)
    error(`Congé de ${row.employeNom} refusé`)
    await charger()
  } catch (err) {
    error(err.response?.data?.message || 'Erreur refus')
  }
}

onMounted(charger)
</script>
