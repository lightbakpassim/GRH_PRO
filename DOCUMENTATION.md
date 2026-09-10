# Documentation GRH_PRO

Application de **Gestion des Ressources Humaines** : employés, pointages, congés, absences, paiements et notifications.

| Couche | Technologie | Dossier |
|--------|-------------|---------|
| Frontend | Vue 3 + Vite + Pinia + Tailwind + axios | `grh-pro/` |
| Backend | Spring Boot 3.1.5 + JWT + JPA | `BACKEND/` |
| Base | MariaDB (`gestionRH`) | — |

---

## Table des matières

1. [Vue d’ensemble](#1-vue-densemble)
2. [Architecture](#2-architecture)
3. [Prérequis & installation](#3-prérequis--installation)
4. [Lancement](#4-lancement)
5. [Comptes de test](#5-comptes-de-test)
6. [Frontend](#6-frontend)
7. [Backend](#7-backend)
8. [API REST](#8-api-rest)
9. [Modèle de données](#9-modèle-de-données)
10. [Sécurité](#10-sécurité)
11. [Configuration](#11-configuration)
12. [Tests](#12-tests)
13. [Limitations connues](#13-limitations-connues)

---

## 1. Vue d’ensemble

GRH_PRO propose deux espaces :

| Espace | Rôle | Fonctions |
|--------|------|-----------|
| **Admin** | `Admin` | CRUD employés / départements / utilisateurs, validation pointages & congés, génération / suivi des paiements |
| **Employé** | `Employe` | Pointage (entrée/sortie), demandes de congés, consultation des bulletins |

Authentification par **JWT** (`Authorization: Bearer …`). Le frontend consomme l’API via axios (`VITE_API_URL`).

---

## 2. Architecture

```
┌─────────────────────┐         REST / JWT          ┌──────────────────────┐
│  grh-pro (Vue 3)    │ ──────────────────────────► │  BACKEND (Spring)    │
│  Port 3000 (Vite)   │                             │  Port 8080           │
│  Pinia auth store   │ ◄────────────────────────── │  Controllers /api/*  │
└─────────────────────┘                             └──────────┬───────────┘
                                                               │ JPA
                                                               ▼
                                                     ┌──────────────────────┐
                                                     │  MariaDB gestionRH   │
                                                     └──────────────────────┘
```

**Flux login**

1. `LoginPage` → `POST /api/auth/login` `{ login, motDePasse }`
2. Réponse : `{ token, login, role, idEmploye, nomComplet }`
3. Store Pinia + `localStorage` (`auth_token`, `user`)
4. Interceptor axios ajoute le Bearer ; le router redirige selon le rôle

---

## 3. Prérequis & installation

### Prérequis

- **JDK 17+** (le projet cible Java 17 ; compatible JDK 25 avec Lombok 1.18.46)
- **Maven 3.8+**
- **Node.js** `^20.19` ou `>=22.12`
- **MariaDB** avec la base `gestionRH` et le schéma (tables + triggers éventuels)
- `ddl-auto=none` : le schéma **n’est pas** créé automatiquement par Hibernate

### Installation

```bash
# Backend
cd BACKEND
# Copier BACKEND/.env.example et exporter les variables (surtout DB_PASSWORD)

# Frontend
cd ../grh-pro
npm install
```

---

## 4. Lancement

### Backend

```bash
cd BACKEND
export DB_USERNAME=root
export DB_PASSWORD='votre_mot_de_passe'
export SEED_TEST_USERS=true
export CORS_ORIGINS='http://localhost:3000,http://127.0.0.1:3000,http://localhost:5173,http://127.0.0.1:5173'
mvn spring-boot:run
```

- API : http://localhost:8080  
- Swagger UI : http://localhost:8080/swagger-ui.html  
- OpenAPI JSON : http://localhost:8080/v3/api-docs  

### Frontend

```bash
cd grh-pro
npm run dev
```

- App : http://localhost:3000 (port défini dans `vite.config.js`)

> **CORS** : l’origine du navigateur (`localhost` vs `127.0.0.1`) doit figurer dans `CORS_ORIGINS`. Sinon le login échoue avec « Invalid CORS request ».

### Build production

```bash
# Front
cd grh-pro && npm run build && npm run typecheck

# Back
cd BACKEND && mvn -DskipTests package
java -jar target/Gestion_RH-0.0.1-SNAPSHOT.jar
```

---

## 5. Comptes de test

Si `SEED_TEST_USERS=true` (défaut), le `DataSeeder` crée / réinitialise au démarrage :

| Rôle | Login | Mot de passe |
|------|--------|--------------|
| SuperAdmin (plateforme) | `plateforme@grh.tg` | `plateforme123` |
| Admin | `admin@grh.tg` | `admin123` |
| DG | `dg@grh.tg` | `dgChangeMe1` |

Les comptes employés se créent via l’interface Admin. L’espace `/plateforme` permet d’onboarder une entreprise (compte DG) et de suspendre un tenant.

Le seeder / `SchemaInitializer` normalise aussi la colonne `role` (VARCHAR, `Employe` sans accent).

En **production** : `SEED_TEST_USERS=false`. Mail : `MAIL_ENABLED=true` + SMTP.

---

## 6. Frontend

### Stack

| Lib | Version (approx.) |
|-----|-------------------|
| Vue | 3.5 |
| Vite | 8 |
| Vue Router | 5 |
| Pinia | 3 |
| axios | 1.17 |
| Tailwind CSS | 3.4 |
| TypeScript | 5.7 (couche `API/` + `utils/`) |
| dayjs, Heroicons | — |

### Structure

```
grh-pro/src/
├── main.js, App.vue, env.d.ts
├── router/index.js
├── stores/data.js          # useAuthStore
├── API/                    # clients.ts, auth, employes, conges, paiements, pointages, departements
├── utils/api.ts, mappers.ts
├── composable/useToast.js
├── components/             # AdminSidebar, EmployeSidebar, DataTable, StatCard, StatusBadge, ToastContainer
└── pages/
    ├── LoginPage.vue
    ├── admin/              # Layout + Dashboard, Employes, Pointages, Conges, Paiement
    └── employe/            # Layout + Dashboard, Pointage, Conges, Paiement
```

### Routes

| Path | Accès |
|------|--------|
| `/login` | Public |
| `/admin/*` | Auth + rôle `Admin` |
| `/employe/*` | Auth + rôle `Employe` |

Sous-routes admin : `dashboard`, `employes`, `pointages`, `conges`, `paiement`.  
Sous-routes employé : `dashboard`, `pointage`, `conges`, `paiement`.

### Modules API ↔ pages

| Page | API |
|------|-----|
| Login | `authAPI` |
| AdminDashboard | employes, pointages, conges, paiements |
| AdminEmployes | employes, departements |
| AdminPointages | pointages (`/suivi-temps`) |
| AdminConges | conges |
| AdminPaiement | paiements, employes |
| Employe* | pointages / conges / paiements (`mes-*`) |

### Pointage employé

L’entrée est stockée en `sessionStorage` ; à la sortie, un `POST /api/suivi-temps` envoie `heuresDebut` + `heuresFin`.

### Scripts npm

| Commande | Rôle |
|----------|------|
| `npm run dev` | Serveur Vite (port 3000) |
| `npm run build` | Build production |
| `npm run typecheck` | `tsc --noEmit` |
| `npm run preview` | Prévisualiser le build |

---

## 7. Backend

### Stack

| Élément | Détail |
|---------|--------|
| Spring Boot | 3.1.5 |
| Java | 17 |
| Sécurité | spring-security + JJWT 0.12.5 |
| Persistence | Spring Data JPA + MariaDB |
| Docs | springdoc-openapi 2.3.0 |
| Lombok | 1.18.46 (+ `proc=full`) |

### Packages (`com.example.gestion_rh`)

| Package | Rôle |
|---------|------|
| `config` | SecurityConfig, OpenApiConfig, DataSeeder |
| `controller` | 9 contrôleurs REST |
| `dto/request`, `dto/response` | Contrats API |
| `model` | 8 entités JPA |
| `repository` | Spring Data |
| `service` | Logique métier |
| `security` | JwtService, filtre JWT, SecurityUtils (anti-IDOR) |
| `exception` | GlobalExceptionHandler, BusinessException, ResourceNotFoundException |

### Pagination

Réponses paginées via `PageResponse<T>` (`content`, `page`, `size`, `totalElements`, `totalPages`).  
Paramètres typiques : `?page=0&size=20`.  
Le front utilise `unwrapList()` (`utils/api.ts`) pour accepter page ou liste.

---

## 8. API REST

Base : `/api` — auth Bearer sauf `/api/auth/**` et Swagger.

### Auth — `/api/auth`

| Méthode | Path | Accès |
|---------|------|--------|
| POST | `/login` | Public |

Body : `{ "login": "...", "motDePasse": "..." }`

### Employés — `/api/employes` (Admin)

`GET /` (page + filtres `idDepartement`, `search`), `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`

### Utilisateurs — `/api/utilisateurs` (Admin)

`GET /`, `GET /{id}`, `POST /`, `PUT /{id}`, `PATCH /{id}/toggle-statut`, `DELETE /{id}`

### Départements — `/api/departements` (Admin)

CRUD standard

### Congés — `/api/conges`

| Path | Accès |
|------|--------|
| `GET /`, `GET /en-attente` | Admin |
| `GET /mes-conges`, `POST /`, `GET /{id}`, `DELETE /{id}` | Admin + Employe |
| `PATCH /{id}/approuver`, `PATCH /{id}/refuser` | Admin |

### Absences — `/api/absences`

Même logique (liste / en-attente / mes-absences / approuver / refuser)

### Suivi du temps — `/api/suivi-temps`

| Path | Accès |
|------|--------|
| `GET /`, `GET /en-attente` | Admin |
| `GET /mon-suivi`, `POST /` | Admin + Employe |
| `PATCH /{id}/approuver\|refuser`, `DELETE /{id}` | Admin |

### Paiements — `/api/paiements`

| Path | Accès |
|------|--------|
| `GET /`, `GET /{id}`, `POST /`, `PUT /{id}`, `PATCH /{id}/effectuer`, `DELETE /{id}` | Admin |
| `GET /mes-bulletins` | Admin + Employe |

### Notifications — `/api/notifications`

| Path | Accès |
|------|--------|
| `GET /mes-notifications`, `/non-lues`, `/count`, `PATCH /{id}/lire`, `PATCH /lire-tout` | Admin + Employe |
| `POST /`, `DELETE /{id}` | Admin |

Documentation interactive : **Swagger UI** avec schéma `bearerAuth`.

---

## 9. Modèle de données

```
Departement 1 ──< Employe
Employe 1 ── 1 Utilisateur
Employe 1 ──< SuiviTemps | DemandeConge | Absence | Paiement | Notification
Utilisateur (valideur) ── SuiviTemps | DemandeConge | Absence
Paiement 1 ──< Notification
```

| Table | Entité | Enums notables |
|-------|--------|----------------|
| `departement` | Departement | — |
| `employe` | Employe | Sexe, EtatCivil, StatutEmploye |
| `utilisateur` | Utilisateur | Role (`Admin`, `Employe`), StatutUtilisateur |
| `suivi_temps` | SuiviTemps | StatutSupp |
| `demande_conge` | DemandeConge | StatutConge |
| `absence` | Absence | StatutAbsence, TypeAbsence |
| `paiement` | Paiement | StatutPaiement |
| `notification` | Notification | TypeNotification |

Des colonnes calculées / triggers MariaDB existent (ex. `nbJours`, `totalNet`, heures) — le schéma SQL est la source de vérité.

---

## 10. Sécurité

- **JWT** : claims `role`, `idEmploye` ; session STATELESS ; CSRF désactivé
- **Rôles** : authority `ROLE_Admin` / `ROLE_Employe` (enum Java **sans accent** : `Employe`)
- **BCrypt** pour les mots de passe
- **Anti-IDOR** (`SecurityUtils`) : un Employé ne peut cibler que son propre `idEmploye` sur les opérations `mes-*` / création
- **CORS** : origines listées, credentials autorisés
- **Swagger** : endpoints docs publics

---

## 11. Configuration

### Backend (`application.properties` / env)

| Variable | Défaut | Description |
|----------|--------|-------------|
| `SERVER_PORT` | `8080` | Port HTTP |
| `DB_URL` | `jdbc:mariadb://localhost:3306/gestionRH?...` | JDBC |
| `DB_USERNAME` | `root` | User DB |
| `DB_PASSWORD` | *(vide)* | Mot de passe DB (**requis** en local typique) |
| `JWT_SECRET` | clé dev | Secret signature JWT |
| `JWT_EXPIRATION` | `86400000` | Durée access token (ms) |
| `CORS_ORIGINS` | localhost + 127.0.0.1 :3000/:5173 | Origines front |
| `SEED_TEST_USERS` | `true` | Seed comptes test |
| `JPA_SHOW_SQL` | `false` | Logs SQL |

Référence : `BACKEND/.env.example`.

### Frontend (`.env`)

```env
VITE_API_URL=http://localhost:8080/api
VITE_APP_ENV=development
```

---

## 12. Tests

```bash
cd BACKEND
mvn test
```

| Classe | Contenu |
|--------|---------|
| `GestionRhApplicationTests` | Chargement contexte (profil `test`, H2) |
| `EmployeControllerSecurityTest` | 401 / 403 / 200 sur `/api/employes` |
| `SecurityUtilsTest` | Résolution / ownership ID employé |

Profil test : `SEED_TEST_USERS=false`, H2 en mémoire.

Front : `npm run typecheck`.

---

## 13. Limitations connues

- Pas d’API de **solde de congés** réel (estimation côté UI éventuelle)
- Pas de génération **PDF** de bulletin
- Refresh token configuré mais **non exposé** dans `JwtService`
- Notifications peu (ou pas) branchées dans les pages Vue
- Absences : API présente, UI dédiée limitée
- TypeScript progressif (pages encore majoritairement `.vue` / JS)
- Schéma MariaDB à maintenir manuellement (`ddl-auto=none`)

---

## 14. Workflow RH / DG (évolution)

### Création d’employé (GRH / Admin)
1. L’admin crée l’employé avec **email professionnel** et **département obligatoire**.
2. Un compte `Utilisateur` (rôle `Employe`) est créé automatiquement.
3. Un **mot de passe unique** (12 caractères) est généré.
4. Un **email SMTP réel** envoie login + mot de passe à l’employé.
5. L’action est tracée dans `historique_action`.

### Compte DG
- Login seed : `dg@grh.tg` / `dgChangeMe1` (à changer).
- Rôle `DG` — espace `/dg` (rapports + historique).
- Changement de mot de passe : `PATCH /api/auth/change-password` (tous les rôles).

### Rapport hebdomadaire
- Planifié **chaque vendredi à 22:00 GMT**.
- PDF (pointages entrée/sortie par employé) stocké en base (`rapport.fichier_pdf`).
- Livré **in-app** au DG (pas par email) — consultation / téléchargement dans `/dg/rapports`.
- Déclenchement manuel (**Admin uniquement**) : `POST /api/rapports/generer-hebdo`.
- Dashboard DG : `GET /api/dashboard/entreprise` (rafraîchi côté UI toutes les 20 s).

### SMTP (variables)
Voir `BACKEND/.env.example` : `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `MAIL_ENABLED`.


### Arborescence dépôt

```
GRH_PRO/
├── DOCUMENTATION.md          ← ce fichier
├── README.md
├── BACKEND/                  Spring Boot
│   ├── .env.example
│   ├── pom.xml
│   └── src/main/java/com/example/gestion_rh/
└── grh-pro/                  Vue 3
    ├── .env
    ├── package.json
    └── src/
```

### Contacts utiles

- Swagger : `/swagger-ui.html`
- Login test admin : `admin@grh.tg` / `admin123`
- Login test DG : `dg@grh.tg` / `dgChangeMe1`
