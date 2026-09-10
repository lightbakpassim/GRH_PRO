<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-800">Historique des actions</h1>
      <p class="text-gray-500 mt-1">Traçabilité (employés, rapports, MDP, paiements validés…)</p>
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

const { error } = useToast()
const items = ref([])

onMounted(async () => {
  try {
    const { data } = await rapportsAPI.getHistorique()
    items.value = data
  } catch (err) {
    error(err.response?.data?.message || 'Erreur historique')
  }
})

const formatDate = (d) => d ? new Date(d).toLocaleString('fr-FR') : ''
</script>
