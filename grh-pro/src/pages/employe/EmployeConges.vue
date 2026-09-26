<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-800">Demande de congé</h1>
      <p class="text-gray-500 mt-1">Juin 2026</p>
    </div>

    <!-- Formulaire de demande -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">Nouvelle demande de congé</h2>
      <form @submit.prevent="submitDemande" class="space-y-5">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Date de début</label>
            <input
                v-model="form.dateDebut"
                type="date"
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                required
                @change="calculerDuree"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Date de fin</label>
            <input
                v-model="form.dateFin"
                type="date"
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                required
                @change="calculerDuree"
            />
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Motif</label>
          <select
              v-model="form.motif"
              class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              required
          >
            <option value="">Sélectionner un motif</option>
            <option value="Vacances">Vacances annuelles</option>
            <option value="Formation">Formation professionnelle</option>
            <option value="Maladie">Maladie</option>
            <option value="Personnel">Raisons personnelles</option>
          </select>
        </div>

        <div class="bg-blue-50 rounded-lg p-4 flex items-center justify-between flex-wrap gap-4">
          <div>
            <p class="text-sm text-gray-600">Durée calculée</p>
            <p class="text-2xl font-bold text-blue-600">{{ duree }} jours</p>
          </div>
          <button
              type="submit"
              class="bg-blue-600 text-white px-6 py-2 rounded-md font-medium hover:bg-blue-700 transition-colors"
          >
            Envoyer la demande
          </button>
        </div>
      </form>
    </div>

    <!-- Solde congés -->
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 text-center">
        <p class="text-3xl font-bold text-blue-600">{{ solde.total }}</p>
        <p class="text-sm text-gray-500">Total congés</p>
      </div>
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 text-center">
        <p class="text-3xl font-bold text-green-600">{{ solde.pris }}</p>
        <p class="text-sm text-gray-500">Pris</p>
      </div>
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 text-center">
        <p class="text-3xl font-bold text-orange-600">{{ solde.restant }}</p>
        <p class="text-sm text-gray-500">Restant</p>
      </div>
    </div>

    <!-- Historique des demandes -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">Mes demandes de congé</h2>
      <DataTable
          :columns="columns"
          :data="mesDemandes"
      >
        <template #column-statut="{ row }">
          <StatusBadge :status="row.statut" />
        </template>
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useDataStore } from '@/stores/dataStore'
import { useToast } from '@/composable/useToast'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import dayjs from 'dayjs'

const dataStore = useDataStore()
const { success, error } = useToast()

const currentEmployeId = ref(1) // Light B.
const currentEmployeNom = ref('Light B.')

const form = ref({
  dateDebut: '',
  dateFin: '',
  motif: ''
})

const duree = ref(0)

const columns = [
  { key: 'dateDebut', label: 'Début', type: 'date' },
  { key: 'dateFin', label: 'Fin', type: 'date' },
  { key: 'nbJours', label: 'Jours' },
  { key: 'motif', label: 'Motif' },
  { key: 'statut', label: 'Statut', type: 'badge' }
]

const mesDemandes = computed(() => {
  return dataStore.conges
      .filter(c => c.employeId === currentEmployeId.value)
      .sort((a, b) => new Date(b.dateDebut) - new Date(a.dateDebut))
})

const solde = computed(() => {
  const total = 30 // 30 jours de congés par an
  const pris = mesDemandes.value
      .filter(c => c.statut === 'Validé')
      .reduce((sum, c) => sum + c.nbJours, 0)
  return {
    total,
    pris,
    restant: total - pris
  }
})

const calculerDuree = () => {
  if (form.value.dateDebut && form.value.dateFin) {
    const debut = dayjs(form.value.dateDebut)
    const fin = dayjs(form.value.dateFin)
    if (fin.isAfter(debut) || fin.isSame(debut)) {
      duree.value = fin.diff(debut, 'day') + 1
    } else {
      duree.value = 0
      error('La date de fin doit être postérieure à la date de début')
    }
  }
}

const submitDemande = () => {
  if (!form.value.dateDebut || !form.value.dateFin || !form.value.motif) {
    error('Veuillez remplir tous les champs')
    return
  }

  if (duree.value <= 0) {
    error('Durée invalide')
    return
  }

  if (duree.value > solde.value.restant) {
    error(`Vous n'avez que ${solde.value.restant} jours de congés restants`)
    return
  }

  const nouvelleDemande = {
    employeId: currentEmployeId.value,
    employeNom: currentEmployeNom.value,
    dateDebut: form.value.dateDebut,
    dateFin: form.value.dateFin,
    nbJours: duree.value,
    motif: form.value.motif,
    statut: 'En attente'
  }

  dataStore.ajouterConge(nouvelleDemande)
  success('Votre demande de congé a été envoyée avec succès')

  // Reset form
  form.value = {
    dateDebut: '',
    dateFin: '',
    motif: ''
  }
  duree.value = 0
}

onMounted(() => {
  // Initialisation
})
</script>