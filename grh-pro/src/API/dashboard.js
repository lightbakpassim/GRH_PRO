import apiClient from './clients.js'

export const dashboardAPI = {
    getAdminStats() {
        return apiClient.get('/dashboard/admin/stats')
    },

    getEmployeStats(id) {
        return apiClient.get(`/dashboard/employe/${id}/stats`)
    },

    getHeuresSuppAttente() {
        return apiClient.get('/dashboard/heures-supp/attente')
    },

    getCongesAttente() {
        return apiClient.get('/dashboard/conges/attente')
    }
}