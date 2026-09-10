<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
      <div>
        <h1 class="text-2xl font-semibold text-slate-900">Vue des entreprises</h1>
        <p class="text-slate-500 text-sm mt-1">
          Santé et activité de chaque organisation
          <span v-if="stats.genereA" class="text-teal-700 ml-1">· MAJ {{ stats.genereA }}</span>
        </p>
      </div>
      <div class="flex items-center gap-3 text-sm text-slate-500">
        <span class="inline-flex items-center gap-1.5">
          <span class="w-2 h-2 rounded-full bg-teal-600 animate-pulse" />
          Auto {{ refreshSec }}s
        </span>
        <button
            type="button"
            class="px-3 py-2 min-h-10 bg-slate-100 hover:bg-slate-200 rounded-lg text-slate-700"
            @click="charger"
        >
          Actualiser
        </button>
      </div>
    </div>

    <div class="grid grid-cols-2 lg:grid-cols-4 xl:grid-cols-7 gap-3">
      <div v-for="card in cards" :key="card.label" class="bg-white border border-slate-200 rounded-xl p-4">
        <p class="text-[11px] uppercase tracking-wide text-slate-500">{{ card.label }}</p>
        <p class="text-2xl font-semibold text-slate-900 mt-1">{{ card.value }}</p>
      </div>
    </div>

    <div v-if="loading" class="text-center text-slate-400 py-12">Chargement…</div>

    <template v-else>
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div class="bg-white border border-slate-200 rounded-xl p-4 sm:p-5">
          <h2 class="text-sm font-semibold text-slate-800">Statut des boîtes</h2>
          <p class="text-xs text-slate-400 mb-3">Actives vs suspendues</p>
          <DonutChart
              :labels="['Actives', 'Suspendues']"
              :values="[stats.entreprisesActives || 0, stats.entreprisesSuspendues || 0]"
              :colors="['#0f766e', '#ef4444']"
              :height="240"
          />
        </div>

        <div class="bg-white border border-slate-200 rounded-xl p-4 sm:p-5">
          <h2 class="text-sm font-semibold text-slate-800">Qualité de flux</h2>
          <p class="text-xs text-slate-400 mb-3">Répartition des scores</p>
          <DonutChart
              :labels="qualiteLabels"
              :values="qualiteValues"
              :colors="['#10b981', '#0f766e', '#f59e0b', '#f97316', '#ef4444']"
              :height="240"
          />
        </div>

        <div class="bg-white border border-slate-200 rounded-xl p-4 sm:p-5">
          <h2 class="text-sm font-semibold text-slate-800">Backlog plateforme</h2>
          <p class="text-xs text-slate-400 mb-3">Demandes en attente</p>
          <DonutChart
              :labels="['Heures supp.', 'Congés', 'Paiements']"
              :values="backlogValues"
              :colors="['#f59e0b', '#0ea5e9', '#4caf7d']"
              :height="240"
          />
        </div>
      </div>

      <div class="bg-white border border-slate-200 rounded-xl p-4 sm:p-5">
        <h2 class="text-sm font-semibold text-slate-800">Tendance 7 jours</h2>
        <p class="text-xs text-slate-400 mb-3">Pointages, actions métier et congés créés</p>
        <LineChart
            :labels="serieLabels"
            :datasets="serieDatasets"
            y-title="Volume"
            :height="300"
            show-empty
        />
      </div>

      <div class="grid grid-cols-1 xl:grid-cols-2 gap-4">
        <div class="bg-white border border-slate-200 rounded-xl p-4 sm:p-5">
          <h2 class="text-sm font-semibold text-slate-800">Effectifs par entreprise</h2>
          <p class="text-xs text-slate-400 mb-3">Nombre d’employés</p>
          <BarChart
              :labels="barLabels"
              :datasets="[{ label: 'Employés', data: barEmployes, color: '#0f766e' }]"
              y-title="Employés"
              :height="280"
          />
        </div>
        <div class="bg-white border border-slate-200 rounded-xl p-4 sm:p-5">
          <h2 class="text-sm font-semibold text-slate-800">Score de flux</h2>
          <p class="text-xs text-slate-400 mb-3">Comparaison des boîtes (0–100)</p>
          <BarChart
              :labels="barLabels"
              :datasets="[{ label: 'Score', data: barScores, color: '#0ea5e9' }]"
              y-title="Score"
              :height="280"
          />
        </div>
      </div>

      <div class="bg-white border border-slate-200 rounded-xl p-4 sm:p-5">
        <h2 class="text-sm font-semibold text-slate-800 mb-1">Activité récente (7j)</h2>
        <p class="text-xs text-slate-400 mb-3">Pointages et connexions employés</p>
        <BarChart
            :labels="barLabels"
            :datasets="[
              { label: 'Pointages', data: barPointages, color: '#0f766e' },
              { label: 'Connectés', data: barConnectes, color: '#f59e0b' }
            ]"
            y-title="Volume"
            :height="300"
        />
      </div>

      <div v-if="!entreprises.length" class="bg-white border border-slate-200 rounded-xl p-8 text-center text-slate-500">
        Aucune entreprise connectée. Créez-en une depuis l’onglet Entreprises.
      </div>

      <div v-else class="grid grid-cols-1 xl:grid-cols-2 gap-4">
        <article
            v-for="e in entreprises"
            :key="e.idEntreprise"
            class="bg-white border border-slate-200 rounded-xl p-5 flex flex-col gap-4"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <h2 class="text-lg font-semibold text-slate-900 truncate">{{ e.nomEntreprise }}</h2>
              <p class="text-xs text-slate-500 mt-0.5">DG : {{ e.dgLogin || '—' }}</p>
            </div>
            <div class="flex flex-col items-end gap-1 shrink-0">
              <span
                  :class="e.statut === 'Actif' ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-700'"
                  class="px-2 py-0.5 rounded-full text-xs font-medium"
              >
                {{ e.statut }}
              </span>
              <span
                  :class="qualiteClass(e.qualiteFlux)"
                  class="px-2 py-0.5 rounded-full text-xs font-medium"
              >
                Flux : {{ e.qualiteFlux }}
              </span>
            </div>
          </div>

          <div>
            <div class="flex justify-between text-xs text-slate-500 mb-1">
              <span>Score de flux</span>
              <span class="font-medium text-slate-700">{{ e.scoreFlux }}/100</span>
            </div>
            <div class="h-2 rounded-full bg-slate-100 overflow-hidden">
              <div
                  class="h-full rounded-full transition-all"
                  :class="barClass(e.scoreFlux)"
                  :style="{ width: e.scoreFlux + '%' }"
              />
            </div>
          </div>

          <div class="grid grid-cols-2 sm:grid-cols-4 gap-3 text-center">
            <div class="rounded-lg bg-slate-50 p-3">
              <p class="text-xl font-semibold text-slate-900">{{ e.nbEmployes }}</p>
              <p class="text-[11px] text-slate-500 mt-0.5">Employés</p>
            </div>
            <div class="rounded-lg bg-slate-50 p-3">
              <p class="text-xl font-semibold text-slate-900">{{ e.nbEmployesConnectes7j }}</p>
              <p class="text-[11px] text-slate-500 mt-0.5">Connectés (7j)</p>
            </div>
            <div class="rounded-lg bg-slate-50 p-3">
              <p class="text-xl font-semibold text-slate-900">{{ e.pointages7j }}</p>
              <p class="text-[11px] text-slate-500 mt-0.5">Pointages (7j)</p>
            </div>
            <div class="rounded-lg bg-slate-50 p-3">
              <p class="text-xl font-semibold text-slate-900">{{ backlog(e) }}</p>
              <p class="text-[11px] text-slate-500 mt-0.5">En attente</p>
            </div>
          </div>

          <div class="text-xs text-slate-500 flex flex-wrap gap-x-4 gap-y-1 border-t border-slate-100 pt-3">
            <span>Admins : {{ e.nbAdmins }}</span>
            <span>Comptes actifs : {{ e.nbUtilisateursActifs }}</span>
            <span>HS : {{ e.heuresSuppEnAttente }}</span>
            <span>Congés : {{ e.congesEnAttente }}</span>
            <span>Paies : {{ e.paiementsEnAttente }}</span>
            <span>Dernière activité : {{ formatDate(e.derniereActivite) }}</span>
          </div>
        </article>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { plateformeAPI } from '@/API/plateforme'
import { useToast } from '@/composable/useToast'
import dayjs from 'dayjs'
import DonutChart from '@/components/charts/DonutChart.vue'
import LineChart from '@/components/charts/LineChart.vue'
import BarChart from '@/components/charts/BarChart.vue'

const { error } = useToast()
const loading = ref(true)
const refreshSec = 25
let timer = null

const stats = ref({
  totalEntreprises: 0,
  entreprisesActives: 0,
  entreprisesSuspendues: 0,
  totalUtilisateurs: 0,
  totalEmployes: 0,
  totalPointages7j: 0,
  totalConnectes7j: 0,
  totalBacklog: 0,
  entreprises: [],
  pointages7j: [],
  activites7j: [],
  conges7j: [],
  genereA: null
})

const entreprises = computed(() => stats.value.entreprises || [])

const cards = computed(() => [
  { label: 'Boîtes', value: stats.value.totalEntreprises },
  { label: 'Actives', value: stats.value.entreprisesActives },
  { label: 'Suspendues', value: stats.value.entreprisesSuspendues },
  { label: 'Employés', value: stats.value.totalEmployes },
  { label: 'Utilisateurs', value: stats.value.totalUtilisateurs },
  { label: 'Pointages 7j', value: stats.value.totalPointages7j },
  { label: 'Backlog', value: stats.value.totalBacklog }
])

const QUALITES = ['Excellent', 'Bon', 'Moyen', 'Faible', 'Critique']
const qualiteLabels = QUALITES
const qualiteValues = computed(() =>
  QUALITES.map(q => entreprises.value.filter(e => e.qualiteFlux === q).length)
)

const backlogValues = computed(() => {
  const list = entreprises.value
  return [
    list.reduce((s, e) => s + (e.heuresSuppEnAttente || 0), 0),
    list.reduce((s, e) => s + (e.congesEnAttente || 0), 0),
    list.reduce((s, e) => s + (e.paiementsEnAttente || 0), 0)
  ]
})

const serieLabels = computed(() => (stats.value.pointages7j || []).map(p => p.label))
const serieDatasets = computed(() => [
  {
    label: 'Pointages',
    data: (stats.value.pointages7j || []).map(p => p.valeur),
    color: '#0f766e'
  },
  {
    label: 'Actions',
    data: (stats.value.activites7j || []).map(p => p.valeur),
    color: '#0ea5e9'
  },
  {
    label: 'Congés',
    data: (stats.value.conges7j || []).map(p => p.valeur),
    color: '#f59e0b'
  }
])

const barLabels = computed(() => entreprises.value.map(e => e.nomEntreprise))
const barEmployes = computed(() => entreprises.value.map(e => e.nbEmployes || 0))
const barScores = computed(() => entreprises.value.map(e => e.scoreFlux || 0))
const barPointages = computed(() => entreprises.value.map(e => e.pointages7j || 0))
const barConnectes = computed(() => entreprises.value.map(e => e.nbEmployesConnectes7j || 0))

const backlog = (e) =>
  (e.heuresSuppEnAttente || 0) + (e.congesEnAttente || 0) + (e.paiementsEnAttente || 0)

const formatDate = (d) => (d ? dayjs(d).format('DD/MM/YYYY HH:mm') : 'Aucune')

const qualiteClass = (q) => {
  const map = {
    Excellent: 'bg-emerald-50 text-emerald-700',
    Bon: 'bg-teal-50 text-teal-800',
    Moyen: 'bg-amber-50 text-amber-800',
    Faible: 'bg-orange-50 text-orange-800',
    Critique: 'bg-red-50 text-red-700'
  }
  return map[q] || 'bg-slate-100 text-slate-600'
}

const barClass = (score) => {
  if (score >= 80) return 'bg-emerald-500'
  if (score >= 60) return 'bg-teal-600'
  if (score >= 40) return 'bg-amber-500'
  if (score >= 20) return 'bg-orange-500'
  return 'bg-red-500'
}

const charger = async () => {
  try {
    const { data } = await plateformeAPI.dashboard()
    stats.value = data
  } catch (e) {
    error(e.response?.data?.message || 'Impossible de charger le tableau de bord')
  } finally {
    loading.value = false
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
