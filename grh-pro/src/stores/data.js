import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '@/API/auth'

export const useAuthStore = defineStore('auth', () => {
    const user = ref(null)
    const token = ref(null)
    const isAuthenticated = computed(() => !!user.value && !!token.value)

    const login = async (email, password) => {
        const { data } = await authAPI.login({
            login: email,
            motDePasse: password
        })

        user.value = {
            id: data.idEmploye,
            idEmploye: data.idEmploye,
            name: data.nomComplet,
            email: data.login,
            role: data.role
        }
        token.value = data.token
        localStorage.setItem('auth_token', data.token)
        localStorage.setItem('user', JSON.stringify(user.value))

        return { success: true, user: user.value }
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
            try {
                user.value = JSON.parse(storedUser)
                token.value = storedToken
            } catch {
                logout()
            }
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
