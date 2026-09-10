import apiClient from './clients'

export const congesAPI = {
  getAll(params: Record<string, unknown> = {}) {
    return apiClient.get('/conges', { params })
  },
  getEnAttente() {
    return apiClient.get('/conges/en-attente')
  },
  getMesConges() {
    return apiClient.get('/conges/mes-conges')
  },
  getById(id: number) {
    return apiClient.get(`/conges/${id}`)
  },
  create(demande: Record<string, unknown>) {
    return apiClient.post('/conges', demande)
  },
  validate(id: number) {
    return apiClient.patch(`/conges/${id}/approuver`)
  },
  reject(id: number) {
    return apiClient.patch(`/conges/${id}/refuser`)
  },
  delete(id: number) {
    return apiClient.delete(`/conges/${id}`)
  }
}
