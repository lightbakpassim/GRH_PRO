<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">Gestion des paiements</h1>
        <p class="text-gray-500 mt-1">Bulletins et paiements</p>
      </div>
      <button
          @click="openGenererModal"
          class="w-full sm:w-auto bg-primary text-white px-4 py-2.5 min-h-11 rounded-md font-medium transition-all duration-200 hover:bg-primary-dark active:scale-95 flex items-center justify-center gap-2"
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
        <select v-model="filtreStatut" class="w-full px-4 py-2.5 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary sm:w-40">
          <option value="">Tous statuts</option>
          <option value="Effectué">Effectué</option>
          <option value="Validé">Validé</option>
          <option value="En_attente">En attente</option>
        </select>
        <select v-model="filtreMois" class="w-full px-4 py-2.5 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary sm:w-40">
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
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
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
            <th class="px-3 sm:px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Employé</th>
            <th class="px-3 sm:px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Mois</th>
            <th class="hidden md:table-cell px-3 sm:px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Salaire base</th>
            <th class="hidden md:table-cell px-3 sm:px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">HS</th>
            <th class="px-3 sm:px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Net</th>
            <th class="px-3 sm:px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Statut</th>
            <th class="px-3 sm:px-6 py-3 text-right text-xs font-semibold text-gray-600 uppercase">Actions</th>
          </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
          <tr v-for="paiement in paiementsFiltres" :key="paiement.idPaiement" class="hover:bg-gray-50">
            <td class="px-3 sm:px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ paiement.nomCompletEmploye }}</td>
            <td class="px-3 sm:px-6 py-4 whitespace-nowrap text-sm text-gray-700">{{ getNomMois(paiement.mois) }} {{ paiement.annee }}</td>
            <td class="hidden md:table-cell px-3 sm:px-6 py-4 whitespace-nowrap text-sm text-gray-700">{{ formatCurrency(paiement.salaireBase) }}</td>
            <td class="hidden md:table-cell px-3 sm:px-6 py-4 whitespace-nowrap text-sm text-gray-700">{{ formatCurrency(paiement.heuresSuppMontant) }}</td>
            <td class="px-3 sm:px-6 py-4 whitespace-nowrap text-sm font-semibold text-primary">{{ formatCurrency(paiement.totalNet) }}</td>
            <td class="px-3 sm:px-6 py-4 whitespace-nowrap">
              <StatusBadge :status="paiement.statut" />
            </td>
            <td class="px-3 sm:px-6 py-4 whitespace-nowrap text-right">
              <div class="flex items-center justify-end gap-2">
                <button
                    v-if="paiement.statut === 'En_attente'"
                    @click="marquerEffectue(paiement)"
                    class="text-success hover:text-success/80 p-2.5 min-h-11 min-w-11 inline-flex items-center justify-center rounded transition-colors"
                    title="Marquer comme effectué"
                >
                  <CheckIcon class="w-5 h-5" />
                </button>
                <span v-else-if="paiement.statut === 'Effectué'" class="text-xs text-warning hidden sm:inline">
                  Attente employé
                </span>
                <span v-else-if="paiement.statut === 'Validé'" class="text-xs text-success hidden sm:inline">
                  Validé
                </span>
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
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md max-h-[90vh] overflow-y-auto">
        <div class="flex justify-between items-center p-4 border-b sticky top-0 bg-white">
          <h3 class="text-lg font-semibold">Générer un paiement</h3>
          <button type="button" @click="closeModal" class="text-gray-400 hover:text-gray-600 p-2">
            <XMarkIcon class="w-6 h-6" />
          </button>
        </div>
        <form @submit.prevent="genererPaiement" class="p-4 space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Employé</label>
            <select
                v-model="formPaiement.idEmploye"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                required
                @change="recalculer"
            >
              <option value="">Sélectionner un employé</option>
              <option v-for="emp in employesActifs" :key="emp.idEmploye" :value="emp.idEmploye">
                {{ emp.prenomEmploye }} {{ emp.nomEmploye }}
              </option>
            </select>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Mois</label>
              <select
                  v-model="formPaiement.mois"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                  required
                  @change="recalculer"
              >
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
              <input
                  v-model="formPaiement.annee"
                  type="number"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                  placeholder="2026"
                  required
                  @change="recalculer"
              />
            </div>
          </div>

          <div v-if="calculLoading" class="text-sm text-gray-500">Calcul en cours…</div>
          <div v-else-if="calculDetail" class="bg-slate-50 border rounded-md p-3 text-xs text-gray-600 space-y-1">
            <p>HS : {{ calculDetail.detailHs }}</p>
            <p>Retenues : {{ calculDetail.detailRetenues }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Salaire base (FCFA)</label>
            <input
                v-model="formPaiement.salaireBase"
                type="number"
                step="0.01"
                class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 focus:outline-none focus:ring-2 focus:ring-primary"
                required
                readonly
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Heures supplémentaires (FCFA)
              <span v-if="calculDetail" class="text-xs text-gray-400 font-normal">
                — {{ calculDetail.heuresSupplementaires }} h
              </span>
            </label>
            <input
                v-model="formPaiement.heuresSuppMontant"
                type="number"
                step="0.01"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                required
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Retenues (FCFA)
              <span v-if="calculDetail" class="text-xs text-gray-400 font-normal">
                — {{ calculDetail.heuresRetenues }} h
              </span>
            </label>
            <input
                v-model="formPaiement.retenues"
                type="number"
                step="0.01"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                required
            />
          </div>
          <div class="bg-blue-50 rounded-md p-3 flex justify-between items-center">
            <span class="text-sm text-gray-600">Net estimé</span>
            <span class="text-lg font-bold text-primary">{{ formatCurrency(netEstime) }}</span>
          </div>
          <div class="flex justify-end gap-3 pt-4">
            <button type="button" @click="closeModal" class="btn-secondary">Annuler</button>
            <button type="submit" :disabled="calculLoading || !formPaiement.idEmploye" class="btn-primary disabled:opacity-50">
              Générer
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useToast } from '@/composable/useToast'
import { paiementsAPI } from '@/API/paiements'
import { employesAPI } from '@/API/employes'
import { unwrapList, ALL_PAGE } from '@/utils/api'
import StatusBadge from '@/components/StatusBadge.vue'
import { PlusIcon, MagnifyingGlassIcon, CheckIcon, XMarkIcon } from '@heroicons/vue/24/outline'

const { success, error } = useToast()

const searchQuery = ref('')
const filtreStatut = ref('')
const filtreMois = ref('')
const showModal = ref(false)
const loading = ref(false)
const paiements = ref([])
const employes = ref([])

const formPaiement = ref({
  idEmploye: '',
  mois: '',
  annee: new Date().getFullYear(),
  salaireBase: '',
  heuresSuppMontant: 0,
  retenues: 0
})

const calculLoading = ref(false)
const calculDetail = ref(null)

const employesActifs = computed(() =>
  employes.value.filter(e => (e.statutEmploye || e.statut) !== 'Inactif')
)

const netEstime = computed(() => {
  const s = Number(formPaiement.value.salaireBase || 0)
  const hs = Number(formPaiement.value.heuresSuppMontant || 0)
  const r = Number(formPaiement.value.retenues || 0)
  return Math.max(0, s + hs - r)
})

const paiementsFiltres = computed(() => {
  let result = [...paiements.value]

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
  const effectues = paiementsFiltres.value.filter(p =>
    p.statut === 'Effectué' || p.statut === 'Validé'
  ).length
  const enAttente = paiementsFiltres.value.filter(p => p.statut === 'En_attente').length
  const totalNet = paiementsFiltres.value.reduce((sum, p) => sum + Number(p.totalNet || 0), 0)
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
  calculDetail.value = null
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  calculDetail.value = null
}

const recalculer = async () => {
  const { idEmploye, mois, annee } = formPaiement.value
  if (!idEmploye || !mois || !annee) return

  calculLoading.value = true
  try {
    const { data } = await paiementsAPI.calculer({
      idEmploye: Number(idEmploye),
      mois: Number(mois),
      annee: Number(annee)
    })
    calculDetail.value = data
    formPaiement.value.salaireBase = data.salaireBase
    formPaiement.value.heuresSuppMontant = data.heuresSuppMontant
    formPaiement.value.retenues = data.retenues
  } catch (err) {
    calculDetail.value = null
    error(err.response?.data?.message || 'Impossible de calculer le bulletin')
  } finally {
    calculLoading.value = false
  }
}

const genererPaiement = async () => {
  try {
    await recalculer()
    const payload = {
      idEmploye: Number(formPaiement.value.idEmploye),
      mois: Number(formPaiement.value.mois),
      annee: Number(formPaiement.value.annee),
      salaireBase: Number(formPaiement.value.salaireBase),
      heuresSuppMontant: Number(formPaiement.value.heuresSuppMontant),
      retenues: Number(formPaiement.value.retenues)
    }
    const response = await paiementsAPI.generate(payload)
    paiements.value.unshift(response.data)
    success('Paiement généré avec succès')
    closeModal()
  } catch (err) {
    error(err.response?.data?.message || 'Erreur lors de la génération')
  }
}

const marquerEffectue = async (paiement) => {
  try {
    const response = await paiementsAPI.marquerEffectue(paiement.idPaiement)
    const index = paiements.value.findIndex(p => p.idPaiement === paiement.idPaiement)
    if (index !== -1) {
      paiements.value[index] = response.data
    }
    success(`Paiement de ${paiement.nomCompletEmploye} marqué comme effectué`)
  } catch (err) {
    error(err.response?.data?.message || 'Erreur lors du marquage')
  }
}

const chargerDonnees = async () => {
  loading.value = true
  try {
    const [paiementsRes, employesRes] = await Promise.all([
      paiementsAPI.getAll(ALL_PAGE),
      employesAPI.getAll(ALL_PAGE)
    ])
    paiements.value = unwrapList(paiementsRes.data)
    employes.value = unwrapList(employesRes.data)
  } catch (err) {
    error(err.response?.data?.message || 'Erreur lors du chargement des paiements')
  } finally {
    loading.value = false
  }
}

onMounted(chargerDonnees)
</script>