<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-800">Paiement</h1>
      <p class="text-gray-500 mt-1">Juin 2026</p>
    </div>

    <!-- Paiement du mois -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">Paiement juin 2026</h2>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
        <div class="space-y-3">
          <div class="flex justify-between py-2 border-b border-gray-100">
            <span class="text-gray-600">Salaire de base</span>
            <span class="font-medium">{{ formatCurrency(paiementActuel?.salaireBase || 150000) }}</span>
          </div>
          <div class="flex justify-between py-2 border-b border-gray-100">
            <span class="text-gray-600">Heures supplémentaires</span>
            <span class="font-medium text-green-600">+ {{ formatCurrency(paiementActuel?.heuresSupp || 15336) }}</span>
          </div>
          <div class="flex justify-between py-2 border-b border-gray-100">
            <span class="text-gray-600">Retenues</span>
            <span class="font-medium text-red-600">- {{ formatCurrency(retenues) }}</span>
          </div>
          <div class="flex justify-between py-3 bg-blue-50 rounded-lg px-3 -mx-3">
            <span class="font-semibold text-gray-800">Total Net</span>
            <span class="font-bold text-blue-600 text-lg">{{ formatCurrency(paiementActuel?.totalNet || 165336) }}</span>
          </div>
        </div>
        <div class="flex flex-col items-center justify-center gap-3 border-l border-gray-200 pl-6">
          <StatusBadge :status="paiementActuel?.statut || 'Effectué'" />
          <button
              @click="telechargerBulletin"
              class="bg-blue-600 text-white px-4 py-2 rounded-md font-medium hover:bg-blue-700 transition-colors flex items-center gap-2"
          >
            <DocumentArrowDownIcon class="w-5 h-5" />
            Télécharger le bulletin
          </button>
        </div>
      </div>
    </div>

    <!-- Historique des paiements -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">Historique des paiements</h2>
      <DataTable
          :columns="columns"
          :data="mesPaiements"
      >
        <template #column-statut="{ row }">
          <StatusBadge :status="row.statut" />
        </template>
        <template #column-salaireBase="{ row }">
          {{ formatCurrency(row.salaireBase) }}
        </template>
        <template #column-heuresSupp="{ row }">
          {{ formatCurrency(row.heuresSupp) }}
        </template>
        <template #column-totalNet="{ row }">
          <span class="font-semibold">{{ formatCurrency(row.totalNet) }}</span>
        </template>
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useDataStore } from '@/stores/dataStore'
import { useToast } from '@/composable/useToast'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { DocumentArrowDownIcon } from '@heroicons/vue/24/outline'

const dataStore = useDataStore()
const { success } = useToast()

const currentEmployeId = ref(1) // Light B.

const columns = [
  { key: 'mois', label: 'Mois' },
  { key: 'salaireBase', label: 'Salaire de base' },
  { key: 'heuresSupp', label: 'Heures Supp' },
  { key: 'totalNet', label: 'Total Net' },
  { key: 'statut', label: 'Statut', type: 'badge' }
]

const mesPaiements = computed(() => {
  return dataStore.paiements
      .filter(p => p.employeId === currentEmployeId.value)
      .sort((a, b) => {
        const moisOrdre = { 'juin 2026': 6, 'mai 2026': 5, 'avril 2026': 4 }
        return moisOrdre[b.mois] - moisOrdre[a.mois]
      })
})

const paiementActuel = computed(() => {
  return mesPaiements.value.find(p => p.mois === 'juin 2026')
})

const retenues = computed(() => {
  // Calcul des retenues (exemple: 5% du salaire de base)
  const base = paiementActuel.value?.salaireBase || 150000
  return Math.round(base * 0.05)
})

const formatCurrency = (value) => {
  if (!value) return '0 FCFA'
  return new Intl.NumberFormat('fr-FR').format(value) + ' FCFA'
}

const telechargerBulletin = () => {
  success('Téléchargement du bulletin de paie en cours...')
}
</script>