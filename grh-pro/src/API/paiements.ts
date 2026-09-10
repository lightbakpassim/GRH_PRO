import apiClient from './clients'

export const paiementsAPI = {
  getAll(params: Record<string, unknown> = {}) {
    return apiClient.get('/paiements', { params })
  },
  getById(id: number) {
    return apiClient.get(`/paiements/${id}`)
  },
  getMesBulletins() {
    return apiClient.get('/paiements/mes-bulletins')
  },
  generate(paiement: Record<string, unknown>) {
    return apiClient.post('/paiements', paiement)
  },
  update(id: number, paiement: Record<string, unknown>) {
    return apiClient.put(`/paiements/${id}`, paiement)
  },
  marquerEffectue(id: number) {
    return apiClient.patch(`/paiements/${id}/effectuer`)
  },
  valider(id: number) {
    return apiClient.patch(`/paiements/${id}/valider`)
  },
  calculer(params: { idEmploye: number; mois: number; annee: number }) {
    return apiClient.get('/paiements/calculer', { params })
  },
  delete(id: number) {
    return apiClient.delete(`/paiements/${id}`)
  }
}
