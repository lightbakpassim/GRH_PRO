<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">Rapports hebdomadaires</h1>
        <p class="text-gray-500 mt-1">
          Auto chaque vendredi à 22h GMT — génération manuelle possible ci-dessous
        </p>
      </div>
      <button
          @click="generer"
          :disabled="generating"
          class="w-full sm:w-auto px-4 py-2.5 min-h-11 bg-teal-700 text-white rounded-md hover:bg-teal-800 disabled:opacity-50"
      >
        {{ generating ? 'Génération…' : 'Générer le rapport PDF' }}
      </button>
    </div>

    <div class="bg-white rounded-lg border shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
      <table class="min-w-full text-sm">
        <thead class="bg-gray-50 text-left">
          <tr>
            <th class="px-4 py-3">Titre</th>
            <th class="hidden sm:table-cell px-4 py-3">Période</th>
            <th class="hidden md:table-cell px-4 py-3">Généré le</th>
            <th class="px-4 py-3">Action</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rapports" :key="r.idRapport" class="border-t">
            <td class="px-4 py-3 font-medium">{{ r.titre }}</td>
            <td class="hidden sm:table-cell px-4 py-3">{{ r.periodeDebut }} → {{ r.periodeFin }}</td>
            <td class="hidden md:table-cell px-4 py-3">{{ formatDate(r.dateGeneration) }}</td>
            <td class="px-4 py-3">
              <button @click="download(r)" class="text-blue-600 hover:underline py-2">PDF</button>
            </td>
          </tr>
          <tr v-if="!rapports.length">
            <td colspan="4" class="px-4 py-8 text-center text-gray-400">Aucun rapport généré</td>
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
const rapports = ref([])
const generating = ref(false)

const charger = async () => {
  try {
    const { data } = await rapportsAPI.getAll()
    rapports.value = data
  } catch (err) {
    error(err.response?.data?.message || 'Erreur chargement')
  }
}

const generer = async () => {
  generating.value = true
  try {
    await rapportsAPI.genererHebdo()
    success('Rapport généré et livré au DG')
    await charger()
  } catch (err) {
    error(err.response?.data?.message || 'Échec génération')
  } finally {
    generating.value = false
  }
}

const download = async (r) => {
  try {
    const { data } = await rapportsAPI.downloadPdf(r.idRapport)
    const url = URL.createObjectURL(new Blob([data], { type: 'application/pdf' }))
    const a = document.createElement('a')
    a.href = url
    a.download = r.nomFichier || 'rapport.pdf'
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    error('Téléchargement impossible')
  }
}

const formatDate = (d) => d ? new Date(d).toLocaleString('fr-FR') : ''
onMounted(charger)
</script>
