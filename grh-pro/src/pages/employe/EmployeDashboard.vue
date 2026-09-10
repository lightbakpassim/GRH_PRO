<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-800">Mon espace</h1>
      <p class="text-gray-500 mt-1">{{ authStore.user?.name }}</p>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard
          title="Heures ce mois"
          :value="stats.heuresCeMois + ' h'"
          :icon="ClockIcon"
          icon-bg="blue"
          subtitle="Total heures travaillées"
      />
      <StatCard
          title="Heures Supp"
          :value="stats.heuresSupp + ' h'"
          :icon="BoltIcon"
          icon-bg="orange"
          subtitle="Heures supplémentaires"
      />
      <StatCard
          title="Congés Restants"
          :value="stats.congesRestants + ' j'"
          :icon="CalendarIcon"
          icon-bg="green"
          subtitle="Solde estimé (30 j/an)"
      />
      <StatCard
          title="Paiement mois"
          :value="stats.paiementStatus ? 'Effectué' : 'En attente'"
          :icon="CreditCardIcon"
          icon-bg="blue"
          subtitle="Salaire du mois"
      />
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
        <ClockIcon class="w-5 h-5 text-blue-500" />
        Pointage du jour
      </h2>
      <div class="flex items-center justify-between flex-wrap gap-4">
        <div class="flex items-center gap-4 flex-wrap">
          <div class="text-center">
            <p class="text-sm text-gray-500">Aujourd'hui</p>
            <p class="text-lg font-semibold">{{ todayDate }}</p>
          </div>
          <div class="w-px h-8 bg-gray-200 hidden sm:block"></div>
          <div class="text-center">
            <p class="text-sm text-gray-500">Entrée</p>
            <p class="text-lg font-semibold text-green-600">{{ pointageActuel?.entree || '--:--' }}</p>
          </div>
          <div class="text-center">
            <p class="text-sm text-gray-500">Sortie</p>
            <p class="text-lg font-semibold text-red-600">{{ pointageActuel?.sortie || '--:--' }}</p>
          </div>
          <div class="text-center">
            <p class="text-sm text-gray-500">H. Trav</p>
            <p class="text-lg font-semibold">{{ pointageActuel ? Number(pointageActuel.heuresTrav).toFixed(2) : '0' }} h</p>
          </div>
        </div>
        <router-link
            to="/employe/pointage"
            class="bg-blue-600 text-white px-6 py-2 rounded-md font-medium hover:bg-blue-700"
        >
          Gérer le pointage
        </router-link>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">Mon activité ce mois</h2>
      <DataTable :columns="columns" :data="mesPointages">
        <template #column-statut="{ row }">
          <StatusBadge :status="row.statut" />
        </template>
        <template #column-heuresTrav="{ row }">
          {{ Number(row.heuresTrav).toFixed(2) }} h
        </template>
        <template #column-heuresSupp="{ row }">
          <span :class="row.heuresSupp > 0 ? 'text-orange-600 font-medium' : ''">
            {{ Number(row.heuresSupp).toFixed(2) }} h
          </span>
        </template>
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/data.js'
import { pointagesAPI } from '@/API/pointages'
import { congesAPI } from '@/API/conges'
import { paiementsAPI } from '@/API/paiements'
import { mapPointage, mapConge, mapPaiement } from '@/utils/mappers'
import { useToast } from '@/composable/useToast'
import StatCard from '@/components/StatCard.vue'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { ClockIcon, BoltIcon, CalendarIcon, CreditCardIcon } from '@heroicons/vue/24/outline'
import dayjs from 'dayjs'

const authStore = useAuthStore()
const { error } = useToast()

const todayDate = ref(dayjs().format('DD/MM/YYYY'))
const todayISO = dayjs().format('YYYY-MM-DD')
const mesPointages = ref([])
const stats = ref({ heuresCeMois: 0, heuresSupp: 0, congesRestants: 30, paiementStatus: false })

const columns = [
  { key: 'date', label: 'Date', type: 'date' },
  { key: 'entree', label: 'Entrée' },
  { key: 'sortie', label: 'Sortie' },
  { key: 'heuresTrav', label: 'H. Trav' },
  { key: 'heuresSupp', label: 'H. Supp' },
  { key: 'statut', label: 'Statut', type: 'badge' }
]

const pointageActuel = computed(() => mesPointages.value.find(p => p.date === todayISO))

const charger = async () => {
  try {
    const now = new Date()
    const mois = now.getMonth() + 1
    const annee = now.getFullYear()

    const [suiviRes, congeRes, paiementRes] = await Promise.all([
      pointagesAPI.getMonSuivi({ mois, annee }),
      congesAPI.getMesConges(),
      paiementsAPI.getMesBulletins()
    ])

    const pointages = suiviRes.data.map(mapPointage)
    mesPointages.value = pointages.sort((a, b) => new Date(b.date) - new Date(a.date))

    const conges = congeRes.data.map(mapConge)
    const pris = conges
      .filter(c => c.statut === 'Approuvée')
      .reduce((s, c) => s + (c.nbJours || 0), 0)

    const paiements = paiementRes.data.map(mapPaiement)
    const paiementMois = paiements.find(p => p.mois === mois && p.annee === annee)

    stats.value = {
      heuresCeMois: Number(pointages.reduce((s, p) => s + p.heuresTrav, 0).toFixed(1)),
      heuresSupp: Number(pointages.reduce((s, p) => s + p.heuresSupp, 0).toFixed(1)),
      congesRestants: Math.max(0, 30 - pris),
      paiementStatus: paiementMois?.statut === 'Effectué'
    }
  } catch (err) {
    error(err.response?.data?.message || 'Erreur chargement dashboard')
  }
}

onMounted(charger)
</script>
