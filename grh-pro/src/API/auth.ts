import apiClient from './clients'

export const authAPI = {
  login(credentials: { login: string; motDePasse: string }) {
    return apiClient.post('/auth/login', credentials)
  },
  changePassword(payload: { motDePasseActuel: string; nouveauMotDePasse: string }) {
    return apiClient.patch('/auth/change-password', payload)
  }
}
