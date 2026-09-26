<template>
  <div class="min-h-screen bg-gray-100 flex items-center justify-center p-4">
    <div class="max-w-6xl w-full">
      <div class="grid lg:grid-cols-2 gap-8 items-center">
        <!-- Left side - Hero -->
        <div class="hidden lg:block">
          <div class="bg-white rounded-xl shadow-lg p-8 text-center">
            <div class="inline-flex items-center justify-center w-20 h-20 bg-blue-100 rounded-2xl mb-6">
              <svg class="w-10 h-10 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
              </svg>
            </div>
            <h1 class="text-3xl font-bold text-gray-800 mb-4">GRH Pro</h1>
            <p class="text-gray-600">Gérez vos ressources humaines efficacement</p>
            <p class="text-gray-500 text-sm mt-4">Pointages, congés, paiement et notifications tout en un seul endroit</p>

            <div class="grid grid-cols-3 gap-4 mt-8 pt-4 border-t border-gray-100">
              <div>
                <div class="text-2xl font-bold text-primary-600">24</div>
                <div class="text-xs text-gray-500">Employés</div>
              </div>
              <div>
                <div class="text-2xl font-bold text-primary-600">8</div>
                <div class="text-xs text-gray-500">Modules</div>
              </div>
              <div>
                <div class="text-2xl font-bold text-primary-600">100%</div>
                <div class="text-xs text-gray-500">Sécurisé</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right side - Login Form -->
        <div class="bg-white rounded-xl shadow-lg p-8">
          <div class="text-center mb-8">
            <h2 class="text-2xl font-bold text-gray-800">Bienvenue</h2>
            <p class="text-gray-500 mt-2">Connectez-vous à votre espace</p>
          </div>

          <div class="flex gap-3 mb-6">
            <button
                v-for="role in roles"
                :key="role.value"
                @click="selectedRole = role.value"
                :class="[
                'flex-1 py-2 rounded-lg font-medium transition-all duration-200',
                selectedRole === role.value
                  ? 'bg-primary text-white shadow-md'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              ]"
            >
              {{ role.label }}
            </button>
          </div>

          <form @submit.prevent="handleLogin" class="space-y-5">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Identifiants</label>
              <input
                  v-model="form.email"
                  type="email"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-primary focus:border-transparent transition-all"
                  :class="{ 'border-red-500': errors.email }"
                  placeholder="exemple@grh.tg"
                  required
              />
              <p v-if="errors.email" class="text-xs text-red-500 mt-1">{{ errors.email }}</p>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Mot de passe</label>
              <div class="relative">
                <input
                    v-model="form.password"
                    :type="showPassword ? 'text' : 'password'"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                    :class="{ 'border-red-500': errors.password }"
                    placeholder="••••••••"
                    required
                />
                <button
                    type="button"
                    @click="showPassword = !showPassword"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  <EyeIcon v-if="!showPassword" class="w-5 h-5" />
                  <EyeSlashIcon v-else class="w-5 h-5" />
                </button>
              </div>
              <div class="flex justify-between mt-1">
                <p v-if="errors.password" class="text-xs text-red-500">{{ errors.password }}</p>
                <button type="button" class="text-xs text-blue-600 hover:text-blue-700 ml-auto">
                  Mot de passe oublié ?
                </button>
              </div>
            </div>

            <button
                type="submit"
                :disabled="loading"
                class="w-full bg-primary text-white px-4 py-2 rounded-lg font-medium transition-all duration-200 hover:bg-primary-dark active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="!loading">Se Connecter</span>
              <span v-else class="flex items-center justify-center gap-2">
                <svg class="animate-spin h-5 w-5" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"/>
                </svg>
                Connexion...
              </span>
            </button>
          </form>

          <div class="mt-6 pt-6 border-t border-gray-200">
            <p class="text-xs text-center text-gray-400">
              Compte démo Admin: admin@grh.tg / admin123
              <br />
              Compte démo Employé: light@grh.tg / admin123
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useDataStore} from '@/stores/dataStore.js'
import { useAuthStore } from '@/stores/data.js'
import { useToast } from '@/composable/useToast'
import { EyeIcon, EyeSlashIcon } from '@heroicons/vue/24/outline'

const router = useRouter()
const authStore = useAuthStore()
const dataStore = useDataStore()
const { success, error } = useToast()

const roles = [
  { label: 'Administrateur', value: 'Admin' },
  { label: 'Employés', value: 'Employé' }
]

const selectedRole = ref('Admin')
const showPassword = ref(false)
const loading = ref(false)
const errors = reactive({ email: '', password: '' })

const form = reactive({
  email: 'admin@grh.tg',
  password: 'admin123'
})

const handleLogin = async () => {
  errors.email = ''
  errors.password = ''

  if (!form.email) {
    errors.email = 'L\'email est requis'
    return
  }
  if (!form.password) {
    errors.password = 'Le mot de passe est requis'
    return
  }

  loading.value = true

  try {
    const result = await authStore.login(form.email, form.password)

    if (result.success) {
      success('Connexion réussie ! Bienvenue ' + result.user.name)
      const redirectPath = result.user.role === 'Admin' ? '/admin' : '/employe'
      router.push(redirectPath)
    } else {
      error(result.message)
    }
  } catch (err) {
    error('Erreur de connexion')
    console.error(err)
  } finally {
    loading.value = false
  }
}
</script>