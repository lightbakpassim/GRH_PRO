<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">Gestion des paiements</h1>
        <p class="text-gray-500 mt-1">Juin 2026</p>
      </div>
      <button
          @click="openGenererModal"
          class="bg-primary text-white px-4 py-2 rounded-md font-medium transition-all duration-200 hover:bg-primary-dark active:scale-95 flex items-center gap-2"
      >
        <PlusIcon class="w-5 h-5" />
        Générer un paiement
      </button>
    </div>

    <!-- Filtres -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
      <div class="flex flex-col sm:flex-row gap-4">
        <div class="flex-1 relative">
          <MagnifyingGlassIcon class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
              v-model="searchQuery"
              type="text"
              placeholder="Rechercher par employé..."
              class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
          />
        </div>
        <select v-model="filtreStatut" class="px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary sm:w-40">
          <option value="">Tous statuts</option>
          <option value="Effectué">Effectué</option>
          <option value="En_attente">En attente</option>
        </select>
        <select v-model="filtreMois" class="px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary sm:w-40">
          <option value="">Tous mois</option>
          <option value="1">Janvier</option>
          <option value="2">Février</option>
          <option value="3">Mars</option>
          <option value="4">Avril</option>
          <option value="5">Mai</option>
          <option value="6">Juin</option>
          <option value="7">Juillet</option>
          <option value="8">Août</option>
          <option value="9">Septembre</option>
          <option value="10">Octobre</option>
          <option value="11">Novembre</option>
          <option value="12">Décembre</option>
        </select>
      </div>
    </div>

    <!-- Stats rapides -->
    <div class="grid grid-cols-1 sm:grid-cols-4 gap-4">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 text-center">
        <p class="text-2xl font-bold text-primary">{{ stats.total }}</p>
        <p class="text-sm text-gray-500">Total paiements</p>
      </div>
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 text-center">
        <p class="text-2xl font-bold text-success">{{ stats.effectues }}</p>
        <p class="text-sm text-gray-500">Effectués</p>
      </div>
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 text-center">
        <p class="text-2xl font-bold text-warning">{{ stats.enAttente }}</p>
        <p class="text-sm text-gray-500">En attente</p>
      </div>
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 text-center">
        <p class="text-2xl font-bold text-primary">{{ formatCurrency(stats.totalNet) }}</p>
        <p class="text-sm text-gray-500">Montant total</p>
      </div>
    </div>

    <!-- Table -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Employé</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Mois</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Salaire base</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Heures Supp</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Total Net</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Statut</th>
            <th class="px-6 py-3 text-right text-xs font-semibold text-gray-600 uppercase">Actions</th>
          </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
          <tr v-for="paiement in paiementsFiltres" :key="paiement.idPaiement" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ paiement.nomCompletEmploye }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-700">{{ getNomMois(paiement.mois) }} {{ paiement.annee }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-700">{{ formatCurrency(paiement.salaireBase) }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-700">{{ formatCurrency(paiement.heuresSuppMontant) }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-semibold text-primary">{{ formatCurrency(paiement.totalNet) }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <StatusBadge :status="paiement.statut === 'Effectué' ? 'Effectué' : 'En attente'" />
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right">
              <div class="flex items-center justify-end gap-2">
                <button
                    v-if="paiement.statut !== 'Effectué'"
                    @click="marquerEffectue(paiement)"
                    class="text-success hover:text-success/80 p-1 rounded transition-colors"
                    title="Marquer comme effectué"
                >
                  <CheckIcon class="w-5 h-5" />
                </button>
                <button
                    @click="telechargerBulletin(paiement)"
                    class="text-primary hover:text-primary/80 p-1 rounded transition-colors"
                    title="Télécharger le bulletin"
                >
                  <DocumentArrowDownIcon class="w-5 h-5" />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="paiementsFiltres.length === 0">
            <td colspan="7" class="px-6 py-8 text-center text-gray-500">
              Aucun paiement trouvé
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Générer un paiement -->
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="closeModal">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md">
        <div class="flex justify-between items-center p-4 border-b">
          <h3 class="text-lg font-semibold">Générer un paiement</h3>
          <button @click="closeModal" class="text-gray-400 hover:text-gray-600">
            <XMarkIcon class="w-6 h-6" />
          </button>
        </div>
        <form @submit.prevent="genererPaiement" class="p-4 space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Employé</label>
            <select v-model="formPaiement.idEmploye" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary" required>
              <option value="">Sélectionner un employé</option>
              <option v-for="emp in employes" :key="emp.idEmploye" :value="emp.idEmploye">
                {{ emp.prenomEmploye }} {{ emp.nomEmploye }}
              </option>
            </select>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Mois</label>
              <select v-model="formPaiement.mois" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary" required>
                <option value="">Sélectionner</option>
                <option value="1">Janvier</option>
                <option value="2">Février</option>
                <option value="3">Mars</option>
                <option value="4">Avril</option>
                <option value="5">Mai</option>
                <option value="6">Juin</option>
                <option value="7">Juillet</option>
                <option value="8">Août</option>
                <option value="9">Septembre</option>
                <option value="10">Octobre</option>
                <option value="11">Novembre</option>
                <option value="12">Décembre</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Année</label>
              <input v-model="formPaiement.annee" type="number" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary" placeholder="2026" required />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Salaire base (FCFA)</label>
            <input v-model="formPaiement.salaireBase" type="number" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary" required />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Heures supplémentaires (FCFA)</label>
            <input v-model="formPaiement.heuresSuppMontant" type="number" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary" required />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Retenues (FCFA)</label>
            <input v-model="formPaiement.retenues" type="number" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary" required />
          </div>
          <div class="flex justify-end gap-3 pt-4">
            <button type="button" @click="closeModal" class="btn-secondary">Annuler</button>
            <button type="submit" class="btn-primary">Générer</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useDataStore } from '@/stores/dataStore'
import { useToast } from '@/composable/useToast'
import { paiementsAPI } from '@/API/paiements'
import StatusBadge from '@/components/StatusBadge.vue'
import { PlusIcon, MagnifyingGlassIcon, CheckIcon, DocumentArrowDownIcon, XMarkIcon } from '@heroicons/vue/24/outline'

const dataStore = useDataStore()
const { success, error } = useToast()

const searchQuery = ref('')
const filtreStatut = ref('')
const filtreMois = ref('')
const showModal = ref(false)
const loading = ref(false)

const formPaiement = ref({
  idEmploye: '',
  mois: '',
  annee: new Date().getFullYear(),
  salaireBase: '',
  heuresSuppMontant: 0,
  retenues: 0
})

const employes = computed(() => dataStore.employes)

const paiementsFiltres = computed(() => {
  let result = [...dataStore.paiements]

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(p => p.nomCompletEmploye?.toLowerCase().includes(query))
  }

  if (filtreStatut.value) {
    result = result.filter(p => p.statut === filtreStatut.value)
  }

  if (filtreMois.value) {
    result = result.filter(p => p.mois === parseInt(filtreMois.value))
  }

  return result
})

const stats = computed(() => {
  const total = paiementsFiltres.value.length
  const effectues = paiementsFiltres.value.filter(p => p.statut === 'Effectué').length
  const enAttente = paiementsFiltres.value.filter(p => p.statut === 'En_attente').length
  const totalNet = paiementsFiltres.value.reduce((sum, p) => sum + (p.totalNet || 0), 0)
  return { total, effectues, enAttente, totalNet }
})

const formatCurrency = (value) => {
  if (!value) return '0 FCFA'
  return new Intl.NumberFormat('fr-FR').format(value) + ' FCFA'
}

const getNomMois = (mois) => {
  const moisNoms = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre']
  return moisNoms[mois - 1] || mois
}

const openGenererModal = () => {
  formPaiement.value = {
    idEmploye: '',
    mois: new Date().getMonth() + 1,
    annee: new Date().getFullYear(),
    salaireBase: '',
    heuresSuppMontant: 0,
    retenues: 0
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const genererPaiement = async () => {
  try {
    const response = await paiementsAPI.generate(formPaiement.value)
    dataStore.paiements.unshift(response.data)
    success('Paiement généré avec succès')
    closeModal()
  } catch (err) {
    error(err.response?.data?.message || 'Erreur lors de la génération')
  }
}

const marquerEffectue = async (paiement) => {
  try {
    const response = await paiementsAPI.marquerEffectue(paiement.idPaiement)
    const index = dataStore.paiements.findIndex(p => p.idPaiement === paiement.idPaiement)
    if (index !== -1) {
      dataStore.paiements[index] = response.data
    }
    success(`Paiement de ${paiement.nomCompletEmploye} marqué comme effectué`)
  } catch (err) {
    error(err.response?.data?.message || 'Erreur lors du marquage')
  }
}

const telechargerBulletin = async (paiement) => {
  try {
    const response = await paiementsAPI.telechargerBulletin(paiement.idPaiement)
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `bulletin_${paiement.nomCompletEmploye}_${paiement.mois}_${paiement.annee}.pdf`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    success('Téléchargement du bulletin commencé')
  } catch (err) {
    error('Erreur lors du téléchargement du bulletin')
  }
}

onMounted(async () => {
  await dataStore.loadAllData()
})
</script>