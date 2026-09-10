<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-800">Rapports reçus</h1>
      <p class="text-gray-500 mt-1">
        PDF de l’activité de votre entreprise — livrés chaque vendredi à 22h
      </p>
    </div>

    <div class="bg-white rounded-lg border shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
      <table class="min-w-full text-sm">
        <thead class="bg-gray-50 text-left">
          <tr>
            <th class="px-4 py-3">Titre</th>
            <th class="hidden sm:table-cell px-4 py-3">Période</th>
            <th class="hidden md:table-cell px-4 py-3">Généré le</th>
            <th class="px-4 py-3">Statut</th>
            <th class="px-4 py-3">Action</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rapports" :key="r.idRapport" class="border-t">
            <td class="px-4 py-3 font-medium">{{ r.titre }}</td>
            <td class="hidden sm:table-cell px-4 py-3">{{ r.periodeDebut }} → {{ r.periodeFin }}</td>
            <td class="hidden md:table-cell px-4 py-3">{{ formatDate(r.dateGeneration) }}</td>
            <td class="px-4 py-3">
              <span :class="r.lu ? 'text-gray-500' : 'text-amber-600 font-medium'">
                {{ r.lu ? 'Lu' : 'Nouveau' }}
              </span>
            </td>
            <td class="px-4 py-3">
              <button @click="download(r)" class="text-blue-600 hover:underline py-2">PDF</button>
            </td>
          </tr>
          <tr v-if="!rapports.length">
            <td colspan="5" class="px-4 py-8 text-center text-gray-400">Aucun rapport reçu</td>
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
const rapports = ref([])

const charger = async () => {
  try {
    const { data } = await rapportsAPI.getAll()
    rapports.value = data
  } catch (err) {
    error(err.response?.data?.message || 'Erreur chargement rapports')
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
    await charger()
  } catch {
    error('Téléchargement impossible')
  }
}

const formatDate = (d) => d ? new Date(d).toLocaleString('fr-FR') : ''
onMounted(charger)
</script>
