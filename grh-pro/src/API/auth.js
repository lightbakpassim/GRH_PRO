import apiClient from './clients.js'

export const authAPI = {
    login(credentials) {
        return apiClient.post('/auth/login', credentials)
    },

    logout() {
        return apiClient.post('/auth/logout')
    },

    getCurrentUser() {
        return apiClient.get('/auth/me')
    }
}