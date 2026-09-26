import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// Données mockées pour la connexion
const MOCK_USERS = {
    'admin@grh.tg': {
        id: 1,
        name: 'Admin GRH',
        email: 'admin@grh.tg',
        role: 'Admin',
        password: 'admin123'
    },
    'light@grh.tg': {
        id: 2,
        name: 'Light B.',
        email: 'light@grh.tg',
        role: 'Employé',
        password: 'emp123'
    }
}

export const useAuthStore = defineStore('auth', () => {
    const user = ref(null)
    const token = ref(null)
    const isAuthenticated = computed(() => !!user.value)

    const login = async (email, password) => {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const foundUser = MOCK_USERS[email]
                if (foundUser && foundUser.password === password) {
                    user.value = {
                        id: foundUser.id,
                        name: foundUser.name,
                        email: foundUser.email,
                        role: foundUser.role
                    }
                    token.value = 'mock-jwt-token-' + Date.now()
                    localStorage.setItem('auth_token', token.value)
                    localStorage.setItem('user', JSON.stringify(user.value))
                    resolve({ success: true, user: user.value })
                } else {
                    reject({ success: false, message: 'Email ou mot de passe incorrect' })
                }
            }, 500)
        })
    }

    const logout = () => {
        user.value = null
        token.value = null
        localStorage.removeItem('auth_token')
        localStorage.removeItem('user')
    }

    const checkAuth = () => {
        const storedUser = localStorage.getItem('user')
        const storedToken = localStorage.getItem('auth_token')
        if (storedUser && storedToken) {
            user.value = JSON.parse(storedUser)
            token.value = storedToken
        }
    }

    checkAuth()

    return {
        user,
        token,
        isAuthenticated,
        login,
        logout,
        checkAuth
    }
})