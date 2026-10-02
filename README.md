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

L'ancienne landing Next.js (React) a été convertie à l'identique en Angular : mêmes sections, mêmes
classes Tailwind, mêmes textes FR / EN, mêmes images, thème clair / sombre. Les animations
`framer-motion` sont remplacées par la directive `appReveal` (IntersectionObserver).

```
frontend/src/app/
├── components/
│   ├── vitrine/          site public — /
│   │   ├── layout/       navbar, footer
│   │   ├── sections/     hero, how-it-works, features, about, pricing, faq
│   │   ├── pages/        landing, catalogue /auto-ecoles, /connexion, /inscription,
│   │   │                 /verify-email, /reset-password, /mot-de-passe-oublie
│   │   └── i18n/         fr.json, en.json, I18nService
│   ├── dashboard/        espace moniteur / élève — /dashboard
│   │   ├── layout/       barre latérale (menu selon le rôle)
│   │   └── pages/        bienvenue (création / adhésion), accueil, élèves, demandes,
│   │                     véhicules, réservations, cours, examens, paiements
│   └── back-office/      administrateurs de la plateforme — /backoffice
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

Crée un fichier `.env` à la racine du projet :

```properties
# Base de données (valeurs par défaut : localhost:5432, postgres / root)
DBNAME=drivehubDB
# DB_URL=jdbc:postgresql://localhost:5432/drivehubDB
# DB_USERNAME=postgres
# DB_PASSWORD=root

# Origines du front autorisées (CORS)
# CORS_ALLOWED_ORIGINS=http://localhost:4200

# Compte ROOT créé au premier démarrage
MOCK_ROOT_USERNAME=root@drivehub.cm
MOCK_ROOT_PASSWORD=change-moi

# JWT
JWT_SECRET_KEY=ta_cle_secrete_minimum_32_caracteres   # vérifiée au démarrage
# JWT_EXPIRATION_MINUTES=1440

# Email (Gmail + App Password)
MAIL_USERNAME=ton.email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx

# Frontend (liens envoyés par email)
APP_FRONTEND_URL=http://localhost:4200

# Paiement : SIMULATED (défaut) ou CAMPAY
PAYMENT_PROVIDER=SIMULATED
# CAMPAY_BASE_URL=https://demo.campay.net/api
# CAMPAY_USERNAME=...            (identifiants de l'application Campay)
# CAMPAY_PASSWORD=...
# CAMPAY_WEBHOOK_KEY=...         (clé de signature du webhook)
```

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
mvn spring-boot:run
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
│   └── V9__reprise_donnees_tenants_existants.sql   ← reprise des données de l'ancien code
└── tenant/                                  ← appliquées à CHAQUE schéma d'auto-école
    ├── V2__init_tenant_schema_template.sql
    ├── V3__corrections_champs_metier.sql
    └── V4__paiement_mobile_money.sql
```

Les migrations `tenant/` sont exécutées par Flyway à la création d'une auto-école **et** au démarrage
pour toutes les auto-écoles existantes (`TenantMigrationRunner`). Pour faire évoluer les tables métier :
créer `tenant/V5__....sql` — ne jamais modifier une migration déjà appliquée.

## Tester

```bash
cd backend && mvn test                                  # 115 tests unitaires et d'intégration
./scripts/e2e-multitenant.sh http://localhost:8082 \
   "postgresql://postgres:root@localhost:5432/drivehubDB"  # parcours complet sur l'API lancée (47 vérifications)
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
- Mots de passe hashés avec BCrypt

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
