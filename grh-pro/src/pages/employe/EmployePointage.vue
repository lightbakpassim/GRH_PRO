<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-800">Pointages</h1>
      <p class="text-gray-500 mt-1">Pointer entrée puis sortie — enregistrement à la sortie</p>
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">Pointage du jour</h2>
      <div class="flex flex-col sm:flex-row items-center justify-between gap-6">
        <div class="flex items-center gap-6 flex-wrap">
          <div class="text-center">
            <p class="text-sm text-gray-500">Date</p>
            <p class="text-xl font-bold text-gray-800">{{ todayDate }}</p>
          </div>
          <div class="w-px h-10 bg-gray-200 hidden sm:block"></div>
          <div class="text-center">
            <p class="text-sm text-gray-500">Entrée</p>
            <p class="text-2xl font-bold text-green-600">{{ affichageJour.entree }}</p>
          </div>
          <div class="text-center">
            <p class="text-sm text-gray-500">Sortie</p>
            <p class="text-2xl font-bold" :class="affichageJour.sortie !== '--:--' ? 'text-red-600' : 'text-gray-400'">
              {{ affichageJour.sortie }}
            </p>
          </div>
          <div class="text-center">
            <p class="text-sm text-gray-500">H. travaillées</p>
            <p class="text-xl font-bold text-blue-600">{{ affichageJour.heuresTrav }} h</p>
          </div>
        </div>
        <div class="flex gap-3">
          <button
              v-if="!affichageJour.hasEntree"
              @click="pointerEntree"
              class="bg-green-600 text-white px-6 py-2 rounded-md font-medium hover:bg-green-700"
          >
            Pointer l'entrée
          </button>
          <button
              v-else-if="!affichageJour.hasSortie"
              @click="pointerSortie"
              :disabled="saving"
              class="bg-red-600 text-white px-6 py-2 rounded-md font-medium hover:bg-red-700 disabled:opacity-50"
          >
            Pointer la sortie
          </button>
          <button v-else disabled class="bg-gray-400 text-white px-6 py-2 rounded-md cursor-not-allowed">
            Journée complétée
          </button>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">Mon historique de pointage</h2>
      <DataTable :columns="columns" :data="mesPointages">
        <template #column-statut="{ row }">
          <StatusBadge :status="row.statut" />
        </template>
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/data.js'
import { pointagesAPI } from '@/API/pointages'
import {
  mapPointage,
  getPointageDraft,
  setPointageDraft,
  clearPointageDraft
} from '@/utils/mappers'
import { useToast } from '@/composable/useToast'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import dayjs from 'dayjs'

const authStore = useAuthStore()
const { success, error } = useToast()

const mesPointages = ref([])
const draft = ref(null)
const saving = ref(false)

const todayDate = ref(dayjs().format('DD/MM/YYYY'))
const todayISO = dayjs().format('YYYY-MM-DD')

const columns = [
  { key: 'date', label: 'Date', type: 'date' },
  { key: 'entree', label: 'Entrée' },
  { key: 'sortie', label: 'Sortie' },
  { key: 'heuresTrav', label: 'H. Trav' },
  { key: 'heuresSupp', label: 'H. Supp' },
  { key: 'statut', label: 'Statut', type: 'badge' }
]

const pointageServeur = computed(() => mesPointages.value.find(p => p.date === todayISO))

const affichageJour = computed(() => {
  if (pointageServeur.value) {
    return {
      entree: pointageServeur.value.entree,
      sortie: pointageServeur.value.sortie,
      heuresTrav: Number(pointageServeur.value.heuresTrav).toFixed(2),
      hasEntree: true,
      hasSortie: true
    }
  }
  if (draft.value?.date === todayISO) {
    return {
      entree: draft.value.entree,
      sortie: '--:--',
      heuresTrav: '0.00',
      hasEntree: true,
      hasSortie: false
    }
  }
  return { entree: '--:--', sortie: '--:--', heuresTrav: '0.00', hasEntree: false, hasSortie: false }
})

const charger = async () => {
  try {
    const { data } = await pointagesAPI.getMonSuivi()
    mesPointages.value = data.map(mapPointage).sort((a, b) => new Date(b.date) - new Date(a.date))
    draft.value = getPointageDraft()
    if (draft.value?.date !== todayISO) {
      clearPointageDraft()
      draft.value = null
    }
  } catch (err) {
    error(err.response?.data?.message || 'Erreur chargement pointages')
  }
}

const pointerEntree = () => {
  if (pointageServeur.value) {
    error('Pointage déjà enregistré pour aujourd\'hui')
    return
  }
  const entree = dayjs().format('HH:mm')
  const payload = { date: todayISO, entree }
  setPointageDraft(payload)
  draft.value = payload
  success(`Entrée pointée à ${entree}`)
}

const pointerSortie = async () => {
  if (!draft.value?.entree) {
    error('Pointez d\'abord l\'entrée')
    return
  }
  const sortie = dayjs().format('HH:mm')
  saving.value = true
  try {
    await pointagesAPI.create({
      idEmploye: authStore.user?.idEmploye,
      dateTravail: todayISO,
      heuresDebut: `${draft.value.entree}:00`,
      heuresFin: `${sortie}:00`,
      statutSupp: 'En_attente'
    })
    clearPointageDraft()
    draft.value = null
    success(`Sortie pointée à ${sortie}`)
    await charger()
  } catch (err) {
    error(err.response?.data?.message || 'Erreur enregistrement pointage')
  } finally {
    saving.value = false
  }
}

onMounted(charger)
</script>
