import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/data.js'
import { useDataStore } from '@/stores/dataStore.js'

// Auth
import LoginPage from '@/pages/LoginPage.vue'

// Admin
import AdminLayout from '@/pages/admin/AdminLayout.vue'
import AdminDashboard from '@/pages/admin/AdminDashboard.vue'
import AdminEmployes from '@/pages/admin/AdminEmployes.vue'
import AdminPointages from '@/pages/admin/AdminPointages.vue'
import AdminConges from '@/pages/admin/AdminConges.vue'
import AdminPaiement from '@/pages/admin/AdminPaiement.vue'

// Employé
import EmployeLayout from '@/pages/employe/EmployeLayout.vue'
import EmployeDashboard from '@/pages/employe/EmployeDashboard.vue'
import EmployePointage from '@/pages/employe/EmployePointage.vue'
import EmployeConges from '@/pages/employe/EmployeConges.vue'
import EmployePaiement from '@/pages/employe/EmployePaiement.vue'

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'Login', component: LoginPage },

    // Routes Admin
    {
        path: '/admin',
        component: AdminLayout,
        meta: { requiresAuth: true, role: 'Admin' },
        children: [
            { path: '', redirect: '/admin/dashboard' },
            { path: 'dashboard', name: 'AdminDashboard', component: AdminDashboard },
            { path: 'employes', name: 'AdminEmployes', component: AdminEmployes },
            { path: 'pointages', name: 'AdminPointages', component: AdminPointages },
            { path: 'conges', name: 'AdminConges', component: AdminConges },
            { path: 'paiement', name: 'AdminPaiement', component: AdminPaiement },
        ]
    },

    // Routes Employé
    {
        path: '/employe',
        component: EmployeLayout,
        meta: { requiresAuth: true, role: 'Employé' },
        children: [
            { path: '', redirect: '/employe/dashboard' },
            { path: 'dashboard', name: 'EmployeDashboard', component: EmployeDashboard },
            { path: 'pointage', name: 'EmployePointage', component: EmployePointage },
            { path: 'conges', name: 'EmployeConges', component: EmployeConges },
            { path: 'paiement', name: 'EmployePaiement', component: EmployePaiement },
        ]
    },

    // Fallback 404 - redirige vers login
    { path: '/:pathMatch(.*)*', redirect: '/login' }
]

// Création du router APRÈS la définition des routes
const router = createRouter({
    history: createWebHistory(),
    routes
})

// Guard global - APRÈS la création du router
router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()
    const isAuthenticated = authStore.isAuthenticated
    const userRole = authStore.user?.role

    // Vérifier l'authentification
    if (to.meta.requiresAuth && !isAuthenticated) {
        next('/login')
        return
    }

    // Vérifier le rôle
    if (to.meta.role && userRole !== to.meta.role) {
        const redirectPath = userRole === 'Admin' ? '/admin' : '/employe'
        next(redirectPath)
        return
    }

    // Redirection si déjà connecté sur login
    if (to.path === '/login' && isAuthenticated) {
        const redirectPath = userRole === 'Admin' ? '/admin' : '/employe'
        next(redirectPath)
        return
    }

    next()
})

export default router