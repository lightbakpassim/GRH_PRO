import apiClient from './clients'

export const pointagesAPI = {
  getAll(params: Record<string, unknown> = {}) {
    return apiClient.get('/suivi-temps', { params })
  },
  getEnAttente() {
    return apiClient.get('/suivi-temps/en-attente')
  },
  getMonSuivi(params: Record<string, unknown> = {}) {
    return apiClient.get('/suivi-temps/mon-suivi', { params })
  },
  create(pointage: Record<string, unknown>) {
    return apiClient.post('/suivi-temps', pointage)
  },
  validate(id: number) {
    return apiClient.patch(`/suivi-temps/${id}/approuver`)
  },
  reject(id: number) {
    return apiClient.patch(`/suivi-temps/${id}/refuser`)
  },
  delete(id: number) {
    return apiClient.delete(`/suivi-temps/${id}`)
  }
}
