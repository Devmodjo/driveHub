# DriveHub — SaaS de Gestion d'Auto-Écoles
  
> Plateforme SaaS multi-tenant de gestion complète d'auto-écoles, construite avec Java 21, Spring Boot 3.5 et Angular 21.
 
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue?logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-GPL-lightgrey)](LICENSE)

--- 
 
## Présentation
 
**DriveHub** est une plateforme SaaS B2B permettant aux auto-écoles de gérer l'ensemble de leurs opérations : élèves, moniteurs, véhicules, cours, examens, réservations et paiements.

L'architecture repose sur le modèle **Shared Database / Separate Schema** : une seule base PostgreSQL avec un schéma dédié par auto-école, garantissant une isolation stricte des données tout en mutualisant l'infrastructure.

---

## Démarrer en local (10 minutes)

Prérequis : **Java 21**, **Node.js 22**, **PostgreSQL** (local).

```bash
# 1. Configuration : crée backend/.env et génère les secrets (affiche le mot de passe du compte ROOT)
./scripts/init-env.sh
#    -> vérifiez DB_URL / DB_USERNAME / DB_PASSWORD dans backend/.env, puis créez la base :
psql -U postgres -c 'CREATE DATABASE "drivehubDB";'

# 2. Backend (http://localhost:8082, tables créées automatiquement)
cd backend && ./mvnw spring-boot:run

# 3. Frontend, dans un autre terminal (http://localhost:4200)
cd frontend && npm ci && npm start
```

Sous Windows : lancez `./scripts/init-env.sh` dans Git Bash (ou copiez `backend/.env.example` en `backend/.env`
et remplissez `JWT_SECRET_KEY`, `DATA_ENCRYPTION_KEY`, `MOCK_ROOT_PASSWORD`) et utilisez `mvnw.cmd`.

**Sans serveur d'emails**, les liens des emails (vérification d'adresse, invitation) s'affichent dans la console
du backend : `[DÉVELOPPEMENT] Lien de l'email ...`. Copiez-les dans le navigateur.

**Parcours de test** (tout fonctionne de bout en bout) :

1. `/inscription?role=MONITOR` : créez un moniteur, ouvrez le lien de vérification, connectez-vous.
2. Page « Bienvenue » : envoyez une CNI et un CAPEC (photo ou PDF), puis la demande de création d'auto-école.
3. `/backoffice/login` avec le compte ROOT : « Demandes », consultez les justificatifs, approuvez.
4. Moniteur : « Accéder à mon espace » (période d'essai de 15 jours), ajoutez un véhicule, un moniteur...
5. `/inscription` : créez un élève (aucun justificatif), « Trouver une auto-école », demandez à rejoindre.
6. Moniteur : « Demandes d'adhésion », acceptez ; l'élève paie (Mobile Money simulé) et réserve une leçon.

Tests automatiques : `cd backend && ./mvnw test` et `cd frontend && npx ng test --watch=false`.
Test de l'API pas à pas : [docs/GUIDE-TEST-SWAGGER.md](docs/GUIDE-TEST-SWAGGER.md). Mise en ligne :
[docs/DEPLOIEMENT.md](docs/DEPLOIEMENT.md).

## Architecture Multi-Tenant

Une seule base PostgreSQL, deux niveaux de données :

- **Schéma `public` : les informations globales du système.** Comptes (`_users`), administrateurs de la
  plateforme, **registre des auto-écoles** (`driving_school_registry`), demandes d'adhésion, liste des
  tenants valides (`tenants`). C'est ici qu'une auto-école est déclarée, vérifiée et validée.
- **Un schéma par auto-école (le tenant).** Une fois la demande validée, les données de l'auto-école
  sont réparties dans *son* schéma : fiche de l'établissement, moniteurs, élèves, véhicules, cours,
  examens, réservations, paiements. Deux auto-écoles ne partagent aucune table métier.

```mermaid
flowchart LR
    subgraph DB["Base PostgreSQL drivehubDB"]
        direction LR
        subgraph PUB["schéma public — données globales"]
            U["_users"]
            PA["platform_admin"]
            REG["driving_school_registry"]
            JR["school_join_request"]
            T["tenants"]
        end
        subgraph A["schéma ae_le_volant_3f2a1c"]
            A1["driving_school · monitors · students"]
            A2["vehicles · courses · exams"]
            A3["reservations · payments"]
        end
        subgraph B["schéma ae_horizon_9b7d02"]
            B1["mêmes tables, autres données"]
        end
    end
    REG -- "validation : création du schéma" --> A
    REG -- "validation : création du schéma" --> B
    T -. "liste des schémas autorisés" .-> A
    T -.-> B
```

**Résolution du tenant à chaque requête :**

```mermaid
flowchart LR
    R["Requête HTTP<br/>Authorization + X-Tenant-ID"] --> F1["TenantResolutionFilter<br/>le tenant existe-t-il dans public.tenants ?"]
    F1 --> F2["JwtAuthenticationFilter<br/>claim tenant du JWT = X-Tenant-ID ?"]
    F2 --> H["Hibernate multi-tenant<br/>SET search_path TO schéma"]
    H --> C["Contrôleur / service<br/>(aucun tenant_id dans le code métier)"]
```

Les routes du schéma public (`/api/auth`, `/api/platform`, `/api/driving-schools`, `/api/join-school`,
`/api/webhooks`) n'ont pas besoin de `X-Tenant-ID`. Pour exécuter du code dans un autre schéma au
milieu d'un traitement (par exemple créer la fiche de l'auto-école au moment de la validation), on
utilise `TenantExecutor`, qui ouvre une nouvelle transaction dans le bon schéma.

---

## Stack Technique

| Couche | Technologie |
|--------|-------------|
| Backend | Java 21, Spring Boot 3.5.x |
| Sécurité | Spring Security, JWT (JJWT 0.11.5) |
| Persistance | Hibernate 6, Spring Data JPA |
| Base de données | PostgreSQL 14+ |
| Migrations | Flyway |
| Email | Spring Mail + Thymeleaf |
| Documentation API | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven 3.x |
| Paiement | Campay (MTN Mobile Money, Orange Money), passerelle simulée en développement |
| Frontend | Angular 21 (une seule application : vitrine, dashboard, back-office), Tailwind CSS 4 |

---

## Acteurs du Système

### Utilisateurs Métier (par auto-école)

| Rôle | Description |
|------|-------------|
| `MONITOR` | Moniteur — responsable de l'auto-école, crée et gère l'établissement |
| `STUDENT` | Élève — s'inscrit à une auto-école après validation |

### Administrateurs Plateforme (Back-Office Global)

| Rôle | Description |
|------|-------------|
| `ROOT` | Contrôle total — active les admins, approuve les auto-écoles |
| `SUPER_ADMIN` | Supervision des auto-écoles actives |
| `REVIEWER` | Valide les demandes de création d'auto-école |

> Les `PlatformAdmin` sont totalement séparés des utilisateurs métier — tables distinctes, chaînes de filtres JWT distinctes.

---

## Workflow Principal

```mermaid
sequenceDiagram
    autonumber
    actor M as Moniteur
    actor E as Élève
    participant F as Frontend Angular
    participant API as API Spring Boot
    participant P as schéma public
    participant T as schéma de l'auto-école
    actor R as Admin plateforme (ROOT / REVIEWER)

    M->>F: /inscription (rôle moniteur)
    F->>API: POST /api/auth/register/monitor
    API->>P: _users (REGISTERED) + email de vérification
    M->>F: lien /verify-email?token=...
    F->>API: GET /api/auth/verify-email
    API->>P: statut EMAIL_VERIFIED
    M->>F: /dashboard/bienvenue : formulaire auto-école
    F->>API: POST /api/driving-schools/request
    API->>P: driving_school_registry (PENDING)
    R->>API: PATCH /api/platform/registries/{id}/approve
    API->>T: CREATE SCHEMA + migrations Flyway tenant
    API->>P: tenants (schéma actif), registre APPROVED
    API->>T: fiche driving_school + moniteur fondateur
    M->>F: « Accéder à mon espace »
    F->>API: POST /api/auth/refresh-token
    API-->>F: nouveau JWT avec claim tenant
    Note over F,API: toutes les requêtes métier portent X-Tenant-ID = tenant

    E->>F: inscription + vérification email (mêmes étapes)
    E->>F: /auto-ecoles : « Demander à m'inscrire »
    F->>API: POST /api/join-school/public
    API->>P: school_join_request (PENDING)
    M->>F: /dashboard/demandes : Approuver
    F->>API: POST /api/join-school/admin/{id}/approve
    API->>T: fiche students créée dans le schéma
    E->>F: « Accéder à mon espace » (refresh-token)
    E->>F: réservations, cours, examens, paiements
```

En résumé :

1. **Inscription** dans `public._users`, puis vérification de l'email.
2. **Demande d'auto-école** (moniteur) dans le registre public : statut `PENDING`.
3. **Validation par la plateforme** : création du schéma, migrations Flyway, enregistrement dans
   `public.tenants`, copie de la fiche de l'auto-école et du moniteur fondateur dans le schéma.
4. **Accès métier** : un nouveau jeton (`/api/auth/refresh-token`, ou une reconnexion) contient le
   claim `tenant` ; le frontend l'envoie dans `X-Tenant-ID`.
5. **Adhésion** d'un élève (ou d'un moniteur salarié) : demande dans le schéma public, validation par
   le moniteur responsable, profil copié dans le schéma de l'auto-école.

Un email part à chaque étape : vérification de l'adresse, auto-école validée ou refusée (au moniteur),
nouvelle demande d'adhésion (au responsable), adhésion acceptée ou refusée (au demandeur).

---

## Endpoints Disponibles

### USER API — `/api/auth`

| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| `POST` | `/api/auth/register/student` | Public | Inscription élève |
| `POST` | `/api/auth/register/monitor` | Public | Inscription moniteur |
| `POST` | `/api/auth/login` | Public | Authentification |
| `GET` | `/api/auth/me` | JWT | Utilisateur connecté |
| `POST` | `/api/auth/refresh-token` | JWT | Nouveau jeton (avec le tenant après une validation) |
| `POST` | `/api/auth/logout` | JWT | Déconnexion : le jeton est refusé ensuite (401) |
| `GET` | `/api/auth/verify-email` | Public | Vérification email |
| `POST` | `/api/auth/resend-verification` | Public | Renvoyer l'email |
| `POST` | `/api/auth/forgot-password` | Public | Mot de passe oublié |
| `POST` | `/api/auth/reset-password` | Public | Réinitialiser le mot de passe |

### PLATFORM ADMIN API — `/api/platform`

| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| `POST` | `/api/platform/admin/register` | Public | Inscription admin |
| `POST` | `/api/platform/admin/login` | Public | Connexion admin |
| `GET` | `/api/platform/admin/pending` | ROOT | Admins en attente |
| `GET` | `/api/platform/admin/{id}/activate` | ROOT | Activer un admin |
| `GET` | `/api/platform/admin/me` | JWT Admin | Profil admin connecté |
| `POST` | `/api/platform/admin/logout` | JWT Admin | Déconnexion admin |
| `POST` | `/api/platform/emails` | ROOT / SUPER_ADMIN | Envoyer un email (toutes les auto-écoles, tous les moniteurs, tous les élèves, membres d'une auto-école, adresses choisies) |
| `GET` | `/api/platform/emails` | ROOT / SUPER_ADMIN | Historique des envois |
| `GET` | `/api/platform/emails/audience-count` | ROOT / SUPER_ADMIN | Nombre de destinataires avant l'envoi |
| `GET` | `/api/platform/emails/recipients?q=` | ROOT / SUPER_ADMIN | Recherche de destinataires |
| `GET` | `/api/platform/registries/pending` | REVIEWER/ROOT | Auto-écoles en attente |
| `PATCH` | `/api/platform/registries/{id}/approve` | REVIEWER/ROOT | Approuver une auto-école |

### DRIVING SCHOOL API — `/api/driving-schools`

| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| `POST` | `/api/driving-schools/request` | JWT Monitor | Créer une demande d'auto-école |
| `GET` | `/api/driving-schools/me` | JWT Monitor | Ma demande et son statut (204 si aucune) |
| `GET` | `/api/driving-schools/public/all` | Public | Catalogue des auto-écoles validées |

### JOIN SCHOOL API — `/api/join-school` (schéma public)

| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| `POST` | `/api/join-school/public` | JWT Student/Monitor (email vérifié) | Demander à rejoindre une auto-école |
| `GET` | `/api/join-school/me` | JWT Student/Monitor | Mes demandes et leur statut |
| `GET` | `/api/join-school/admin/pending` | JWT Monitor responsable | Demandes en attente de son auto-école |
| `POST` | `/api/join-school/admin/{id}/approve` | JWT Monitor responsable | Accepter (copie la fiche dans le schéma tenant) |
| `POST` | `/api/join-school/admin/{id}/reject` | JWT Monitor responsable | Refuser |

### API MÉTIER (tenant) — en-tête `X-Tenant-ID` obligatoire

Toutes ces routes s'exécutent dans le schéma de l'auto-école. Le `X-Tenant-ID` doit être égal au
claim `tenant` du JWT, sinon la requête est refusée (401).

| Ressource | Moniteur | Élève |
|-----------|----------|-------|
| `/api/students` | liste, détail, `PUT /{id}` (CNI, permis), `DELETE /{id}` | `GET /me` |
| `/api/monitors` | liste, `GET /me` | liste (pour réserver) |
| `/api/vehicles` | CRUD (immatriculation unique, état) | lecture |
| `/api/courses` | CRUD | lecture |
| `/api/exams` | CRUD, `POST /{id}/inscriptions`, `GET /{id}/inscriptions`, `PATCH /inscriptions/{id}?status=` | lecture, `GET /inscriptions/me` |
| `/api/reservations` | créer (CONFIRMED), lister tout, `PATCH /{id}/confirm`, `PATCH /{id}/cancel` | créer (PENDING), lister les siennes, annuler |
| `/api/payments` | enregistrer à la caisse (VALIDATE), lister tout, `PATCH /{id}/validate`, `/{id}/reject`, `GET /summary` | payer par MOMO/OM via Campay (PENDING), lister les siens, `POST /{id}/refresh` |

Règles métier : pas de double réservation d'un moniteur ou d'un véhicule sur le même créneau (60 min),
véhicule disponible obligatoire pour une leçon de conduite, inscription à un examen de la même catégorie
de permis que l'élève, statut d'un paiement fixé par le serveur (jamais par le client).

---

## Paiement Mobile Money (Campay)

Le paiement passe par une interface `PaymentGateway` (pattern Strategy). Le fournisseur est choisi
dans `application.yaml` (`payment.provider`) :

| Valeur | Usage |
|--------|-------|
| `SIMULATED` (défaut) | Développement : aucun appel externe. Le paiement passe à « Validé » à la première actualisation ; un numéro finissant par `0000` simule un refus. |
| `CAMPAY` | Réel : MTN Mobile Money et Orange Money via l'API Campay (`demo.campay.net` en test, `www.campay.net` en production). |

```mermaid
sequenceDiagram
    autonumber
    actor E as Élève
    participant F as Frontend
    participant API as PaymentService
    participant C as Campay
    participant T as schéma de l'auto-école

    E->>F: /dashboard/paiements : montant + numéro MoMo / OM
    F->>API: POST /api/payments (X-Tenant-ID)
    API->>T: paiement PENDING (référence interne « schéma:id »)
    API->>C: POST /collect/ (montant, numéro, référence)
    C-->>E: demande de confirmation sur le téléphone
    E->>C: saisie du code PIN
    C->>API: webhook /api/webhooks/campay (signature vérifiée)
    API->>T: paiement VALIDATE ou REJECTED (schéma lu dans la référence)
    F->>API: POST /api/payments/{id}/refresh (si le webhook tarde)
    API->>C: GET /transaction/{référence}/
```

Le moniteur, lui, enregistre les paiements reçus à la caisse (statut `VALIDATE` immédiat) et peut
valider ou rejeter à la main un paiement en attente.

---

## Frontend (une seule application Angular)

Le projet est **100 % Angular** : il n'y a plus de React ni de Next.js. L'ancienne landing Next.js a été
convertie à l'identique dans `components/vitrine` :

- mêmes sections, mêmes classes Tailwind, mêmes textes FR / EN, mêmes images, thème clair / sombre ;
- mêmes polices, servies par l'application (`@fontsource/lato`, `@fontsource-variable/plus-jakarta-sans`),
  comme `next/font` : aucun appel à Google Fonts ;
- mêmes icônes : `@lucide/angular`, même version que `lucide-react` (`<svg [lucideIcon]="icons.X">`) ;
- animations `framer-motion` remplacées par la directive `appReveal` (IntersectionObserver).

Contrôle : la page Next.js d'origine et la vitrine Angular ont été comparées pixel par pixel
(1440 px clair et sombre, 390 px mobile, menu mobile ouvert, FAQ ouverte) : mêmes hauteurs de
sections, aucune différence visible.

Styles : Tailwind CSS pour la mise en page (classes dans les templates) et `src/styles.css` pour le
thème (variables de couleurs) et les quelques classes partagées (`.premium-card`, `.field-input`, `.btn-primary`...).
Tailwind ne génère que les classes réellement utilisées, ce qui garde les fichiers CSS légers.

```
frontend/src/app/
├── components/
│   ├── vitrine/          site public — /
│   │   ├── layout/       navbar, footer
│   │   ├── sections/     hero, how-it-works, features, about, pricing, faq
│   │   ├── pages/        landing, catalogue /auto-ecoles, et en plein écran (écran divisé, photo à gauche) :
│   │   │                 /connexion, /inscription, /verify-email, /reset-password, /mot-de-passe-oublie
│   │   └── i18n/         fr.json, en.json, I18nService
│   ├── dashboard/        espace moniteur / élève — /dashboard
│   │   ├── layout/       barre latérale (menu selon le rôle)
│   │   └── pages/        bienvenue (création / adhésion), accueil, élèves, demandes,
│   │                     véhicules, réservations, cours, examens, paiements
│   └── back-office/      propriétaire de la plateforme — /backoffice (mobile d'abord) : vue d'ensemble,
│                         auto-écoles, demandes en attente, administrateurs, envoi d'emails, profil
├── services/             SessionService (moniteur / élève), SchoolApiService, AuthService (admins), ThemeService
├── interceptors/         jeton + X-Tenant-ID ajoutés automatiquement
├── guards/               sessionGuard, tenantGuard, monitorGuard, authGuardGuard (admins)
└── interfaces/           drivehub.models.ts : un type par DTO du backend
```

| Espace | Jeton (localStorage) | En-tête tenant |
|--------|---------------------|----------------|
| Vitrine et dashboard (moniteurs, élèves) | `drivehub_user_token` | `X-Tenant-ID` = claim `tenant` du jeton |
| Back-office (`/api/platform`) | `drivehub_token` | aucun (schéma public) |

```bash
cd frontend
npm install
npm start            # http://localhost:4200 (API attendue sur http://localhost:8082)
npm test             # tests unitaires
```

---

## Mise en ligne et CI/CD

**Guide pas à pas : [docs/DEPLOIEMENT.md](docs/DEPLOIEMENT.md)** — deux comptes : Render (API + base PostgreSQL,
clés générées automatiquement) et Vercel (site) ; emails envoyés depuis Gmail.

| Fichier | Rôle |
|---------|------|
| `.github/workflows/ci.yml` | À chaque push / pull request : tests backend (PostgreSQL réel) + build et tests frontend |
| `.github/workflows/deploy.yml` | Push sur `Develop` : tests, puis redéploiement Render et image `ghcr.io/.../drivehub-api` |
| `backend/Dockerfile` | Image du backend (Java 21, utilisateur sans droits, vérification de santé `/actuator/health`) |
| `docker-compose.yml` | API (+ PostgreSQL avec `--profile db`) sur un serveur VPS |
| `render.yaml` | Render (Blueprint) : crée l'API et sa base PostgreSQL en une fois |
| `backend/.env.example` | Toutes les variables d'environnement, commentées |

## Déployer le frontend sur Vercel

La configuration est dans `frontend/vercel.json`. Dans Vercel → *Settings* :

| Réglage | Valeur |
|---------|--------|
| **Root Directory** | `frontend` (le dépôt contient aussi `backend/`) |
| Framework Preset | Angular |
| Build / Output / Install Command | laisser vides : `vercel.json` les fixe (`npm ci`, `npm run build`, `dist/drivehub/browser`) |
| Environment Variables | `API_URL` = adresse du backend (ex. `https://drivehub-api.onrender.com`) |
| Node.js Version | 22.x |

Pourquoi une erreur 404 apparaît sinon :
- Angular ne génère pas le site dans `dist/` mais dans **`dist/drivehub/browser/`** : avec « Output = dist »,
  Vercel ne trouve pas de `index.html` et répond 404 alors que le déploiement a réussi ;
- les pages Angular (`/connexion`, `/dashboard`...) n'existent pas en fichiers : la règle `rewrites` de
  `vercel.json` renvoie toutes les adresses vers `index.html`, puis Angular affiche la bonne page.

**Adresse du backend :** variable `API_URL` dans Vercel (injectée au build par `frontend/scripts/set-api-url.mjs`),
sinon `frontend/src/environments/environment.ts` ; `environment.development.ts` pour `npm start`. Tant que le backend n'est pas hébergé, la vitrine
fonctionne en ligne mais pas la connexion ni le dashboard. Côté backend, ajouter le domaine du frontend
dans `CORS_ALLOWED_ORIGINS` et le mettre dans `APP_FRONTEND_URL`.

**Images :** les photos d'origine (`frontend/images-src/`, jusqu'à 18 Mo) ne sont pas publiées.
`npm run images` en crée des versions WebP légères dans `public/images/optimized/` (95 Ko au lieu de 18 Mo
pour le haut de la page d'accueil) ; le navigateur choisit la taille adaptée à l'écran.

---

## Installation et Démarrage

### Prérequis

- Java 21+
- PostgreSQL 14+
- Maven 3.8+
- Node.js 20+ (frontend)

### 1. Cloner le projet

```bash
git clone https://github.com/Devmodjo/driveHub.git
cd driveHub/backend
```

### 2. Configurer les variables d'environnement

Toute la configuration est dans **`backend/.env`** (connexion PostgreSQL, secrets, emails, stockage...).
Le modèle commenté est [`backend/.env.example`](backend/.env.example) :

```bash
cp .env.example .env     # dans backend/, puis remplir
```

```properties
DB_URL=jdbc:postgresql://localhost:5432/drivehubDB
DB_USERNAME=postgres
DB_PASSWORD=root
JWT_SECRET_KEY=...           # openssl rand -base64 48
DATA_ENCRYPTION_KEY=...      # openssl rand -base64 32 (chiffre les justificatifs : à conserver)
MOCK_ROOT_USERNAME=root@drivehub.cm
MOCK_ROOT_PASSWORD=...
MAIL_USERNAME=...            # adresse Gmail (mot de passe d'application dans MAIL_PASSWORD)
MAIL_PASSWORD=...
```

`.env` n'est jamais versionné. En production, les mêmes variables sont saisies chez l'hébergeur
(voir [docs/DEPLOIEMENT.md](docs/DEPLOIEMENT.md)) : changer de base = changer `DB_URL`,
`DB_USERNAME` et `DB_PASSWORD`, rien d'autre.

### 3. Créer la base de données PostgreSQL

```sql
CREATE DATABASE "drivehubDB";
```

> **Base existante : inutile de la supprimer.** Au démarrage, Flyway applique les nouvelles
> migrations. `V9__reprise_donnees_tenants_existants.sql` rattrape les auto-écoles validées avec
> l'ancien code : elle les enregistre dans `public.tenants` et recopie la fiche de l'auto-école et
> du moniteur fondateur dans leur schéma. Repartir d'une base vide reste possible (supprimer puis
> recréer `drivehubDB`), mais ce n'est pas nécessaire.

### 4. Lancer l'application

```bash
./mvnw spring-boot:run          # ou : docker compose --profile db up -d --build (API + PostgreSQL)
```

Flyway applique automatiquement les migrations au démarrage.

### 5. Accéder à la documentation API

```
http://localhost:8082/swagger-ui.html
```

---

## Structure du Projet

Chaque module suit le même découpage : `application` (entrée HTTP), `domain` (métier), `infrastructure` (accès aux données).

```
backend/src/main/java/cm/mvtech/drivehub/
├── core/
│   ├── domain/entities/       ← EntityBase (id UUID, dates, suppression logique), TenantEntity
│   ├── domain/service/        ← TenantProvisioningService (Flyway par schéma), TenantMigrationRunner
│   └── infrastructure/        ← TenantContext, TenantIdentifierResolver, SchemaMultiTenantConnectionProvider,
│                                 TenantExecutor (exécuter du code dans un tenant), TenantSchemas (noms sûrs)
├── modules/
│   ├── auth/                  ← Authentification, JWT, Users, UserTenantResolver, CurrentUserProvider
│   ├── drivingschool/         ← Auto-école, Registry, CurrentSchoolProvider
│   ├── monitor/   student/   vehicle/   course/   exam/   reservation/   payment/
│   │   ├── application/controller   ← endpoints REST
│   │   ├── application/dto          ← DTOs (records) validés
│   │   ├── domain/model             ← entités JPA
│   │   ├── domain/services          ← règles métier
│   │   └── infrastructure/mapper|repository ← MapStruct, Spring Data
│   ├── enums/   exception/   messageapi/
├── platform/admin/            ← Back-office PlatformAdmin
└── configs/                   ← CORS, OpenAPI, Hibernate Multitenant
```

---

## Migrations Flyway

```
src/main/resources/db/migration/
├── public/                                  ← appliquées au démarrage sur le schéma public
│   ├── V1__init_migration_public_schema.sql
│   ├── V5 … V7                              ← tokens admin, motif, statuts
│   ├── V8__join_request_references_registry.sql
│   ├── V9__reprise_donnees_tenants_existants.sql   ← reprise des données de l'ancien code
│   ├── V10__revoked_tokens.sql                     ← déconnexion (jetons révoqués)
│   ├── V11__textes_longs.sql                       ← présentation d'auto-école en TEXT
│   ├── V12__consentement_confidentialite.sql       ← preuve du consentement
│   ├── V13__envois_emails_plateforme.sql           ← historique des emails du back-office
│   ├── V14__justificatifs.sql                      ← CNI et CAPEC des moniteurs (chiffrés), journal des consultations
│   ├── V15__abonnements.sql                        ← offres, période d'essai, factures (facturation désactivée)
│   ├── V16__justificatifs_en_base.sql              ← contenu chiffré des justificatifs (stockage DATABASE)
│   └── V17__adresse_publique_auto_ecole.sql        ← adresse lisible de la page publique (slug)
└── tenant/                                  ← appliquées à CHAQUE schéma d'auto-école
    ├── V2__init_tenant_schema_template.sql
    ├── V3__corrections_champs_metier.sql
    ├── V4__paiement_mobile_money.sql
    └── V5__textes_longs.sql                        ← contenu des cours, présentation en TEXT
```

Les migrations `tenant/` sont exécutées par Flyway à la création d'une auto-école **et** au démarrage
pour toutes les auto-écoles existantes (`TenantMigrationRunner`). Pour faire évoluer les tables métier :
créer `tenant/V6__....sql` — ne jamais modifier une migration déjà appliquée.

## Pages publiques et référencement Google

Chaque auto-école validée a une page publique : `/auto-ecoles/{adresse}` (ex. `/auto-ecoles/auto-ecole-le-volant-douala`)
avec sa présentation, ses coordonnées (téléphone, WhatsApp, email, plan) et le bouton « Demander à m'inscrire ».
L'adresse est créée avec la demande d'auto-école (migration V17, `SchoolSlugService`). Pour Google : titre, description,
adresse canonique, aperçu de partage et fiche schema.org `DrivingSchool` (`SeoService`), plan du site `/sitemap.xml`
et `/robots.txt` générés en direct par deux fonctions Vercel (`frontend/api/`). API : `GET /api/driving-schools/public/{slug}`.
Déclaration dans Google Search Console : [docs/DEPLOIEMENT.md](docs/DEPLOIEMENT.md), étape 4.

## Justificatifs des moniteurs (CNI, CAPEC)

Pour écarter les auto-écoles clandestines, chaque **moniteur** fournit sa pièce d'identité et son CAPEC (fondateur :
avant la demande de création, vérifiés par l'équipe DriveHub ; moniteur qui rejoint une auto-école : vérifiés par
son responsable ; moniteur ajouté par le responsable : fournis par lui, avec invitation par email). **Rien n'est
demandé aux élèves.** Fichiers chiffrés (AES-256-GCM, une clé par fichier),
numéros chiffrés en base, chaque consultation journalisée. Les fichiers chiffrés sont stockés dans PostgreSQL par
défaut (`STORAGE_PROVIDER=DATABASE`) ; Cloudflare R2 reste possible (`STORAGE_PROVIDER=R2`). API : [docs/API-JUSTIFICATIFS.md](docs/API-JUSTIFICATIFS.md).
Abonnements (essai de 15 jours, facturation préparée mais désactivée) : [docs/ABONNEMENTS-ET-PAIEMENTS.md](docs/ABONNEMENTS-ET-PAIEMENTS.md).
Plan de mise en œuvre des paiements réels (élèves et abonnements) : [docs/PLAN-PAIEMENTS.md](docs/PLAN-PAIEMENTS.md).

## Emails

Un seul modèle (`templates/emails/layout.html`, logo DriveHub joint), une version texte et une version HTML,
en-têtes soignés. Gmail, adresse professionnelle et spams :
[docs/EMAILS-DELIVRABILITE.md](docs/EMAILS-DELIVRABILITE.md). L'interface rappelle aux utilisateurs de regarder
dans leurs courriers indésirables.

## Données personnelles et vérification des auto-écoles

- **Consentement obligatoire à l'inscription** (`acceptPrivacyPolicy`) : date et version acceptées enregistrées
  sur le compte. Politique publiée sur `/confidentialite` ; texte et points à faire valider par un juriste :
  [docs/POLITIQUE-CONFIDENTIALITE.md](docs/POLITIQUE-CONFIDENTIALITE.md) (loi n° 2024/017 du 23 décembre 2024).
- **Documents qui prouvent qu'une auto-école est en règle au Cameroun** et proposition de vérification :
  [docs/VERIFICATION-AUTO-ECOLES.md](docs/VERIFICATION-AUTO-ECOLES.md).
- Délai de validation annoncé aux utilisateurs : **48 à 72 heures** (`ValidationDelay` côté backend,
  `VALIDATION_DELAY` côté frontend).

## Erreurs renvoyées par l'API

Toutes les erreurs ont la forme `{ status, error, message, path, fieldErrors? }` :
`message` est une phrase affichable telle quelle ; `fieldErrors` (formulaire invalide) donne le message de chaque
champ, affiché sous le champ concerné par le frontend. Les erreurs SQL sont traduites (valeur trop longue,
doublon de nom d'auto-école, d'email ou d'immatriculation...). 400 = donnée invalide, 401 = pas de jeton valide,
403 = rôle insuffisant, 404 = introuvable, 409 = conflit.

## Tester

**Pas à pas depuis Swagger** (créer une auto-école, la valider, faire rejoindre un élève, réserver,
payer, se déconnecter, avec les emails attendus à chaque étape) : [docs/GUIDE-TEST-SWAGGER.md](docs/GUIDE-TEST-SWAGGER.md).

```bash
cd backend && ./mvnw test                               # tests unitaires et d'intégration (PostgreSQL local)
./scripts/e2e-multitenant.sh http://localhost:8082 \
   "postgresql://postgres:root@localhost:5432/drivehubDB"  # parcours complet sur l'API lancée
```

---

## Configuration Email (Gmail)

Pour activer l'envoi d'emails :

1. Activer la **validation en deux étapes** sur ton compte Google
2. Générer un **mot de passe d'application** : `Mon compte → Sécurité → Mots de passe des applications`
3. Renseigner les credentials dans `.env`

---

## Sécurité

- JWT stateless avec claims contrôlés (`role`, `profileStatus`, `tenant`)
- Séparation stricte des chaînes de filtres (Users vs PlatformAdmin)
- Aucune donnée partagée entre schémas tenant
- Tokens email à usage unique avec TTL (24h vérification, 1h reset)
- Déconnexion réelle : le jeton est inscrit (empreinte SHA-256) dans `public.revoked_tokens` et refusé ensuite
- 401 = pas de jeton valide (absent, expiré, révoqué) ; 403 = connecté mais rôle insuffisant
- Mots de passe hashés avec BCrypt
- Justificatifs chiffrés (AES-256-GCM) et secrets uniquement en variables d'environnement
- Image Docker exécutée sans droits d'administration ; Swagger désactivable en production (`SWAGGER_ENABLED=false`)

---

## Roadmap

- [x] Multi-tenancy par schéma PostgreSQL
- [x] Authentification JWT (Users + PlatformAdmin)
- [x] Vérification email + Forgot Password
- [x] Workflow création et approbation auto-école
- [x] Migrations Flyway
- [ ] Gestion des étudiants par vague d'inscription
- [x] Gestion des véhicules, cours, examens
- [x] Système de réservations (conflits de créneaux)
- [x] Paiements : caisse, Mobile Money via Campay (webhook signé), passerelle simulée
- [ ] Tableau de bord analytique
- [x] Frontend Angular unique : vitrine (ex-landing Next.js), dashboard moniteur / élève, back-office
- [ ] Déploiement VPS (Docker + Nginx)

---

## Auteur

**Modjo Victor Y** — Développeur Java / Spring Boot

- Agence : [mv-tech.vercel.app](https://mv-tech.vercel.app)
- Portfolio : [modjovictor.vercel.app](https://modjovictor.vercel.app)
- LinkedIn : [in/victor-modjo](https://www.linkedin.com/in/victor-modjo)

---

> *"Build systems like you expect them to scale."*
