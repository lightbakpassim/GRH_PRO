export type StatutCongeApi = 'Approuvée' | 'En_attente' | 'Refusée' | string
export type StatutPointageApi = 'Approuvées' | 'En_attente' | 'Refusées' | string

export interface EmployeDto {
  idEmploye: number
  nomEmploye: string
  prenomEmploye: string
  emailEmploye: string
  telephone?: string | null
  poste: string
  nomDepartement?: string
  idDepartement: number
  salaireBase: number
  statutEmploye: string
  dateEmbauche?: string
  sexe?: string
  etatCivil?: string
}

export interface CongeDto {
  idConge: number
  idEmploye: number
  nomCompletEmploye: string
  dateDebut: string
  dateFin: string
  nbJours?: number
  motifConge: string
  statutConge: StatutCongeApi
  dateDemande?: string
}

export interface PointageDto {
  idSuivi: number
  idEmploye: number
  nomCompletEmploye: string
  dateTravail: string
  heuresDebut?: string
  heuresFin?: string
  heuresTravaillees?: number
  heuresSupplementaires?: number
  statutSupp: StatutPointageApi
}

export interface PaiementDto {
  idPaiement: number
  idEmploye: number
  nomCompletEmploye: string
  mois: number
  annee: number
  salaireBase?: number
  heuresSuppMontant?: number
  retenues?: number
  totalNet?: number
  statut: string
  datePaiement?: string
}

export interface PointageDraft {
  date: string
  entree: string
}

export function labelStatutConge(statut: string | undefined): string {
  const map: Record<string, string> = {
    Approuvée: 'Approuvée',
    En_attente: 'En attente',
    Refusée: 'Refusée',
    'En attente': 'En attente',
    Validé: 'Approuvée',
    Refusé: 'Refusée'
  }
  return (statut && map[statut]) || statut || '—'
}

export function labelStatutPointage(statut: string | undefined): string {
  const map: Record<string, string> = {
    Approuvées: 'Validé',
    En_attente: 'En attente',
    Refusées: 'Refusé',
    'En attente': 'En attente',
    Validé: 'Validé',
    Refusé: 'Refusé'
  }
  return (statut && map[statut]) || statut || '—'
}

export function formatTime(value?: string | null): string {
  if (!value) return '--:--'
  return String(value).slice(0, 5)
}

export function formatMoisAnnee(mois?: number, annee?: number): string {
  const noms = [
    'janvier', 'février', 'mars', 'avril', 'mai', 'juin',
    'juillet', 'août', 'septembre', 'octobre', 'novembre', 'décembre'
  ]
  return `${noms[(mois || 1) - 1]} ${annee}`
}

export function mapEmploye(e: EmployeDto) {
  return {
    id: e.idEmploye,
    idEmploye: e.idEmploye,
    nom: e.nomEmploye,
    prenom: e.prenomEmploye,
    email: e.emailEmploye,
    telephone: e.telephone,
    poste: e.poste,
    departement: e.nomDepartement,
    idDepartement: e.idDepartement,
    salaireBase: e.salaireBase,
    statut: e.statutEmploye,
    dateEmbauche: e.dateEmbauche,
    sexe: e.sexe,
    etatCivil: e.etatCivil
  }
}

export function mapConge(c: CongeDto) {
  return {
    id: c.idConge,
    idConge: c.idConge,
    employeId: c.idEmploye,
    employeNom: c.nomCompletEmploye,
    dateDebut: c.dateDebut,
    dateFin: c.dateFin,
    nbJours: c.nbJours ?? 0,
    motif: c.motifConge,
    statut: labelStatutConge(c.statutConge),
    statutRaw: c.statutConge,
    dateDemande: c.dateDemande
  }
}

export function mapPointage(p: PointageDto) {
  return {
    id: p.idSuivi,
    idSuivi: p.idSuivi,
    employeId: p.idEmploye,
    employeNom: p.nomCompletEmploye,
    date: p.dateTravail,
    entree: formatTime(p.heuresDebut),
    sortie: formatTime(p.heuresFin),
    heuresTrav: Number(p.heuresTravaillees || 0),
    heuresSupp: Number(p.heuresSupplementaires || 0),
    statut: labelStatutPointage(p.statutSupp),
    statutRaw: p.statutSupp
  }
}

export function mapPaiement(p: PaiementDto) {
  return {
    id: p.idPaiement,
    idPaiement: p.idPaiement,
    employeId: p.idEmploye,
    employeNom: p.nomCompletEmploye,
    mois: p.mois,
    annee: p.annee,
    moisLabel: formatMoisAnnee(p.mois, p.annee),
    salaireBase: Number(p.salaireBase || 0),
    heuresSupp: Number(p.heuresSuppMontant || 0),
    retenues: Number(p.retenues || 0),
    totalNet: Number(p.totalNet || 0),
    statut: p.statut,
    datePaiement: p.datePaiement
  }
}

const DRAFT_KEY = 'grh_pointage_draft'

export function getPointageDraft(): PointageDraft | null {
  try {
    const raw = sessionStorage.getItem(DRAFT_KEY)
    return raw ? (JSON.parse(raw) as PointageDraft) : null
  } catch {
    return null
  }
}

export function setPointageDraft(draft: PointageDraft): void {
  sessionStorage.setItem(DRAFT_KEY, JSON.stringify(draft))
}

export function clearPointageDraft(): void {
  sessionStorage.removeItem(DRAFT_KEY)
}
