import apiClient from './clients'

export const employesAPI = {
  getAll(params: Record<string, unknown> = {}) {
    return apiClient.get('/employes', { params })
  },
  getById(id: number) {
    return apiClient.get(`/employes/${id}`)
  },
  create(employe: Record<string, unknown>) {
    return apiClient.post('/employes', employe)
  },
  update(id: number, employe: Record<string, unknown>) {
    return apiClient.put(`/employes/${id}`, employe)
  },
  delete(id: number) {
    return apiClient.delete(`/employes/${id}`)
  },
  renvoyerIdentifiants(id: number) {
    return apiClient.post(`/employes/${id}/renvoyer-identifiants`)
  }
}
