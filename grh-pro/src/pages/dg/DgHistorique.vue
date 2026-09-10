<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">Historique des actions</h1>
        <p class="text-gray-500 mt-1">Traçabilité propre à votre entreprise</p>
      </div>
      <button
          type="button"
          class="px-4 py-2 text-sm font-medium rounded-lg border border-red-200 text-red-700 hover:bg-red-50 disabled:opacity-40"
          :disabled="clearing || !items.length"
          @click="vider"
      >
        {{ clearing ? 'Suppression…' : 'Vider l’historique' }}
      </button>
    </div>
    <div class="bg-white rounded-lg border shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
      <table class="min-w-full text-sm">
        <thead class="bg-gray-50 text-left">
          <tr>
            <th class="px-4 py-3">Date</th>
            <th class="px-4 py-3">Action</th>
            <th class="hidden sm:table-cell px-4 py-3">Acteur</th>
            <th class="px-4 py-3">Détail</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="h in items" :key="h.idHistorique" class="border-t align-top">
            <td class="px-4 py-3 whitespace-nowrap text-xs sm:text-sm">{{ formatDate(h.dateAction) }}</td>
            <td class="px-4 py-3 font-medium text-sm">{{ h.action }}</td>
            <td class="hidden sm:table-cell px-4 py-3 text-sm">{{ h.acteurLogin }}</td>
            <td class="px-4 py-3 text-gray-600 text-sm break-words max-w-[14rem] sm:max-w-md">{{ h.detail }}</td>
          </tr>
          <tr v-if="!items.length">
            <td colspan="4" class="px-4 py-8 text-center text-gray-400">Aucune action enregistrée</td>
          </tr>
        </tbody>
      </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { rapportsAPI } from '@/API/rapports'
import { useToast } from '@/composable/useToast'

const { success, error } = useToast()
const items = ref([])
const clearing = ref(false)

const load = async () => {
  try {
    const { data } = await rapportsAPI.getHistorique()
    items.value = data
  } catch (err) {
    error(err.response?.data?.message || 'Erreur historique')
  }
}

onMounted(load)

const vider = async () => {
  if (!confirm('Supprimer définitivement tout l’historique de votre entreprise ?')) return
  clearing.value = true
  try {
    const { data } = await rapportsAPI.viderHistorique()
    success(`${data.supprimes ?? 0} entrée(s) supprimée(s)`)
    items.value = []
  } catch (err) {
    error(err.response?.data?.message || 'Impossible de vider l’historique')
  } finally {
    clearing.value = false
  }
}

const formatDate = (d) => d ? new Date(d).toLocaleString('fr-FR') : ''
</script>
