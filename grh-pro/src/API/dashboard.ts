import apiClient from './clients'

export const dashboardAPI = {
  getEntreprise() {
    return apiClient.get('/dashboard/entreprise')
  }
}
