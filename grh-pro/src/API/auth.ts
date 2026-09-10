import apiClient from './clients'

export const authAPI = {
  login(credentials: { login: string; motDePasse: string }) {
    return apiClient.post('/auth/login', credentials)
  },
  me() {
    return apiClient.get('/auth/me')
  },
  updateProfil(payload: {
    login?: string
    motDePasseActuel: string
    nouveauMotDePasse?: string
  }) {
    return apiClient.patch('/auth/profil', payload)
  },
  changePassword(payload: { motDePasseActuel: string; nouveauMotDePasse: string }) {
    return apiClient.patch('/auth/change-password', payload)
  }
}
