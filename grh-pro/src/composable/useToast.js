import { ref } from 'vue'

const toasts = ref([])

let nextId = 0

export const useToast = () => {
    const addToast = (message, type = 'success', duration = 3000) => {
        const id = nextId++
        const toast = {
            id,
            message,
            type,
            duration
        }

        toasts.value.push(toast)

        setTimeout(() => {
            removeToast(id)
        }, duration)

        return id
    }

    const removeToast = (id) => {
        const index = toasts.value.findIndex(t => t.id === id)
        if (index !== -1) {
            toasts.value.splice(index, 1)
        }
    }

    const success = (message, duration = 3000) => addToast(message, 'success', duration)
    const error = (message, duration = 3000) => addToast(message, 'error', duration)
    const warning = (message, duration = 3000) => addToast(message, 'warning', duration)
    const info = (message, duration = 3000) => addToast(message, 'info', duration)

    return {
        toasts,
        success,
        error,
        warning,
        info,
        removeToast
    }
}