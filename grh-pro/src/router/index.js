import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/data.js'

import LoginPage from '@/pages/LoginPage.vue'

import AdminLayout from '@/pages/admin/AdminLayout.vue'
import AdminDashboard from '@/pages/admin/AdminDashboard.vue'
import AdminEmployes from '@/pages/admin/AdminEmployes.vue'
import AdminPointages from '@/pages/admin/AdminPointages.vue'
import AdminConges from '@/pages/admin/AdminConges.vue'
import AdminPaiement from '@/pages/admin/AdminPaiement.vue'

import EmployeLayout from '@/pages/employe/EmployeLayout.vue'
import EmployeDashboard from '@/pages/employe/EmployeDashboard.vue'
import EmployePointage from '@/pages/employe/EmployePointage.vue'
import EmployeConges from '@/pages/employe/EmployeConges.vue'
import EmployePaiement from '@/pages/employe/EmployePaiement.vue'

import DgLayout from '@/pages/dg/DgLayout.vue'
import DgDashboard from '@/pages/dg/DgDashboard.vue'
import DgRapports from '@/pages/dg/DgRapports.vue'
import DgHistorique from '@/pages/dg/DgHistorique.vue'
import AdminRapports from '@/pages/admin/AdminRapports.vue'
import AdminDepartements from '@/pages/admin/AdminDepartements.vue'

import PlateformeLayout from '@/pages/plateforme/PlateformeLayout.vue'
import PlateformeDashboard from '@/pages/plateforme/PlateformeDashboard.vue'
import PlateformeEntreprises from '@/pages/plateforme/PlateformeEntreprises.vue'
import PlateformeCompte from '@/pages/plateforme/PlateformeCompte.vue'

const homeForRole = (role) => {
  if (role === 'SuperAdmin') return '/plateforme'
  if (role === 'Admin') return '/admin'
  if (role === 'DG') return '/dg'
  return '/employe'
}

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: LoginPage },

  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, role: 'Admin' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard', component: AdminDashboard },
      { path: 'departements', name: 'AdminDepartements', component: AdminDepartements },
      { path: 'employes', name: 'AdminEmployes', component: AdminEmployes },
      { path: 'pointages', name: 'AdminPointages', component: AdminPointages },
      { path: 'conges', name: 'AdminConges', component: AdminConges },
      { path: 'paiement', name: 'AdminPaiement', component: AdminPaiement },
      { path: 'rapports', name: 'AdminRapports', component: AdminRapports },
    ]
  },

  {
    path: '/employe',
    component: EmployeLayout,
    meta: { requiresAuth: true, role: 'Employe' },
    children: [
      { path: '', redirect: '/employe/dashboard' },
      { path: 'dashboard', name: 'EmployeDashboard', component: EmployeDashboard },
      { path: 'pointage', name: 'EmployePointage', component: EmployePointage },
      { path: 'conges', name: 'EmployeConges', component: EmployeConges },
      { path: 'paiement', name: 'EmployePaiement', component: EmployePaiement },
    ]
  },

  {
    path: '/dg',
    component: DgLayout,
    meta: { requiresAuth: true, role: 'DG' },
    children: [
      { path: '', redirect: '/dg/dashboard' },
      { path: 'dashboard', name: 'DgDashboard', component: DgDashboard },
      { path: 'rapports', name: 'DgRapports', component: DgRapports },
      { path: 'historique', name: 'DgHistorique', component: DgHistorique },
    ]
  },

  {
    path: '/plateforme',
    component: PlateformeLayout,
    meta: { requiresAuth: true, role: 'SuperAdmin' },
    children: [
      { path: '', redirect: '/plateforme/dashboard' },
      { path: 'dashboard', name: 'PlateformeDashboard', component: PlateformeDashboard },
      { path: 'entreprises', name: 'PlateformeEntreprises', component: PlateformeEntreprises },
      { path: 'compte', name: 'PlateformeCompte', component: PlateformeCompte },
    ]
  },

  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const isAuthenticated = authStore.isAuthenticated
  const userRole = authStore.user?.role

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
    return
  }

  if (to.meta.role && isAuthenticated && userRole !== to.meta.role) {
    next(homeForRole(userRole))
    return
  }

  // Toujours afficher la page de connexion (pas de skip auto via session)
  next()
})

export default router
