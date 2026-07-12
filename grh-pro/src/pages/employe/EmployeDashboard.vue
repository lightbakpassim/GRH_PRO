<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-bold text-gray-800">Mon espace</h1>
      <p class="text-gray-500 mt-1">Juin 2026</p>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard
          title="Heures ce mois"
          :value="stats.heuresCeMois + ' h'"
          :icon="ClockIcon"
          icon-bg="blue"
          subtitle="Total heures travaillées"
      />
      <StatCard
          title="Heures Supp"
          :value="stats.heuresSupp + ' h'"
          :icon="BoltIcon"
          icon-bg="orange"
          subtitle="Heures supplémentaires"
      />
      <StatCard
          title="Congés Restants"
          :value="stats.congesRestants + ' j'"
          :icon="CalendarIcon"
          icon-bg="green"
          subtitle="Solde congés 2026"
      />
      <StatCard
          title="Paiement Juin"
          :value="stats.paiementStatus ? 'Effectué' : 'En attente'"
          :icon="CreditCardIcon"
          icon-bg="blue"
          subtitle="Salaire du mois"
      />
    </div>

    <!-- Pointage en cours -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
        <ClockIcon class="w-5 h-5 text-blue-500" />
        Pointage en cours
      </h2>
      <div class="flex items-center justify-between flex-wrap gap-4">
        <div class="flex items-center gap-4">
          <div class="text-center">
            <p class="text-sm text-gray-500">Aujourd'hui</p>
            <p class="text-lg font-semibold">{{ todayDate }}</p>
          </div>
          <div class="w-px h-8 bg-gray-200"></div>
          <div class="text-center">
            <p class="text-sm text-gray-500">Entrée</p>
            <p class="text-lg font-semibold text-green-600">{{ pointageActuel?.entree || '--:--' }}</p>
          </div>
          <div class="text-center">
            <p class="text-sm text-gray-500">Sortie</p>
            <p class="text-lg font-semibold text-red-600">{{ pointageActuel?.sortie || '--:--' }}</p>
          </div>
          <div class="text-center">
            <p class="text-sm text-gray-500">H. Trav</p>
            <p class="text-lg font-semibold">{{ heuresTravaillees || '0' }} h</p>
          </div>
        </div>
        <button
            v-if="!pointageActuel?.sortie"
            @click="pointerSortie"
            class="bg-blue-600 text-white px-6 py-2 rounded-md font-medium hover:bg-blue-700 transition-colors"
        >
          Pointer la Sortie
        </button>
        <button
            v-else
            disabled
            class="bg-gray-400 text-white px-6 py-2 rounded-md font-medium cursor-not-allowed"
        >
          Pointage terminé
        </button>
      </div>
    </div>

    <!-- Mon activité ce mois -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">Mon activité ce mois</h2>
      <DataTable
          :columns="columns"
          :data="mesPointages"
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useDataStore } from '@/stores/dataStore'
import StatCard from '@/components/StatCard.vue'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { ClockIcon, BoltIcon, CalendarIcon, CreditCardIcon } from '@heroicons/vue/24/outline'
import dayjs from 'dayjs'

const dataStore = useDataStore()

const todayDate = ref(dayjs().format('DD/MM/YYYY'))
const currentEmployeId = ref(1) // Light B. id

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
  const today = dayjs().format('YYYY-MM-DD')
  return mesPointages.value.find(p => p.date === today)
})

const heuresTravaillees = computed(() => {
  if (pointageActuel.value && pointageActuel.value.sortie !== '---') {
    return pointageActuel.value.heuresTrav.toFixed(2)
  }
  return null
})

const stats = computed(() => {
  return dataStore.getEmployeStats(currentEmployeId.value)
})

onMounted(() => {
  // Simuler un pointage en cours si nécessaire
  if (!pointageActuel.value) {
    // Ajouter un pointage du jour
    dataStore.ajouterPointage({
      employeId: currentEmployeId.value,
      employeNom: 'Light B.',
      date: dayjs().format('YYYY-MM-DD'),
      entree: '08:02',
      sortie: '---',
      heuresTrav: 0,
      heuresSupp: 0,
      statut: 'En attente'
    })
  }
})

const pointerSortie = () => {
  const today = dayjs().format('YYYY-MM-DD')
  const pointage = mesPointages.value.find(p => p.date === today)
  if (pointage && pointage.sortie === '---') {
    const sortieTime = '17:00'
    const entreeTime = pointage.entree
    // Calcul simple des heures (à améliorer avec une vraie logique)
    pointage.sortie = sortieTime
    pointage.heuresTrav = 7.97
    pointage.heuresSupp = 0
    pointage.statut = 'Validé'
  }
}
</script>