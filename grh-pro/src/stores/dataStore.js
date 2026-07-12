import { defineStore } from 'pinia'
import { ref } from 'vue'
import dayjs from 'dayjs'

// Données mockées
const MOCK_EMPLOYES = [
    { id: 1, nom: 'B.', prenom: 'Light', poste: 'Développeur', departement: 'IT', email: 'light@grh.tg', telephone: '+228 90 00 00 01', statut: 'Actif', dateEmbauche: '2024-01-15', salaireBase: 450000 },
    { id: 2, nom: 'K.', prenom: 'Modeste', poste: 'RH', departement: 'RH', email: 'modeste@grh.tg', telephone: '+228 90 00 00 02', statut: 'Actif', dateEmbauche: '2023-06-10', salaireBase: 420000 },
    { id: 3, nom: 'A.', prenom: 'Clemence', poste: 'Comptable', departement: 'FINANCE', email: 'clemence@grh.tg', telephone: '+228 90 00 00 03', statut: 'Actif', dateEmbauche: '2022-11-20', salaireBase: 460000 },
    { id: 4, nom: 'T.', prenom: 'Rayane', poste: 'Manager', departement: 'ADMIN', email: 'rayane@grh.tg', telephone: '+228 90 00 00 04', statut: 'Actif', dateEmbauche: '2024-03-01', salaireBase: 520000 }
]

const MOCK_POINTAGES = [
    { id: 1, employeId: 2, employeNom: 'Modeste K.', date: '2026-06-05', entree: '07:55', sortie: '17:00', heuresTrav: 8.08, heuresSupp: 0.08, statut: 'Validé' },
    { id: 2, employeId: 1, employeNom: 'Light B.', date: '2026-06-05', entree: '08:02', sortie: '19:00', heuresTrav: 10.97, heuresSupp: 2.97, statut: 'En attente' },
    { id: 3, employeId: 1, employeNom: 'Light B.', date: '2026-06-06', entree: '08:02', sortie: '19:00', heuresTrav: 10.97, heuresSupp: 2.97, statut: 'En attente' },
    { id: 4, employeId: 1, employeNom: 'Light B.', date: '2026-06-07', entree: '08:02', sortie: '19:00', heuresTrav: 10.97, heuresSupp: 2.97, statut: 'En attente' },
    { id: 5, employeId: 1, employeNom: 'Light B.', date: '2026-06-08', entree: '07:55', sortie: '17:00', heuresTrav: 8.08, heuresSupp: 0.08, statut: 'Validé' }
]

const MOCK_CONGES = [
    { id: 1, employeId: 2, employeNom: 'Modeste K.', dateDebut: '2026-06-10', dateFin: '2026-06-14', nbJours: 5, motif: 'Vacances', statut: 'Validé' },
    { id: 2, employeId: 1, employeNom: 'Light B.', dateDebut: '2026-06-15', dateFin: '2026-06-16', nbJours: 2, motif: 'Formation', statut: 'En attente' },
    { id: 3, employeId: 3, employeNom: 'Clemence A.', dateDebut: '2026-06-20', dateFin: '2026-06-25', nbJours: 6, motif: 'Vacances', statut: 'En attente' }
]

const MOCK_PAIEMENTS = [
    { id: 1, employeId: 1, employeNom: 'Light B.', mois: 'juin 2026', salaireBase: 150000, heuresSupp: 15336, totalNet: 165336, statut: 'Effectué' },
    { id: 2, employeId: 3, employeNom: 'Clemence A.', mois: 'juin 2026', salaireBase: 150000, heuresSupp: 15336, totalNet: 165336, statut: 'Effectué' },
    { id: 3, employeId: 4, employeNom: 'Rayane T.', mois: 'juin 2026', salaireBase: 150000, heuresSupp: 15336, totalNet: 165336, statut: 'Effectué' }
]

export const useDataStore = defineStore('data', () => {
    const employes = ref([...MOCK_EMPLOYES])
    const pointages = ref([...MOCK_POINTAGES])
    const conges = ref([...MOCK_CONGES])
    const paiements = ref([...MOCK_PAIEMENTS])

    // Actions pour les employés
    const ajouterEmploye = (employe) => {
        const newId = Math.max(...employes.value.map(e => e.id), 0) + 1
        employes.value.push({ ...employe, id: newId, statut: 'Actif' })
    }

    const modifierEmploye = (id, data) => {
        const index = employes.value.findIndex(e => e.id === id)
        if (index !== -1) {
            employes.value[index] = { ...employes.value[index], ...data }
        }
    }

    const supprimerEmploye = (id) => {
        const index = employes.value.findIndex(e => e.id === id)
        if (index !== -1) {
            employes.value[index].statut = 'Inactif'
        }
    }

    // Actions pour les pointages
    const ajouterPointage = (pointage) => {
        const newId = Math.max(...pointages.value.map(p => p.id), 0) + 1
        pointages.value.unshift({ ...pointage, id: newId })
    }

    const validerPointage = (id) => {
        const index = pointages.value.findIndex(p => p.id === id)
        if (index !== -1) {
            pointages.value[index].statut = 'Validé'
        }
    }

    const refuserPointage = (id) => {
        const index = pointages.value.findIndex(p => p.id === id)
        if (index !== -1) {
            pointages.value[index].statut = 'Refusé'
        }
    }

    // Actions pour les congés
    const ajouterConge = (conge) => {
        const newId = Math.max(...conges.value.map(c => c.id), 0) + 1
        conges.value.unshift({ ...conge, id: newId, statut: 'En attente' })
    }

    const validerConge = (id) => {
        const index = conges.value.findIndex(c => c.id === id)
        if (index !== -1) {
            conges.value[index].statut = 'Validé'
        }
    }

    const refuserConge = (id) => {
        const index = conges.value.findIndex(c => c.id === id)
        if (index !== -1) {
            conges.value[index].statut = 'Refusé'
        }
    }

    // Statistiques dashboard Admin
    const getAdminStats = () => {
        return {
            totalEmployes: employes.value.filter(e => e.statut === 'Actif').length,
            heuresSupp: pointages.value.filter(p => p.statut === 'En attente').reduce((sum, p) => sum + p.heuresSupp, 0).toFixed(1),
            congesAttente: conges.value.filter(c => c.statut === 'En attente').length,
            paiementMois: paiements.value.filter(p => p.statut === 'Effectué').length
        }
    }

    // Statistiques dashboard Employé
    const getEmployeStats = (employeId) => {
        const pointagesEmploye = pointages.value.filter(p => p.employeId === employeId)
        const heuresTotal = pointagesEmploye.reduce((sum, p) => sum + p.heuresTrav, 0)
        const heuresSupp = pointagesEmploye.reduce((sum, p) => sum + p.heuresSupp, 0)

        return {
            heuresCeMois: Math.round(heuresTotal),
            heuresSupp: Math.round(heuresSupp),
            congesRestants: 14,
            paiementStatus: paiements.value.some(p => p.employeId === employeId && p.statut === 'Effectué')
        }
    }

    return {
        employes,
        pointages,
        conges,
        paiements,
        ajouterEmploye,
        modifierEmploye,
        supprimerEmploye,
        ajouterPointage,
        validerPointage,
        refuserPointage,
        ajouterConge,
        validerConge,
        refuserConge,
        getAdminStats,
        getEmployeStats
    }
})