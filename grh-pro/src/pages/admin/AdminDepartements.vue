<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">Départements</h1>
        <p class="text-gray-500 mt-1">Organisation — utilisés à la création des employés</p>
      </div>
      <button
          @click="openCreate"
          class="w-full sm:w-auto bg-primary text-white px-4 py-2.5 min-h-11 rounded-md font-medium transition-all duration-200 hover:bg-primary-dark active:scale-95 flex items-center justify-center gap-2"
      >
        <PlusIcon class="w-5 h-5" />
        Ajouter
      </button>
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
      <div class="overflow-x-auto">
      <table class="min-w-full text-sm">
        <thead class="bg-gray-50 text-left">
          <tr>
            <th class="px-4 py-3 font-semibold text-gray-600">Nom</th>
            <th class="px-4 py-3 font-semibold text-gray-600">Employés</th>
            <th class="px-4 py-3 font-semibold text-gray-600 text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="d in departements" :key="d.idDepartement" class="border-t hover:bg-gray-50">
            <td class="px-4 py-3 font-medium text-gray-900">{{ d.nomDepartement }}</td>
            <td class="px-4 py-3 text-gray-600">{{ d.nombreEmployes ?? 0 }}</td>
            <td class="px-4 py-3 text-right">
              <div class="flex items-center justify-end gap-2">
                <button @click="openEdit(d)" class="text-blue-600 hover:text-blue-800 p-2.5 min-h-11 min-w-11 inline-flex items-center justify-center" title="Modifier">
                  <PencilIcon class="w-5 h-5" />
                </button>
                <button @click="supprimer(d)" class="text-red-600 hover:text-red-800 p-2.5 min-h-11 min-w-11 inline-flex items-center justify-center" title="Supprimer">
                  <TrashIcon class="w-5 h-5" />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!departements.length">
            <td colspan="3" class="px-4 py-8 text-center text-gray-400">
              Aucun département — créez-en un pour pouvoir placer les employés
            </td>
          </tr>
        </tbody>
      </table>
      </div>
    </div>

    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="closeModal">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md">
        <div class="flex justify-between items-center p-4 border-b">
          <h3 class="text-lg font-semibold">{{ editingId ? 'Modifier' : 'Créer' }} un département</h3>
          <button type="button" @click="closeModal" class="text-gray-400 hover:text-gray-600">
            <XMarkIcon class="w-6 h-6" />
          </button>
        </div>
        <form @submit.prevent="save" class="p-4 space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Nom du département</label>
            <input
                v-model="nom"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                placeholder="Ex. Ressources humaines"
                required
                maxlength="255"
            />
          </div>
          <div class="flex justify-end gap-3 pt-2">
            <button type="button" @click="closeModal" class="btn-secondary">Annuler</button>
            <button type="submit" :disabled="saving" class="btn-primary disabled:opacity-50">
              {{ saving ? 'Enregistrement…' : 'Enregistrer' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { departementsAPI } from '@/API/departements'
import { useToast } from '@/composable/useToast'
import { PlusIcon, PencilIcon, TrashIcon, XMarkIcon } from '@heroicons/vue/24/outline'

const { success, error } = useToast()
const departements = ref([])
const showModal = ref(false)
const editingId = ref(null)
const nom = ref('')
const saving = ref(false)

const charger = async () => {
  try {
    const { data } = await departementsAPI.getAll()
    departements.value = Array.isArray(data) ? data : []
  } catch (err) {
    error(err.response?.data?.message || 'Erreur chargement départements')
  }
}

const openCreate = () => {
  editingId.value = null
  nom.value = ''
  showModal.value = true
}

const openEdit = (d) => {
  editingId.value = d.idDepartement
  nom.value = d.nomDepartement
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editingId.value = null
  nom.value = ''
}

const save = async () => {
  saving.value = true
  try {
    const payload = { nomDepartement: nom.value.trim() }
    if (editingId.value) {
      await departementsAPI.update(editingId.value, payload)
      success('Département modifié')
    } else {
      await departementsAPI.create(payload)
      success('Département créé')
    }
    closeModal()
    await charger()
  } catch (err) {
    error(err.response?.data?.message || 'Erreur enregistrement')
  } finally {
    saving.value = false
  }
}

const supprimer = async (d) => {
  if (!confirm(`Supprimer le département « ${d.nomDepartement} » ?`)) return
  try {
    await departementsAPI.delete(d.idDepartement)
    success('Département supprimé')
    await charger()
  } catch (err) {
    error(err.response?.data?.message || 'Suppression impossible (employés rattachés ?)')
  }
}

onMounted(charger)
</script>
