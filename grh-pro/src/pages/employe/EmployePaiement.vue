<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-800">Paiement</h1>
      <p class="text-gray-500 mt-1">Mes bulletins de paie</p>
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">
        {{ paiementActuel ? paiementActuel.moisLabel : 'Aucun bulletin ce mois' }}
      </h2>
      <div v-if="paiementActuel" class="grid grid-cols-1 sm:grid-cols-2 gap-6">
        <div class="space-y-3">
          <div class="flex justify-between py-2 border-b border-gray-100">
            <span class="text-gray-600">Salaire de base</span>
            <span class="font-medium">{{ formatCurrency(paiementActuel.salaireBase) }}</span>
          </div>
          <div class="flex justify-between py-2 border-b border-gray-100">
            <span class="text-gray-600">Heures supplémentaires</span>
            <span class="font-medium text-green-600">+ {{ formatCurrency(paiementActuel.heuresSupp) }}</span>
          </div>
          <div class="flex justify-between py-2 border-b border-gray-100">
            <span class="text-gray-600">Retenues</span>
            <span class="font-medium text-red-600">- {{ formatCurrency(paiementActuel.retenues) }}</span>
          </div>
          <div class="flex justify-between py-3 bg-blue-50 rounded-lg px-3 -mx-0 sm:-mx-3">
            <span class="font-semibold text-gray-800">Total Net</span>
            <span class="font-bold text-blue-600 text-lg">{{ formatCurrency(paiementActuel.totalNet) }}</span>
          </div>
        </div>
        <div class="flex flex-col items-center justify-center gap-3 sm:border-l border-gray-200 sm:pl-6 pt-4 sm:pt-0 border-t sm:border-t-0">
          <StatusBadge :status="paiementActuel.statut" />
          <button
              v-if="paiementActuel.statut === 'Effectué'"
              @click="validerPaiement(paiementActuel)"
              :disabled="validatingId === paiementActuel.idPaiement"
              class="mt-2 bg-primary text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-primary-dark disabled:opacity-50"
          >
            Valider la réception
          </button>
          <p v-else-if="paiementActuel.statut === 'Validé'" class="text-xs text-success text-center">
            Réception confirmée
          </p>
          <p v-else class="text-xs text-gray-400 text-center">
            En attente du versement admin
          </p>
        </div>
      </div>
      <p v-else class="text-gray-500 text-sm">Aucun paiement disponible pour le mois en cours.</p>
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">Historique des paiements</h2>
      <DataTable :columns="columns" :data="mesPaiements">
        <template #column-statut="{ row }">
          <StatusBadge :status="row.statut" />
        </template>
        <template #column-moisLabel="{ row }">
          {{ row.moisLabel }}
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
        <template #column-actions="{ row }">
          <button
              v-if="row.statut === 'Effectué'"
              @click="validerPaiement(row)"
              :disabled="validatingId === row.idPaiement"
              class="text-sm text-primary font-medium hover:underline disabled:opacity-50"
          >
            Valider
          </button>
          <span v-else-if="row.statut === 'Validé'" class="text-xs text-success">Validé</span>
          <span v-else class="text-xs text-gray-400">—</span>
        </template>
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { paiementsAPI } from '@/API/paiements'
import { mapPaiement } from '@/utils/mappers'
import { useToast } from '@/composable/useToast'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'

const { success, error } = useToast()
const mesPaiements = ref([])
const validatingId = ref(null)

const columns = [
  { key: 'moisLabel', label: 'Mois' },
  { key: 'salaireBase', label: 'Salaire de base', hideOnMobile: true },
  { key: 'heuresSupp', label: 'Heures Supp', hideOnMobile: true },
  { key: 'totalNet', label: 'Total Net' },
  { key: 'statut', label: 'Statut', type: 'badge' },
  { key: 'actions', label: 'Action' }
]

const paiementActuel = computed(() => {
  const now = new Date()
  return mesPaiements.value.find(p => p.mois === now.getMonth() + 1 && p.annee === now.getFullYear())
})

const formatCurrency = (value) => {
  if (!value && value !== 0) return '0 FCFA'
  return new Intl.NumberFormat('fr-FR').format(value) + ' FCFA'
}

const charger = async () => {
  try {
    const { data } = await paiementsAPI.getMesBulletins()
    mesPaiements.value = data
      .map(mapPaiement)
      .sort((a, b) => (b.annee - a.annee) || (b.mois - a.mois))
  } catch (err) {
    error(err.response?.data?.message || 'Erreur chargement paiements')
  }
}

const validerPaiement = async (paiement) => {
  validatingId.value = paiement.idPaiement
  try {
    const { data } = await paiementsAPI.valider(paiement.idPaiement)
    const mapped = mapPaiement(data)
    const idx = mesPaiements.value.findIndex(p => p.idPaiement === paiement.idPaiement)
    if (idx !== -1) mesPaiements.value[idx] = mapped
    success('Paiement validé — le DG en est informé dans l’historique')
  } catch (err) {
    error(err.response?.data?.message || 'Erreur lors de la validation')
  } finally {
    validatingId.value = null
  }
}

onMounted(charger)
</script>
