<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">Vue entreprise</h1>
        <p class="text-gray-500 mt-1">
          Tableau de bord dynamique
          <span v-if="data?.genereA" class="text-xs text-emerald-600 ml-2">MAJ {{ data.genereA }}</span>
        </p>
      </div>
      <div class="flex flex-wrap items-center gap-3 text-sm text-gray-500">
        <span class="inline-flex items-center gap-1">
          <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
          Temps réel ({{ refreshSec }}s)
        </span>
        <button
            type="button"
            @click="charger"
            class="px-3 py-2 min-h-11 bg-slate-200 rounded-md hover:bg-slate-300"
        >
          Actualiser
        </button>
      </div>
    </div>

    <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
      <div v-for="card in kpiCards" :key="card.title" class="bg-white rounded-lg border p-3 sm:p-4 shadow-sm">
        <p class="text-xs uppercase tracking-wide text-gray-500 truncate">{{ card.title }}</p>
        <p class="text-xl sm:text-2xl font-bold text-slate-900 mt-1 truncate">{{ card.value }}</p>
        <p class="text-xs text-gray-400 mt-1 truncate">{{ card.sub }}</p>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6">
      <div class="bg-white rounded-lg border shadow-sm p-4 sm:p-5">
        <h2 class="text-sm font-semibold text-gray-800 mb-1">Effectifs par département</h2>
        <p class="text-xs text-gray-400 mb-3">Répartition des collaborateurs actifs</p>
        <DonutChart
            :labels="deptLabels"
            :values="deptValues"
            :height="280"
        />
      </div>

      <div class="bg-white rounded-lg border shadow-sm p-4 sm:p-5">
        <h2 class="text-sm font-semibold text-gray-800 mb-1">Paiements du mois</h2>
        <p class="text-xs text-gray-400 mb-3">Statuts des bulletins</p>
        <DonutChart
            :labels="['En attente', 'Effectués', 'Validés']"
            :values="paiementValues"
            :colors="['#f5a623', '#1a7a7a', '#4caf7d']"
            :height="280"
        />
      </div>
    </div>

    <div class="bg-white rounded-lg border shadow-sm p-4 sm:p-5">
      <h2 class="text-sm font-semibold text-gray-800 mb-1">Tendance 7 jours</h2>
      <p class="text-xs text-gray-400 mb-3">Pointages quotidiens et volume d’heures supplémentaires</p>
      <LineChart
          :labels="serieLabels"
          :datasets="serieDatasets"
          y-title="Volume"
          :height="300"
      />
    </div>

    <div class="grid lg:grid-cols-2 gap-4 sm:gap-6">
      <div class="bg-white rounded-lg border shadow-sm p-4">
        <h2 class="font-semibold text-gray-800 mb-3">Pointages du jour</h2>
        <div v-if="!data?.pointagesRecents?.length" class="text-sm text-gray-400">Aucun pointage aujourd’hui</div>
        <ul class="space-y-2 max-h-64 overflow-y-auto">
          <li v-for="(p, i) in data?.pointagesRecents || []" :key="i" class="text-sm border-b border-gray-50 pb-2">
            <div class="font-medium">{{ p.label }}</div>
            <div class="text-gray-500">{{ p.detail }} · {{ p.statut }}</div>
          </li>
        </ul>
      </div>

      <div class="bg-white rounded-lg border shadow-sm p-4">
        <h2 class="font-semibold text-gray-800 mb-3">Congés en attente</h2>
        <div v-if="!data?.congesRecents?.length" class="text-sm text-gray-400">Aucune demande en attente</div>
        <div class="grid sm:grid-cols-1 gap-3 max-h-64 overflow-y-auto">
          <div v-for="(c, i) in data?.congesRecents || []" :key="i" class="text-sm border rounded-md p-3">
            <div class="font-medium">{{ c.label }}</div>
            <div class="text-gray-500">{{ c.detail }}</div>
            <div class="text-amber-600 text-xs mt-1">{{ c.statut }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { dashboardAPI } from '@/API/dashboard'
import { useToast } from '@/composable/useToast'
import DonutChart from '@/components/charts/DonutChart.vue'
import LineChart from '@/components/charts/LineChart.vue'

const { error } = useToast()
const data = ref(null)
const refreshSec = 20
let timer = null

const formatMoney = (n) =>
  n == null ? '—' : new Intl.NumberFormat('fr-FR').format(Number(n)) + ' FCFA'

const kpiCards = computed(() => {
  const d = data.value || {}
  return [
    { title: 'Employés actifs', value: d.employesActifs ?? '—', sub: `${d.employesInactifs ?? 0} inactifs` },
    { title: 'Départements', value: d.totalDepartements ?? '—', sub: 'Organisation' },
    { title: 'Pointages jour', value: d.pointagesAujourdhui ?? '—', sub: 'Entrées / sorties' },
    { title: 'HS en attente', value: d.heuresSuppEnAttente ?? '—', sub: 'À valider par RH' },
    { title: 'Congés en attente', value: d.congesEnAttente ?? '—', sub: 'Demandes RH' },
    { title: 'Paiements mois', value: d.paiementsMoisEnCours ?? '—', sub: `${d.paiementsEnAttente ?? 0} en attente` },
    { title: 'Masse salariale', value: formatMoney(d.masseSalarialeActifs), sub: 'Employés actifs' },
    { title: 'Rapports non lus', value: d.rapportsNonLus ?? '—', sub: 'Inbox DG' }
  ]
})

const deptLabels = computed(() => (data.value?.parDepartement || []).map(d => d.nomDepartement))
const deptValues = computed(() => (data.value?.parDepartement || []).map(d => d.effectif))

const paiementValues = computed(() => {
  const d = data.value || {}
  const effectues = Math.max(0, (d.paiementsMoisEnCours || 0) - (d.paiementsValides || 0))
  return [d.paiementsEnAttente || 0, effectues, d.paiementsValides || 0]
})

const serieLabels = computed(() => (data.value?.pointages7j || []).map(p => p.label))
const serieDatasets = computed(() => [
  {
    label: 'Pointages',
    data: (data.value?.pointages7j || []).map(p => p.valeur),
    color: '#1a7a7a'
  },
  {
    label: 'Heures supp.',
    data: (data.value?.heuresSupp7j || []).map(p => p.valeur),
    color: '#f5a623'
  }
])

const charger = async () => {
  try {
    const res = await dashboardAPI.getEntreprise()
    data.value = res.data
  } catch (err) {
    error(err.response?.data?.message || 'Erreur dashboard')
  }
}

onMounted(() => {
  charger()
  timer = setInterval(charger, refreshSec * 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>
