<template>
  <div class="min-h-screen flex bg-slate-950">
    <!-- Panneau marque -->
    <div class="hidden lg:flex lg:w-[46%] relative overflow-hidden flex-col justify-between p-12 text-white">
      <div
          class="absolute inset-0 bg-gradient-to-br from-teal-900 via-teal-800 to-slate-900"
          aria-hidden="true"
      />
      <div
          class="absolute inset-0 opacity-30"
          style="background-image: radial-gradient(circle at 20% 20%, rgba(255,255,255,.18), transparent 45%), radial-gradient(circle at 80% 70%, rgba(45,212,191,.25), transparent 40%);"
          aria-hidden="true"
      />

      <div class="relative z-10">
        <AppLogo
            size="lg"
            wordmark-class="text-white text-2xl"
            accent-class="text-teal-300"
        />
      </div>

      <div class="relative z-10 max-w-md space-y-4">
        <h1 class="text-4xl font-semibold leading-tight tracking-tight">
          Gestion des ressources humaines, centralisée.
        </h1>
        <p class="text-teal-100/85 text-base leading-relaxed">
          Pointages, congés, paiements et suivi — un espace sécurisé selon votre rôle.
        </p>
      </div>

      <p class="relative z-10 text-sm text-teal-200/60">
        Accès réservé aux collaborateurs autorisés
      </p>
    </div>

    <!-- Formulaire -->
    <div class="flex-1 flex items-center justify-center p-6 sm:p-10 bg-slate-50">
      <div class="w-full max-w-md">
        <div class="lg:hidden mb-8 flex justify-center">
          <AppLogo
              size="lg"
              wordmark-class="text-teal-800 text-2xl"
              accent-class="text-teal-600"
          />
        </div>

        <div class="bg-white border border-slate-200/80 shadow-sm rounded-2xl p-8 sm:p-10">
          <div class="mb-8">
            <div class="hidden lg:flex justify-center mb-6">
              <AppLogo
                  size="xl"
                  :show-wordmark="false"
              />
            </div>
            <h2 class="text-2xl font-semibold text-slate-900 tracking-tight lg:text-center">Connexion</h2>
            <p class="text-slate-500 mt-2 text-sm">
              Saisissez votre email professionnel et votre mot de passe.
            </p>
          </div>

          <form @submit.prevent="handleLogin" class="space-y-5" novalidate>
            <div>
              <label for="login-email" class="block text-sm font-medium text-slate-700 mb-1.5">
                Email
              </label>
              <input
                  id="login-email"
                  v-model="form.email"
                  type="email"
                  autocomplete="username"
                  class="w-full px-3.5 py-2.5 rounded-lg border border-slate-300 bg-white text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-teal-700/30 focus:border-teal-700 transition"
                  :class="{ 'border-red-500 focus:ring-red-200 focus:border-red-500': errors.email }"
                  placeholder="prenom.nom@entreprise.com"
                  required
              />
              <p v-if="errors.email" class="text-xs text-red-600 mt-1.5">{{ errors.email }}</p>
            </div>

            <div>
              <label for="login-password" class="block text-sm font-medium text-slate-700 mb-1.5">
                Mot de passe
              </label>
              <div class="relative">
                <input
                    id="login-password"
                    v-model="form.password"
                    :type="showPassword ? 'text' : 'password'"
                    autocomplete="current-password"
                    class="w-full px-3.5 py-2.5 pr-11 rounded-lg border border-slate-300 bg-white text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-teal-700/30 focus:border-teal-700 transition"
                    :class="{ 'border-red-500 focus:ring-red-200 focus:border-red-500': errors.password }"
                    placeholder="••••••••"
                    required
                />
                <button
                    type="button"
                    @click="showPassword = !showPassword"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                    :aria-label="showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'"
                >
                  <EyeIcon v-if="!showPassword" class="w-5 h-5" />
                  <EyeSlashIcon v-else class="w-5 h-5" />
                </button>
              </div>
              <p v-if="errors.password" class="text-xs text-red-600 mt-1.5">{{ errors.password }}</p>
            </div>

            <button
                type="submit"
                :disabled="loading"
                class="w-full mt-2 bg-teal-800 text-white px-4 py-2.5 rounded-lg font-medium hover:bg-teal-900 active:scale-[0.99] transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="!loading">Se connecter</span>
              <span v-else class="inline-flex items-center justify-center gap-2">
                <svg class="animate-spin h-5 w-5" viewBox="0 0 24 24" aria-hidden="true">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"/>
                </svg>
                Connexion…
              </span>
            </button>
          </form>
        </div>

        <p class="mt-6 text-center text-xs text-slate-400">
          L’espace (Admin, Employé ou DG) s’ouvre automatiquement selon votre compte.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/data.js'
import { useToast } from '@/composable/useToast'
import { EyeIcon, EyeSlashIcon } from '@heroicons/vue/24/outline'
import AppLogo from '@/components/AppLogo.vue'

const router = useRouter()
const authStore = useAuthStore()
const { success, error } = useToast()

const showPassword = ref(false)
const loading = ref(false)
const errors = reactive({ email: '', password: '' })

const form = reactive({
  email: '',
  password: ''
})

const homeForRole = (role) => {
  if (role === 'Admin') return '/admin'
  if (role === 'DG') return '/dg'
  return '/employe'
}

const handleLogin = async () => {
  errors.email = ''
  errors.password = ''

  if (!form.email?.trim()) {
    errors.email = 'L’email est requis'
    return
  }
  if (!form.password) {
    errors.password = 'Le mot de passe est requis'
    return
  }

  loading.value = true

  try {
    const result = await authStore.login(form.email.trim(), form.password)

    if (result.success) {
      success('Connexion réussie. Bienvenue ' + result.user.name)
      router.push(homeForRole(result.user.role))
    } else {
      error(result.message)
    }
  } catch (err) {
    const message = err.response?.data?.message
      || err.response?.data?.error
      || 'Email ou mot de passe incorrect'
    error(message)
  } finally {
    loading.value = false
  }
}
</script>
