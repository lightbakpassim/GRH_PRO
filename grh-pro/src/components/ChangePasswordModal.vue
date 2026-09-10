<template>
  <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="close">
    <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6 space-y-4">
      <h3 class="text-lg font-semibold text-gray-800">Changer le mot de passe</h3>
      <form @submit.prevent="submit" class="space-y-3">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Mot de passe actuel</label>
          <input v-model="form.motDePasseActuel" type="password" required class="w-full px-3 py-2 border rounded-md" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Nouveau mot de passe</label>
          <input v-model="form.nouveauMotDePasse" type="password" minlength="8" required class="w-full px-3 py-2 border rounded-md" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Confirmer</label>
          <input v-model="confirm" type="password" minlength="8" required class="w-full px-3 py-2 border rounded-md" />
        </div>
        <div class="flex flex-col-reverse sm:flex-row sm:justify-end gap-2 pt-2">
          <button type="button" @click="close" class="w-full sm:w-auto px-4 py-2.5 min-h-11 bg-gray-200 rounded-md">Annuler</button>
          <button type="submit" :disabled="loading" class="w-full sm:w-auto px-4 py-2.5 min-h-11 bg-teal-700 text-white rounded-md disabled:opacity-50">
            Enregistrer
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { authAPI } from '@/API/auth'
import { useToast } from '@/composable/useToast'

const props = defineProps({ open: { type: Boolean, default: false } })
const emit = defineEmits(['close'])
const { success, error } = useToast()
const loading = ref(false)
const confirm = ref('')
const form = reactive({ motDePasseActuel: '', nouveauMotDePasse: '' })

watch(() => props.open, (v) => {
  if (v) {
    form.motDePasseActuel = ''
    form.nouveauMotDePasse = ''
    confirm.value = ''
  }
})

const close = () => emit('close')

const submit = async () => {
  if (form.nouveauMotDePasse !== confirm.value) {
    error('La confirmation ne correspond pas')
    return
  }
  loading.value = true
  try {
    await authAPI.changePassword(form)
    success('Mot de passe mis à jour')
    close()
  } catch (err) {
    error(err.response?.data?.message || 'Échec du changement de mot de passe')
  } finally {
    loading.value = false
  }
}
</script>
