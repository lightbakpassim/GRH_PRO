<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">Employés</h1>
        <p class="text-gray-500 mt-1">Juin 2026</p>
      </div>
      <button
          @click="openAddModal"
          class="bg-blue-600 text-white px-4 py-2 rounded-md font-medium transition-all duration-200 hover:bg-blue-700 active:scale-95 flex items-center gap-2"
      >
        <PlusIcon class="w-5 h-5" />
        Ajouter
      </button>
    </div>

    <!-- Search Bar -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
      <div class="flex flex-col sm:flex-row gap-4">
        <div class="flex-1 relative">
          <MagnifyingGlassIcon class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
              v-model="searchQuery"
              type="text"
              placeholder="Rechercher un employé..."
              class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>
        <select v-model="filtreDepartement" class="px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 sm:w-48">
          <option value="">Tous les départements</option>
          <option v-for="dept in departements" :key="dept" :value="dept">{{ dept }}</option>
        </select>
      </div>
    </div>

    <!-- Table -->
    <DataTable
        :columns="columns"
        :data="employesFiltres"
    >
      <template #column-statut="{ row }">
        <StatusBadge :status="row.statut" />
      </template>
      <template #column-actions="{ row }">
        <div class="flex items-center gap-2">
          <button
              @click="editEmploye(row)"
              class="text-blue-600 hover:text-blue-800 p-1 rounded transition-colors"
              title="Modifier"
          >
            <PencilIcon class="w-5 h-5" />
          </button>
          <button
              @click="deleteEmploye(row)"
              class="text-red-600 hover:text-red-800 p-1 rounded transition-colors"
              title="Désactiver"
          >
            <TrashIcon class="w-5 h-5" />
          </button>
        </div>
      </template>
    </DataTable>

    <!-- Modal Ajout/Modification -->
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="closeModal">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md">
        <div class="flex justify-between items-center p-4 border-b">
          <h3 class="text-lg font-semibold">{{ isEditing ? 'Modifier' : 'Ajouter' }} un employé</h3>
          <button @click="closeModal" class="text-gray-400 hover:text-gray-600">
            <XMarkIcon class="w-6 h-6" />
          </button>
        </div>
        <form @submit.prevent="saveEmploye" class="p-4 space-y-4">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Nom</label>
              <input v-model="form.nom" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Prénom</label>
              <input v-model="form.prenom" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input v-model="form.email" type="email" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Téléphone</label>
            <input v-model="form.telephone" type="tel" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Poste</label>
              <input v-model="form.poste" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Département</label>
              <select v-model="form.departement" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required>
                <option value="">Sélectionner</option>
                <option value="IT">IT</option>
                <option value="RH">RH</option>
                <option value="FINANCE">FINANCE</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Salaire de base (FCFA)</label>
            <input v-model="form.salaireBase" type="number" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required />
          </div>
          <div class="flex justify-end gap-3 pt-4">
            <button type="button" @click="closeModal" class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition-colors">Annuler</button>
            <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors">{{ isEditing ? 'Modifier' : 'Ajouter' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useDataStore } from '@/stores/dataStore'
import { useToast } from '@/composable/useToast'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { PlusIcon, MagnifyingGlassIcon, PencilIcon, TrashIcon, XMarkIcon } from '@heroicons/vue/24/outline'

const dataStore = useDataStore()
const { success, error } = useToast()

const searchQuery = ref('')
const filtreDepartement = ref('')
const showModal = ref(false)
const isEditing = ref(false)
const currentId = ref(null)

const form = ref({
  nom: '',
  prenom: '',
  email: '',
  telephone: '',
  poste: '',
  departement: '',
  salaireBase: ''
})

const columns = [
  { key: 'nom', label: 'Nom' },
  { key: 'prenom', label: 'Prénom' },
  { key: 'poste', label: 'Poste' },
  { key: 'departement', label: 'Département' },
  { key: 'email', label: 'Email' },
  { key: 'statut', label: 'Statut', type: 'badge' },
  { key: 'actions', label: 'Action' }
]

const departements = computed(() => {
  const depts = new Set(dataStore.employes.map(e => e.departement))
  return Array.from(depts)
})

const employesFiltres = computed(() => {
  let result = dataStore.employes

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(e =>
        e.nom.toLowerCase().includes(query) ||
        e.prenom.toLowerCase().includes(query) ||
        e.email.toLowerCase().includes(query)
    )
  }

  if (filtreDepartement.value) {
    result = result.filter(e => e.departement === filtreDepartement.value)
  }

  return result
})

const openAddModal = () => {
  isEditing.value = false
  currentId.value = null
  form.value = {
    nom: '',
    prenom: '',
    email: '',
    telephone: '',
    poste: '',
    departement: '',
    salaireBase: ''
  }
  showModal.value = true
}

const editEmploye = (employe) => {
  isEditing.value = true
  currentId.value = employe.id
  form.value = { ...employe }
  showModal.value = true
}

const deleteEmploye = (employe) => {
  if (confirm(`Voulez-vous vraiment désactiver ${employe.prenom} ${employe.nom} ?`)) {
    dataStore.supprimerEmploye(employe.id)
    success(`Employé ${employe.prenom} ${employe.nom} désactivé`)
  }
}

const saveEmploye = () => {
  const employeData = {
    ...form.value,
    nom: form.value.nom.toUpperCase(),
    prenom: form.value.prenom,
    statut: 'Actif',
    dateEmbauche: new Date().toISOString().split('T')[0]
  }

  if (isEditing.value) {
    dataStore.modifierEmploye(currentId.value, employeData)
    success('Employé modifié avec succès')
  } else {
    dataStore.ajouterEmploye(employeData)
    success('Employé ajouté avec succès')
  }

  closeModal()
}

const closeModal = () => {
  showModal.value = false
  isEditing.value = false
  currentId.value = null
}
</script>