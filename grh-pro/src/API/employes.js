import apiClient from './clients.js'

export const employesAPI = {
    getAll() {
        return apiClient.get('/employes')
    },

    getById(id) {
        return apiClient.get(`/employes/${id}`)
    },

    create(employe) {
        return apiClient.post('/employes', employe)
    },

    update(id, employe) {
        return apiClient.put(`/employes/${id}`, employe)
    },

    delete(id) {
        return apiClient.delete(`/employes/${id}`)
    },

    getStats() {
        return apiClient.get('/employes/stats')
    }
}