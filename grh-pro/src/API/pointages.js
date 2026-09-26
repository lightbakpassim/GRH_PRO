import apiClient from './clients.js'

export const pointagesAPI = {
    getAll() {
        return apiClient.get('/suivi-temps')
    },

    getByEmploye(id) {
        return apiClient.get(`/suivi-temps/employe/${id}`)
    },

    start(pointage) {
        return apiClient.post('/suivi-temps', pointage)
    },

    end(id) {
        return apiClient.put(`/suivi-temps/${id}/end`)
    },

    validate(id) {
        return apiClient.patch(`/suivi-temps/${id}/approuver`)
    },

    reject(id) {
        return apiClient.patch(`/suivi-temps/${id}/refuser`)
    }
}