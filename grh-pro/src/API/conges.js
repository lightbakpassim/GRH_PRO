import apiClient from './clients.js'

export const congesAPI = {
    getAll() {
        return apiClient.get('/conges')
    },

    getByEmploye(id) {
        return apiClient.get(`/conges/employe/${id}`)
    },

    create(demande) {
        return apiClient.post('/conges', demande)
    },

    validate(id) {
        return apiClient.patch(`/conges/${id}/approuver`)
    },

    reject(id) {
        return apiClient.patch(`/conges/${id}/refuser`)
    },

    getSolde(employeId) {
        return apiClient.get(`/conges/solde/${employeId}`)
    }
}