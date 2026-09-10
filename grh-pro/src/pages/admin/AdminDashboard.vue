<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">Tableau de bord</h1>
        <p class="text-gray-500 mt-1">
          Vue d'ensemble RH
          <span v-if="entreprise?.genereA" class="text-xs text-emerald-600 ml-2">MAJ {{ entreprise.genereA }}</span>
        </p>
      </div>
      <button
          type="button"
          @click="refreshData"
          class="w-full sm:w-auto bg-gray-200 text-gray-700 px-4 py-2.5 min-h-11 rounded-md font-medium hover:bg-gray-300 inline-flex items-center justify-center gap-1"
      >
        <ArrowPathIcon class="w-4 h-4" />
        Actualiser
      </button>
    </div>

    <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
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
      />
      <StatCard
          title="Congés à traiter"
          :value="stats.congesAttente"
          :icon="CalendarIcon"
          icon-bg="blue"
          subtitle="Demandes en attente"
      />
      <StatCard
          title="Paiements mois"
          :value="stats.paiementMois"
          :icon="CreditCardIcon"
          icon-bg="green"
          subtitle="Effectués / validés"
      />
    </div>

    <!-- Graphiques -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4 sm:gap-6">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-5">
        <h2 class="text-sm font-semibold text-gray-800 mb-1">Effectifs</h2>
        <p class="text-xs text-gray-400 mb-3">Actifs vs inactifs</p>
        <DonutChart
            :labels="['Actifs', 'Inactifs']"
            :values="[entreprise?.employesActifs || 0, entreprise?.employesInactifs || 0]"
            :colors="['#1a7a7a', '#94a3b8']"
            :height="240"
        />
      </div>

      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-5">
        <h2 class="text-sm font-semibold text-gray-800 mb-1">Paiements du mois</h2>
        <p class="text-xs text-gray-400 mb-3">Répartition des statuts</p>
        <DonutChart
            :labels="['En attente', 'Effectués', 'Validés']"
            :values="paiementDonutValues"
            :colors="['#f5a623', '#1a7a7a', '#4caf7d']"
            :height="240"
        />
      </div>

      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-5 lg:col-span-1">
        <h2 class="text-sm font-semibold text-gray-800 mb-1">Par département</h2>
        <p class="text-xs text-gray-400 mb-3">Employés actifs</p>
        <DonutChart
            :labels="deptLabels"
            :values="deptValues"
            :height="240"
        />
      </div>
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-5">
      <h2 class="text-sm font-semibold text-gray-800 mb-1">Activité des 7 derniers jours</h2>
      <p class="text-xs text-gray-400 mb-3">Pointages et heures supplémentaires</p>
      <LineChart
          :labels="serieLabels"
          :datasets="serieDatasets"
          y-title="Quantité"
          :height="280"
      />
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
        <ClockIcon class="w-5 h-5 text-orange-500" />
        Heures supplémentaires - En attente
      </h2>
      <DataTable :columns="heuresSuppColumns" :data="heuresSuppAttente" :actions="heuresSuppActions" />
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
        <CalendarIcon class="w-5 h-5 text-blue-500" />
        Demandes de congé - En attente
      </h2>
      <DataTable :columns="congesColumns" :data="congesAttente" :actions="congesActions" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { pointagesAPI } from '@/API/pointages'
import { congesAPI } from '@/API/conges'
import { dashboardAPI } from '@/API/dashboard'
import { mapPointage, mapConge } from '@/utils/mappers'
import { unwrapList } from '@/utils/api'
import { useToast } from '@/composable/useToast'
import StatCard from '@/components/StatCard.vue'
import DataTable from '@/components/DataTable.vue'
import DonutChart from '@/components/charts/DonutChart.vue'
import LineChart from '@/components/charts/LineChart.vue'
import {
  UsersIcon, ClockIcon, CalendarIcon, CreditCardIcon,
  ArrowPathIcon, CheckIcon, XMarkIcon
} from '@heroicons/vue/24/outline'

const router = useRouter()
const { success, error } = useToast()

const entreprise = ref(null)
const stats = ref({ totalEmployes: 0, heuresSupp: 0, congesAttente: 0, paiementMois: 0 })
const heuresSuppAttente = ref([])
const congesAttente = ref([])

const heuresSuppColumns = [
  { key: 'employeNom', label: 'Employé' },
  { key: 'date', label: 'Date', type: 'date' },
  { key: 'heuresTrav', label: 'H. STD', hideOnMobile: true },
  { key: 'heuresSupp', label: 'H. SUPP' }
]

const congesColumns = [
  { key: 'employeNom', label: 'Employé' },
  { key: 'dateDebut', label: 'Début', type: 'date', hideOnMobile: true },
  { key: 'dateFin', label: 'Fin', type: 'date', hideOnMobile: true },
  { key: 'nbJours', label: 'Jours' },
  { key: 'motif', label: 'Motif', hideOnMobile: true }
]

const heuresSuppActions = [
  { label: 'Valider', icon: CheckIcon, buttonClass: 'text-green-600 hover:text-green-800', onClick: (row) => validerPointage(row) },
  { label: 'Refuser', icon: XMarkIcon, buttonClass: 'text-red-600 hover:text-red-800', onClick: (row) => refuserPointage(row) }
]

const congesActions = [
  { label: 'Valider', icon: CheckIcon, buttonClass: 'text-green-600 hover:text-green-800', onClick: (row) => validerConge(row) },
  { label: 'Refuser', icon: XMarkIcon, buttonClass: 'text-red-600 hover:text-red-800', onClick: (row) => refuserConge(row) }
]

const paiementDonutValues = computed(() => {
  const d = entreprise.value || {}
  const effectues = Math.max(0, (d.paiementsMoisEnCours || 0) - (d.paiementsValides || 0))
  return [d.paiementsEnAttente || 0, effectues, d.paiementsValides || 0]
})

const deptLabels = computed(() =>
  (entreprise.value?.parDepartement || []).map(d => d.nomDepartement)
)
const deptValues = computed(() =>
  (entreprise.value?.parDepartement || []).map(d => d.effectif)
)

const serieLabels = computed(() =>
  (entreprise.value?.pointages7j || []).map(p => p.label)
)
const serieDatasets = computed(() => [
  {
    label: 'Pointages',
    data: (entreprise.value?.pointages7j || []).map(p => p.valeur),
    color: '#1a7a7a'
  },
  {
    label: 'Heures supp.',
    data: (entreprise.value?.heuresSupp7j || []).map(p => p.valeur),
    color: '#f5a623'
  }
])

const refreshData = async (showToast = true) => {
  try {
    const [dashRes, suiviRes, congeRes] = await Promise.all([
      dashboardAPI.getEntreprise(),
      pointagesAPI.getEnAttente(),
      congesAPI.getEnAttente()
    ])

    entreprise.value = dashRes.data
    const suivis = unwrapList(suiviRes.data).map(mapPointage)
    const conges = unwrapList(congeRes.data).map(mapConge)

    heuresSuppAttente.value = suivis
    congesAttente.value = conges

    stats.value = {
      totalEmployes: dashRes.data.employesActifs ?? 0,
      heuresSupp: Number(suivis.reduce((s, p) => s + (p.heuresSupp || 0), 0).toFixed(1)),
      congesAttente: conges.length,
      paiementMois: dashRes.data.paiementsMoisEnCours ?? 0
    }
    if (showToast) success('Données actualisées')
  } catch (err) {
    error(err.response?.data?.message || 'Erreur chargement dashboard')
  }
}

const goToEmployes = () => router.push('/admin/employes')

const validerPointage = async (row) => {
  try {
    await pointagesAPI.validate(row.id)
    success(`Pointage de ${row.employeNom} validé`)
    await refreshData(false)
  } catch (err) {
    error(err.response?.data?.message || 'Erreur')
  }
}

const refuserPointage = async (row) => {
  try {
    await pointagesAPI.reject(row.id)
    error(`Pointage de ${row.employeNom} refusé`)
    await refreshData(false)
  } catch (err) {
    error(err.response?.data?.message || 'Erreur')
  }
}

const validerConge = async (row) => {
  try {
    await congesAPI.validate(row.id)
    success(`Congé de ${row.employeNom} validé`)
    await refreshData(false)
  } catch (err) {
    error(err.response?.data?.message || 'Erreur')
  }
}

const refuserConge = async (row) => {
  try {
    await congesAPI.reject(row.id)
    error(`Congé de ${row.employeNom} refusé`)
    await refreshData(false)
  } catch (err) {
    error(err.response?.data?.message || 'Erreur')
  }
}

onMounted(() => refreshData(false))
</script>
