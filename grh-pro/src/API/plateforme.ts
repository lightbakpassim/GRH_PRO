import apiClient from './clients'

export const plateformeAPI = {
  dashboard() {
    return apiClient.get('/plateforme/dashboard')
  },
  listerEntreprises() {
    return apiClient.get('/plateforme/entreprises')
  },
  getEntreprise(id: number) {
    return apiClient.get(`/plateforme/entreprises/${id}`)
  },
  creerEntreprise(payload: {
    nomEntreprise: string
    emailContact?: string
    telephone?: string
    dgLogin: string
    dgMotDePasse?: string
  }) {
    return apiClient.post('/plateforme/entreprises', payload)
  },
  suspendre(id: number, motif?: string) {
    return apiClient.patch(`/plateforme/entreprises/${id}/suspendre`, { motif })
  },
  reactiver(id: number) {
    return apiClient.patch(`/plateforme/entreprises/${id}/reactiver`)
  },
  supprimer(id: number) {
    return apiClient.delete(`/plateforme/entreprises/${id}`)
  }
}
