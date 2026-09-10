import apiClient from './clients'

export const rapportsAPI = {
  getAll() {
    return apiClient.get('/rapports')
  },
  getHistorique() {
    return apiClient.get('/rapports/historique')
  },
  viderHistorique() {
    return apiClient.delete('/rapports/historique')
  },
  getStatut() {
    return apiClient.get('/rapports/statut')
  },
  genererHebdo() {
    return apiClient.post('/rapports/generer-hebdo')
  },
  downloadPdf(id: number) {
    return apiClient.get(`/rapports/${id}/pdf`, { responseType: 'blob' })
  }
}
