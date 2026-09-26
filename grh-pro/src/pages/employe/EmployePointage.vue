<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-800">Pointages</h1>
      <p class="text-gray-500 mt-1">Juin 2026</p>
    </div>

    <!-- Pointage d'aujourd'hui -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">Pointage du jour</h2>
      <div class="flex flex-col sm:flex-row items-center justify-between gap-6">
        <div class="flex items-center gap-6">
          <div class="text-center">
            <p class="text-sm text-gray-500">Date</p>
            <p class="text-xl font-bold text-gray-800">{{ todayDate }}</p>
          </div>
          <div class="w-px h-10 bg-gray-200"></div>
          <div class="text-center">
            <p class="text-sm text-gray-500">Entrée</p>
            <p class="text-2xl font-bold text-green-600">{{ pointageActuel?.entree || '--:--' }}</p>
          </div>
          <div class="text-center">
            <p class="text-sm text-gray-500">Sortie</p>
            <p class="text-2xl font-bold" :class="pointageActuel?.sortie !== '---' ? 'text-red-600' : 'text-gray-400'">
              {{ pointageActuel?.sortie || '--:--' }}
            </p>
          </div>
          <div class="text-center">
            <p class="text-sm text-gray-500">H. travaillées</p>
            <p class="text-xl font-bold text-blue-600">{{ pointageActuel?.heuresTrav?.toFixed(2) || '0' }} h</p>
          </div>
        </div>
        <div class="flex gap-3">
          <button
              v-if="!pointageActuel?.entree || pointageActuel.entree === '---'"
              @click="pointerEntree"
              class="bg-green-600 text-white px-6 py-2 rounded-md font-medium hover:bg-green-700 transition-colors"
          >
            Pointer l'entrée
          </button>
          <button
              v-else-if="!pointageActuel?.sortie || pointageActuel.sortie === '---'"
              @click="pointerSortie"
              class="bg-red-600 text-white px-6 py-2 rounded-md font-medium hover:bg-red-700 transition-colors"
          >
            Pointer la sortie
          </button>
          <button
              v-else
              disabled
              class="bg-gray-400 text-white px-6 py-2 rounded-md font-medium cursor-not-allowed"
          >
            Journée complétée
          </button>
        </div>
      </div>
    </div>

    <!-- Historique -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">Mon historique de pointage</h2>
      <DataTable
          :columns="columns"
          :data="mesPointages"
      >
        <template #column-statut="{ row }">
          <StatusBadge :status="row.statut" />
        </template>
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useDataStore } from '@/stores/dataStore'
import { useToast } from '@/composable/useToast'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import dayjs from 'dayjs'

const dataStore = useDataStore()
const { success } = useToast()

const currentEmployeId = ref(1) // Light B.

const todayDate = ref(dayjs().format('DD/MM/YYYY'))
const todayDateISO = ref(dayjs().format('YYYY-MM-DD'))

const columns = [
  { key: 'date', label: 'Date', type: 'date' },
  { key: 'entree', label: 'Entrée' },
  { key: 'sortie', label: 'Sortie' },
  { key: 'heuresTrav', label: 'H. Trav' },
  { key: 'heuresSupp', label: 'H. Supp' },
  { key: 'statut', label: 'Statut', type: 'badge' }
]

const mesPointages = computed(() => {
  return dataStore.pointages
      .filter(p => p.employeId === currentEmployeId.value)
      .sort((a, b) => new Date(b.date) - new Date(a.date))
})

const pointageActuel = computed(() => {
  return mesPointages.value.find(p => p.date === todayDateISO.value)
})

const pointerEntree = () => {
  const now = dayjs()
  const entreeTime = now.format('HH:mm')

  dataStore.ajouterPointage({
    id: Date.now(),
    employeId: currentEmployeId.value,
    employeNom: 'Light B.',
    date: todayDateISO.value,
    entree: entreeTime,
    sortie: '---',
    heuresTrav: 0,
    heuresSupp: 0,
    statut: 'En attente'
  })

  success(`Entrée pointée à ${entreeTime}`)
}

const pointerSortie = () => {
  const now = dayjs()
  const sortieTime = now.format('HH:mm')
  const entreeTime = pointageActuel.value.entree

  // Calcul des heures travaillées
  const entree = dayjs(`${todayDateISO.value} ${entreeTime}`)
  const sortie = dayjs(`${todayDateISO.value} ${sortieTime}`)
  let heuresTrav = sortie.diff(entree, 'hour', true)

  let heuresSupp = 0
  if (heuresTrav > 8) {
    heuresSupp = heuresTrav - 8
    heuresTrav = 8
  }

  const pointage = mesPointages.value.find(p => p.date === todayDateISO.value)
  if (pointage) {
    pointage.sortie = sortieTime
    pointage.heuresTrav = parseFloat(heuresTrav.toFixed(2))
    pointage.heuresSupp = parseFloat(heuresSupp.toFixed(2))
    pointage.statut = heuresSupp > 0 ? 'En attente' : 'Validé'
  }

  success(`Sortie pointée à ${sortieTime} - ${heuresTrav.toFixed(2)}h travaillées${heuresSupp > 0 ? `, dont ${heuresSupp.toFixed(2)}h supp` : ''}`)
}

onMounted(() => {
  if (!pointageActuel.value) {
    // Optionnel: créer un pointage vierge pour aujourd'hui
  }
})
</script>