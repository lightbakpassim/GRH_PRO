import apiClient from './clients.js'

export const paiementsAPI = {
    // Récupérer tous les paiements (Admin)
    getAll() {
        return apiClient.get('/paiements')
    },

    // Récupérer un paiement par son id
    getById(id) {
        return apiClient.get(`/paiements/${id}`)
    },

    // Récupérer les paiements par employé
    getByEmploye(id) {
        return apiClient.get(`/paiements/employe/${id}`)
    },

    // Récupérer les paiements par mois/année
    getByMoisAnnee(mois, annee) {
        return apiClient.get(`/paiements?mois=${mois}&annee=${annee}`)
    },

    // Récupérer mes bulletins (employé connecté)
    getMesBulletins() {
        return apiClient.get('/paiements/mes-bulletins')
    },

    // Générer un paiement (Admin)
    generate(paiement) {
        return apiClient.post('/paiements', paiement)
    },

    // Mettre à jour un paiement (Admin)
    update(id, paiement) {
        return apiClient.put(`/paiements/${id}`, paiement)
    },

    // Marquer un paiement comme effectué (Admin)
    marquerEffectue(id) {
        return apiClient.patch(`/paiements/${id}/effectuer`)
    },

    // Supprimer un paiement (Admin)
    delete(id) {
        return apiClient.delete(`/paiements/${id}`)
    },

    // Télécharger le bulletin PDF
    telechargerBulletin(id) {
        return apiClient.get(`/paiements/${id}/bulletin`, {
            responseType: 'blob'
        })
    }
}