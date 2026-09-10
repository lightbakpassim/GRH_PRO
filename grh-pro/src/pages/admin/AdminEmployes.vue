<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">Employés</h1>
        <p class="text-gray-500 mt-1">Gestion du personnel</p>
      </div>
      <button
          @click="openAddModal"
          class="w-full sm:w-auto bg-blue-600 text-white px-4 py-2.5 min-h-11 rounded-md font-medium transition-all duration-200 hover:bg-blue-700 active:scale-95 flex items-center justify-center gap-2"
      >
        <PlusIcon class="w-5 h-5" />
        Ajouter
      </button>
    </div>

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
        <select v-model="filtreDepartement" class="w-full px-4 py-2.5 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 sm:w-48">
          <option value="">Tous les départements</option>
          <option v-for="dept in departements" :key="dept.idDepartement" :value="dept.idDepartement">
            {{ dept.nomDepartement }}
          </option>
        </select>
      </div>
    </div>

    <DataTable :columns="columns" :data="employesFiltres">
      <template #column-statut="{ row }">
        <StatusBadge :status="row.statut" />
      </template>
      <template #column-actions="{ row }">
        <div class="flex items-center gap-1">
          <button @click="editEmploye(row)" class="text-blue-600 hover:text-blue-800 p-2.5 min-h-11 min-w-11 inline-flex items-center justify-center rounded" title="Modifier">
            <PencilIcon class="w-5 h-5" />
          </button>
          <button
              @click="renvoyerIdentifiants(row)"
              class="text-amber-600 hover:text-amber-800 p-2.5 min-h-11 min-w-11 inline-flex items-center justify-center rounded"
              title="Régénérer / renvoyer le mot de passe"
          >
            <KeyIcon class="w-5 h-5" />
          </button>
          <button @click="deleteEmploye(row)" class="text-red-600 hover:text-red-800 p-2.5 min-h-11 min-w-11 inline-flex items-center justify-center rounded" title="Désactiver">
            <TrashIcon class="w-5 h-5" />
          </button>
        </div>
      </template>
    </DataTable>

    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="closeModal">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div class="flex justify-between items-center p-4 border-b">
          <h3 class="text-lg font-semibold">{{ isEditing ? 'Modifier' : 'Ajouter' }} un employé</h3>
          <button @click="closeModal" class="text-gray-400 hover:text-gray-600">
            <XMarkIcon class="w-6 h-6" />
          </button>
        </div>
        <form @submit.prevent="saveEmploye" class="p-4 space-y-4">
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Nom</label>
              <input v-model="form.nom" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Prénom</label>
              <input v-model="form.prenom" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md" required />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input v-model="form.email" type="email" class="w-full px-3 py-2 border border-gray-300 rounded-md" required />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Téléphone</label>
            <input v-model="form.telephone" type="tel" class="w-full px-3 py-2 border border-gray-300 rounded-md" />
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Sexe</label>
              <select v-model="form.sexe" class="w-full px-3 py-2 border border-gray-300 rounded-md" required>
                <option value="M">M</option>
                <option value="F">F</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">État civil</label>
              <select v-model="form.etatCivil" class="w-full px-3 py-2 border border-gray-300 rounded-md" required>
                <option value="Célibataire">Célibataire</option>
                <option value="Marié">Marié</option>
                <option value="Divorcé">Divorcé</option>
                <option value="Veuf">Veuf</option>
              </select>
            </div>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Poste</label>
              <input v-model="form.poste" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Département</label>
              <select v-model="form.idDepartement" class="w-full px-3 py-2 border border-gray-300 rounded-md" required>
                <option value="">Sélectionner</option>
                <option v-for="dept in departements" :key="dept.idDepartement" :value="dept.idDepartement">
                  {{ dept.nomDepartement }}
                </option>
              </select>
              <p v-if="!departements.length" class="text-xs text-amber-600 mt-1">
                Aucun département —
                <router-link to="/admin/departements" class="underline">en créer un</router-link>
              </p>
            </div>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Date d'embauche</label>
              <input v-model="form.dateEmbauche" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-md" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Salaire de base</label>
              <input v-model="form.salaireBase" type="number" min="1" step="0.01" class="w-full px-3 py-2 border border-gray-300 rounded-md" required />
            </div>
          </div>
          <div class="flex justify-end gap-3 pt-4">
            <button type="button" @click="closeModal" class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md">Annuler</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 bg-blue-600 text-white rounded-md disabled:opacity-50">
              {{ isEditing ? 'Modifier' : 'Ajouter' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { employesAPI } from '@/API/employes'
import { departementsAPI } from '@/API/departements'
import { mapEmploye } from '@/utils/mappers'
import { unwrapList, ALL_PAGE } from '@/utils/api'
import { useToast } from '@/composable/useToast'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { PlusIcon, MagnifyingGlassIcon, PencilIcon, TrashIcon, XMarkIcon, KeyIcon } from '@heroicons/vue/24/outline'

const { success, error } = useToast()

const employes = ref([])
const departements = ref([])
const searchQuery = ref('')
const filtreDepartement = ref('')
const showModal = ref(false)
const isEditing = ref(false)
const currentId = ref(null)
const saving = ref(false)

const emptyForm = () => ({
  nom: '',
  prenom: '',
  email: '',
  telephone: '',
  poste: '',
  idDepartement: '',
  salaireBase: '',
  sexe: 'M',
  etatCivil: 'Célibataire',
  dateEmbauche: new Date().toISOString().slice(0, 10),
  statut: 'Actif'
})

const form = ref(emptyForm())

const columns = [
  { key: 'nom', label: 'Nom' },
  { key: 'prenom', label: 'Prénom', hideOnMobile: true },
  { key: 'poste', label: 'Poste' },
  { key: 'departement', label: 'Département', hideOnMobile: true },
  { key: 'email', label: 'Email', hideOnMobile: true },
  { key: 'statut', label: 'Statut', type: 'badge' },
  { key: 'actions', label: 'Action' }
]

const employesFiltres = computed(() => {
  let result = employes.value
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(e =>
      e.nom?.toLowerCase().includes(q) ||
      e.prenom?.toLowerCase().includes(q) ||
      e.email?.toLowerCase().includes(q)
    )
  }
  if (filtreDepartement.value) {
    result = result.filter(e => String(e.idDepartement) === String(filtreDepartement.value))
  }
  return result
})

const toRequest = () => ({
  nomEmploye: form.value.nom.toUpperCase(),
  prenomEmploye: form.value.prenom,
  emailEmploye: form.value.email,
  telephone: form.value.telephone || null,
  sexe: form.value.sexe,
  etatCivil: form.value.etatCivil,
  dateEmbauche: form.value.dateEmbauche,
  poste: form.value.poste,
  salaireBase: Number(form.value.salaireBase),
  statutEmploye: form.value.statut || 'Actif',
  idDepartement: Number(form.value.idDepartement)
})

const charger = async () => {
  try {
    const [empRes, deptRes] = await Promise.all([
      employesAPI.getAll(ALL_PAGE),
      departementsAPI.getAll()
    ])
    employes.value = unwrapList(empRes.data).map(mapEmploye)
    departements.value = unwrapList(deptRes.data)
  } catch (err) {
    error(err.response?.data?.message || 'Erreur chargement employés')
  }
}

const openAddModal = () => {
  isEditing.value = false
  currentId.value = null
  form.value = emptyForm()
  showModal.value = true
}

const editEmploye = (employe) => {
  isEditing.value = true
  currentId.value = employe.id
  form.value = {
    nom: employe.nom,
    prenom: employe.prenom,
    email: employe.email,
    telephone: employe.telephone || '',
    poste: employe.poste,
    idDepartement: employe.idDepartement,
    salaireBase: employe.salaireBase,
    sexe: employe.sexe || 'M',
    etatCivil: employe.etatCivil || 'Célibataire',
    dateEmbauche: employe.dateEmbauche || emptyForm().dateEmbauche,
    statut: employe.statut || 'Actif'
  }
  showModal.value = true
}

const deleteEmploye = async (employe) => {
  if (!confirm(`Désactiver ${employe.prenom} ${employe.nom} ?`)) return
  try {
    await employesAPI.delete(employe.id)
    success(`Employé ${employe.prenom} ${employe.nom} désactivé`)
    await charger()
  } catch (err) {
    error(err.response?.data?.message || 'Erreur suppression')
  }
}

const renvoyerIdentifiants = async (employe) => {
  if (!confirm(`Régénérer le mot de passe de ${employe.prenom} ${employe.nom} (${employe.email}) ?`)) return
  try {
    const { data } = await employesAPI.renvoyerIdentifiants(employe.id)
    if (data?.emailEnvoye) {
      success(`Nouveau mot de passe envoyé à ${employe.email}`)
    } else if (data?.motDePasseTemporaire) {
      const msg = `Email non envoyé.\nLogin : ${employe.email}\nMot de passe : ${data.motDePasseTemporaire}`
      window.alert(msg)
      success(`MDP temporaire : ${data.motDePasseTemporaire}`)
    } else {
      success('Identifiants régénérés')
    }
  } catch (err) {
    error(err.response?.data?.message || 'Erreur renvoi identifiants')
  }
}

const saveEmploye = async () => {
  saving.value = true
  try {
    const payload = toRequest()
    if (isEditing.value) {
      await employesAPI.update(currentId.value, payload)
      success('Employé modifié avec succès')
    } else {
      const { data } = await employesAPI.create(payload)
      if (data?.emailEnvoye) {
        success('Employé créé — mot de passe unique envoyé à ' + (payload.emailEmploye || 'l’email'))
      } else if (data?.motDePasseTemporaire) {
        window.alert(`Email non envoyé.\nLogin : ${payload.emailEmploye}\nMot de passe : ${data.motDePasseTemporaire}`)
        success(`Employé créé — MDP temporaire : ${data.motDePasseTemporaire}`)
      } else {
        success('Employé créé avec compte utilisateur')
      }
    }
    closeModal()
    await charger()
  } catch (err) {
    const data = err.response?.data
    const msg = typeof data === 'object' && data?.message
      ? data.message
      : (typeof data === 'object' ? Object.values(data).join(', ') : null)
    error(msg || 'Erreur enregistrement')
  } finally {
    saving.value = false
  }
}

const closeModal = () => {
  showModal.value = false
  isEditing.value = false
  currentId.value = null
}

onMounted(charger)
</script>
