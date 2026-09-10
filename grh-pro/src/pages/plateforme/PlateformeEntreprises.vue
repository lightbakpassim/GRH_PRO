<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
      <div>
        <h1 class="text-2xl font-semibold text-slate-900">Entreprises</h1>
        <p class="text-slate-500 text-sm mt-1">Créer un compte DG et suivre chaque organisation</p>
      </div>
      <button
          type="button"
          class="px-4 py-2.5 bg-teal-800 text-white rounded-lg text-sm font-medium hover:bg-teal-900"
          @click="openCreate = true"
      >
        + Nouvelle entreprise
      </button>
    </div>

    <div class="bg-white border border-slate-200 rounded-xl overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full text-sm">
          <thead class="bg-slate-50 text-left text-slate-500">
            <tr>
              <th class="px-4 py-3 font-medium">Entreprise</th>
              <th class="px-4 py-3 font-medium">DG</th>
              <th class="px-4 py-3 font-medium">Effectifs</th>
              <th class="px-4 py-3 font-medium">Dernière connexion DG</th>
              <th class="px-4 py-3 font-medium">Statut</th>
              <th class="px-4 py-3 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6" class="px-4 py-8 text-center text-slate-400">Chargement…</td>
            </tr>
            <tr v-else-if="!entreprises.length">
              <td colspan="6" class="px-4 py-8 text-center text-slate-400">Aucune entreprise</td>
            </tr>
            <tr
                v-for="e in entreprises"
                :key="e.idEntreprise"
                class="border-t border-slate-100 hover:bg-slate-50/80"
            >
              <td class="px-4 py-3">
                <div class="font-medium text-slate-900">{{ e.nomEntreprise }}</div>
                <div class="text-xs text-slate-400">{{ e.emailContact || '—' }}</div>
              </td>
              <td class="px-4 py-3 text-slate-700">{{ e.dgLogin || '—' }}</td>
              <td class="px-4 py-3 text-slate-700">
                {{ e.nbAdmins }} admin · {{ e.nbEmployes }} emp.
              </td>
              <td class="px-4 py-3 text-slate-600">
                {{ formatDate(e.dgDerniereConnexion) }}
              </td>
              <td class="px-4 py-3">
                <span
                    :class="e.statut === 'Actif'
                      ? 'bg-emerald-50 text-emerald-700'
                      : 'bg-red-50 text-red-700'"
                    class="inline-flex px-2 py-0.5 rounded-full text-xs font-medium"
                >
                  {{ e.statut }}
                </span>
              </td>
              <td class="px-4 py-3 text-right space-x-2 whitespace-nowrap">
                <button
                    v-if="e.statut === 'Actif'"
                    type="button"
                    class="text-xs font-medium text-amber-700 hover:underline"
                    @click="askSuspend(e)"
                >
                  Déconnecter
                </button>
                <button
                    v-else
                    type="button"
                    class="text-xs font-medium text-teal-800 hover:underline"
                    @click="reactiver(e)"
                >
                  Réactiver
                </button>
                <button
                    type="button"
                    class="text-xs font-medium text-red-700 hover:underline"
                    @click="askDelete(e)"
                >
                  Supprimer
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal création -->
    <div
        v-if="openCreate"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
        @click.self="openCreate = false"
    >
      <div class="bg-white rounded-xl shadow-xl w-full max-w-lg p-6 space-y-4">
        <h2 class="text-lg font-semibold text-slate-900">Créer une entreprise + compte DG</h2>
        <form class="space-y-3" @submit.prevent="creer">
          <div>
            <label class="block text-sm text-slate-700 mb-1">Nom de l’entreprise</label>
            <input v-model="form.nomEntreprise" required class="input" />
          </div>
          <div>
            <label class="block text-sm text-slate-700 mb-1">Email contact</label>
            <input v-model="form.emailContact" type="email" class="input" />
          </div>
          <div>
            <label class="block text-sm text-slate-700 mb-1">Téléphone</label>
            <input v-model="form.telephone" class="input" />
          </div>
          <div>
            <label class="block text-sm text-slate-700 mb-1">Login DG (email)</label>
            <input v-model="form.dgLogin" type="email" required class="input" />
          </div>
          <div>
            <label class="block text-sm text-slate-700 mb-1">Mot de passe DG (optionnel)</label>
            <input v-model="form.dgMotDePasse" type="text" minlength="8" class="input" placeholder="Généré automatiquement si vide" />
          </div>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-3 py-2 text-sm text-slate-600" @click="openCreate = false">Annuler</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 bg-teal-800 text-white rounded-lg text-sm disabled:opacity-50">
              {{ saving ? 'Création…' : 'Créer' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Mot de passe créé -->
    <div
        v-if="createdCreds"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
        @click.self="createdCreds = null"
    >
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6 space-y-3">
        <h2 class="text-lg font-semibold text-slate-900">Entreprise créée</h2>
        <p class="text-sm text-slate-600">Conservez ces identifiants (modifiables après première connexion) :</p>
        <div class="bg-slate-50 rounded-lg p-3 text-sm font-mono space-y-3">
          <div>
            <div class="text-xs text-slate-500 mb-1 font-sans">Compte DG</div>
            <div>Login : {{ createdCreds.dgLogin }}</div>
            <div>MDP : {{ createdCreds.dgMotDePasseTemporaire }}</div>
          </div>
          <div class="border-t border-slate-200 pt-3">
            <div class="text-xs text-slate-500 mb-1 font-sans">Compte RH (Admin)</div>
            <div>Login : {{ createdCreds.rhLogin }}</div>
            <div>MDP : {{ createdCreds.rhMotDePasseTemporaire }}</div>
          </div>
        </div>
        <button type="button" class="w-full py-2 bg-teal-800 text-white rounded-lg text-sm" @click="createdCreds = null">
          Fermer
        </button>
      </div>
    </div>

    <!-- Suspension -->
    <div
        v-if="suspendTarget"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
        @click.self="suspendTarget = null"
    >
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6 space-y-4">
        <h2 class="text-lg font-semibold text-slate-900">Déconnecter l’entreprise</h2>
        <p class="text-sm text-slate-600">
          « {{ suspendTarget.nomEntreprise }} » ne pourra plus se connecter (DG, Admin, Employés).
        </p>
        <textarea
            v-model="motif"
            rows="3"
            class="input"
            placeholder="Motif (incident, impayé, sécurité…)"
        />
        <div class="flex justify-end gap-2">
          <button type="button" class="px-3 py-2 text-sm" @click="suspendTarget = null">Annuler</button>
          <button type="button" class="px-4 py-2 bg-red-700 text-white rounded-lg text-sm" @click="confirmSuspend">
            Confirmer la déconnexion
          </button>
        </div>
      </div>
    </div>
    <!-- Suppression -->
    <div
        v-if="deleteTarget"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
        @click.self="deleteTarget = null"
    >
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6 space-y-4">
        <h2 class="text-lg font-semibold text-slate-900">Supprimer l’entreprise</h2>
        <p class="text-sm text-slate-600">
          « {{ deleteTarget.nomEntreprise }} » sera <strong>définitivement</strong> effacée
          (comptes, employés, pointages, congés, paiements, historique).
        </p>
        <div class="flex justify-end gap-2">
          <button type="button" class="px-3 py-2 text-sm" @click="deleteTarget = null">Annuler</button>
          <button
              type="button"
              class="px-4 py-2 bg-red-700 text-white rounded-lg text-sm disabled:opacity-50"
              :disabled="deleting"
              @click="confirmDelete"
          >
            {{ deleting ? 'Suppression…' : 'Confirmer la suppression' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { plateformeAPI } from '@/API/plateforme'
import { useToast } from '@/composable/useToast'
import dayjs from 'dayjs'

const { success, error } = useToast()
const entreprises = ref([])
const loading = ref(true)
const openCreate = ref(false)
const saving = ref(false)
const createdCreds = ref(null)
const suspendTarget = ref(null)
const deleteTarget = ref(null)
const deleting = ref(false)
const motif = ref('')

const form = reactive({
  nomEntreprise: '',
  emailContact: '',
  telephone: '',
  dgLogin: '',
  dgMotDePasse: ''
})

const formatDate = (d) => (d ? dayjs(d).format('DD/MM/YYYY HH:mm') : 'Jamais')

const load = async () => {
  loading.value = true
  try {
    const { data } = await plateformeAPI.listerEntreprises()
    entreprises.value = data
  } catch (e) {
    error(e.response?.data?.message || 'Chargement impossible')
  } finally {
    loading.value = false
  }
}

const creer = async () => {
  saving.value = true
  try {
    const payload = {
      nomEntreprise: form.nomEntreprise.trim(),
      emailContact: form.emailContact || undefined,
      telephone: form.telephone || undefined,
      dgLogin: form.dgLogin.trim(),
      dgMotDePasse: form.dgMotDePasse || undefined
    }
    const { data } = await plateformeAPI.creerEntreprise(payload)
    success('Entreprise créée')
    openCreate.value = false
    createdCreds.value = data
    Object.assign(form, { nomEntreprise: '', emailContact: '', telephone: '', dgLogin: '', dgMotDePasse: '' })
    await load()
  } catch (e) {
    error(e.response?.data?.message || 'Création échouée')
  } finally {
    saving.value = false
  }
}

const askSuspend = (e) => {
  suspendTarget.value = e
  motif.value = ''
}

const confirmSuspend = async () => {
  try {
    await plateformeAPI.suspendre(suspendTarget.value.idEntreprise, motif.value || undefined)
    success('Entreprise déconnectée')
    suspendTarget.value = null
    await load()
  } catch (e) {
    error(e.response?.data?.message || 'Suspension échouée')
  }
}

const reactiver = async (e) => {
  try {
    await plateformeAPI.reactiver(e.idEntreprise)
    success('Entreprise réactivée')
    await load()
  } catch (err) {
    error(err.response?.data?.message || 'Réactivation échouée')
  }
}

const askDelete = (e) => {
  deleteTarget.value = e
}

const confirmDelete = async () => {
  deleting.value = true
  try {
    await plateformeAPI.supprimer(deleteTarget.value.idEntreprise)
    success('Entreprise supprimée')
    deleteTarget.value = null
    await load()
  } catch (err) {
    error(err.response?.data?.message || 'Suppression échouée')
  } finally {
    deleting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.input {
  @apply w-full px-3 py-2 rounded-lg border border-slate-300 text-slate-900 focus:outline-none focus:ring-2 focus:ring-teal-700/30 focus:border-teal-700;
}
</style>
