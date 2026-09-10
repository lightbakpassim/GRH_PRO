<template>
  <div class="space-y-6 max-w-xl">
    <div>
      <h1 class="text-2xl font-semibold text-slate-900">Mon compte</h1>
      <p class="text-slate-500 text-sm mt-1">
        Identifiants SuperAdmin — modification protégée par le mot de passe actuel
      </p>
    </div>

    <div class="bg-white border border-slate-200 rounded-xl p-5 sm:p-6 space-y-5">
      <div class="text-sm text-slate-600">
        <span class="text-slate-400 uppercase text-[11px] tracking-wide">Rôle</span>
        <p class="font-medium text-slate-900 mt-0.5">SuperAdmin · Plateforme</p>
      </div>

      <form class="space-y-4" @submit.prevent="enregistrer">
        <div>
          <label class="block text-sm text-slate-700 mb-1">Login (email)</label>
          <input
              v-model="form.login"
              type="email"
              required
              class="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-teal-700/30"
          />
        </div>

        <div class="border-t border-slate-100 pt-4">
          <p class="text-sm font-medium text-slate-800 mb-3">Sécurité</p>
          <div class="space-y-3">
            <div>
              <label class="block text-sm text-slate-700 mb-1">Mot de passe actuel *</label>
              <input
                  v-model="form.motDePasseActuel"
                  type="password"
                  required
                  autocomplete="current-password"
                  class="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-teal-700/30"
              />
            </div>
            <div>
              <label class="block text-sm text-slate-700 mb-1">Nouveau mot de passe</label>
              <input
                  v-model="form.nouveauMotDePasse"
                  type="password"
                  minlength="8"
                  autocomplete="new-password"
                  placeholder="Laisser vide pour ne pas changer"
                  class="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-teal-700/30"
              />
            </div>
            <div>
              <label class="block text-sm text-slate-700 mb-1">Confirmer le nouveau mot de passe</label>
              <input
                  v-model="confirm"
                  type="password"
                  minlength="8"
                  autocomplete="new-password"
                  class="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-teal-700/30"
              />
            </div>
          </div>
        </div>

        <div class="flex justify-end pt-2">
          <button
              type="submit"
              :disabled="saving"
              class="px-4 py-2.5 bg-teal-800 text-white rounded-lg text-sm font-medium hover:bg-teal-900 disabled:opacity-50"
          >
            {{ saving ? 'Enregistrement…' : 'Enregistrer' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { authAPI } from '@/API/auth'
import { useAuthStore } from '@/stores/data.js'
import { useToast } from '@/composable/useToast'

const { success, error } = useToast()
const authStore = useAuthStore()
const saving = ref(false)
const confirm = ref('')

const form = reactive({
  login: '',
  motDePasseActuel: '',
  nouveauMotDePasse: ''
})

onMounted(async () => {
  form.login = authStore.user?.email || ''
  try {
    const { data } = await authAPI.me()
    form.login = data.login
    authStore.applySession(data)
  } catch {
    /* garde le login localStorage */
  }
})

const enregistrer = async () => {
  const loginChanged = form.login.trim().toLowerCase() !== (authStore.user?.email || '').toLowerCase()
  const mdpChange = !!form.nouveauMotDePasse

  if (!loginChanged && !mdpChange) {
    error('Aucune modification à enregistrer')
    return
  }
  if (mdpChange && form.nouveauMotDePasse !== confirm.value) {
    error('La confirmation du mot de passe ne correspond pas')
    return
  }

  saving.value = true
  try {
    const payload = {
      login: form.login.trim(),
      motDePasseActuel: form.motDePasseActuel,
      nouveauMotDePasse: mdpChange ? form.nouveauMotDePasse : undefined
    }
    const { data } = await authAPI.updateProfil(payload)
    authStore.applySession(data)
    form.motDePasseActuel = ''
    form.nouveauMotDePasse = ''
    confirm.value = ''
    success('Compte mis à jour')
  } catch (err) {
    error(err.response?.data?.message || 'Échec de la mise à jour')
  } finally {
    saving.value = false
  }
}
</script>
