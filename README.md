# DriveHub — SaaS de Gestion d'Auto-Écoles
 
> Plateforme SaaS multi-tenant de gestion complète d'auto-écoles, construite avec Java 21 et Spring Boot 3.5.

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

```
┌─────────────────────────────────────────────────────────┐
│                   Base PostgreSQL                        │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ schema:public│  │schema:auto_  │  │schema:auto_  │  │
│  │              │  │ecole_dupont  │  │ecole_martin  │  │
│  │ _users       │  │              │  │              │  │
│  │ platform_    │  │ driving_     │  │ driving_     │  │
│  │   admin      │  │   school     │  │   school     │  │
│  │ driving_     │  │ monitors     │  │ monitors     │  │
│  │   school_    │  │ students     │  │ students     │  │
│  │   registry   │  │ vehicles     │  │ vehicles     │  │
│  │ tenants      │  │ courses      │  │ courses      │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

**Résolution du tenant :** à chaque requête HTTP, le header `X-Tenant-ID` déclenche un `SET search_path TO <schema>` via Hibernate Multi-Tenancy. Résultat : aucun `tenant_id` dans les entités, aucune clause `WHERE tenant_id = ?`.

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
| Frontend (prévu) | React (landing), Angular (dashboard + back-office) |

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

```
1. INSCRIPTION
   Monitor/Student s'inscrit → compte créé dans public._users
   Email de vérification envoyé automatiquement

2. VÉRIFICATION EMAIL
   Clic sur le lien → statut passe à EMAIL_VERIFIED

3. CRÉATION AUTO-ÉCOLE (Monitor uniquement)
   Monitor soumet une demande → entrée dans driving_school_registry

4. VALIDATION PLATEFORME
   REVIEWER/ROOT approuve → schéma PostgreSQL créé automatiquement
   Tables métier provisionnées dans le nouveau schéma
   Monitor activé (statut ACTIVE)

5. ACCÈS MÉTIER
   Monitor se connecte avec X-Tenant-ID
   Accès aux données isolées de son auto-école
```

---

## Endpoints Disponibles

### USER API — `/api/auth`

| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| `POST` | `/api/auth/register/student` | Public | Inscription élève |
| `POST` | `/api/auth/register/monitor` | Public | Inscription moniteur |
| `POST` | `/api/auth/login` | Public | Authentification |
| `GET` | `/api/auth/me` | JWT | Utilisateur connecté |
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
| `GET` | `/api/platform/registries/{id}/approve` | REVIEWER/ROOT | Approuver une auto-école |

### DRIVING SCHOOL API — `/api/driving-schools`

| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| `POST` | `/api/driving-schools/request` | JWT Monitor | Créer une demande d'auto-école |
| `GET` | `/api/driving-schools/public/all` | Public | Liste des auto-écoles actives |

---

## Installation et Démarrage

### Prérequis

- Java 21+
- PostgreSQL 14+
- Maven 3.8+

### 1. Cloner le projet

```bash
git clone https://github.com/ton-user/drivehub-backend.git
cd drivehub-backend
```

### 2. Configurer les variables d'environnement

Crée un fichier `.env` à la racine du projet :

```properties
# Base de données
DBNAME=drivehubDB

# JWT
JWT_SECRET_KEY=ta_cle_secrete_minimum_256_bits

# Email (Gmail + App Password)
MAIL_USERNAME=ton.email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx

# Frontend
APP_FRONTEND_URL=http://localhost:3000
```

### 3. Créer la base de données PostgreSQL

```sql
CREATE DATABASE "drivehubDB";
```

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

```
backend/
├── core/
│   ├── domain/
│   │   ├── entities/          ← EntityBase (classe de base JPA)
│   │   └── service/           ← TenantProvisioningService
│   └── infrastructure/        ← TenantContext, TenantIdentifierResolver
│                                 SchemaMultiTenantConnectionProvider
├── modules/
│   ├── auth/                  ← Authentification, JWT, Users
│   ├── drivingschool/         ← Auto-école, Registry
│   ├── monitor/               ← Moniteurs
│   ├── student/               ← Élèves
│   ├── vehicle/               ← Véhicules
│   ├── course/                ← Cours
│   ├── exam/                  ← Examens
│   ├── reservation/           ← Réservations
│   ├── payment/               ← Paiements
│   └── enums/                 ← Enums partagés
├── platform/
│   └── admin/                 ← Back-office PlatformAdmin
└── configs/                   ← Flyway, Security, Hibernate Multitenant
```

---

## Migrations Flyway

```
src/main/resources/db/migration/
├── public/
│   ├── V1__init_public_schema.sql      ← Tables globales (users, admin, registry)
│   └── V2__add_auth_tokens.sql         ← Tokens verify-email / reset-password
└── tenant/
    └── V2__init_tenant_schema_template.sql  ← Template tables métier par auto-école
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
- [ ] Gestion des véhicules et cours
- [ ] Système de réservations
- [ ] Paiements (Mobile Money, Cash)
- [ ] Tableau de bord analytique
- [ ] Frontend React / Angular
- [ ] Déploiement VPS (Docker + Nginx)

---

## Auteur

**Modjo Victor Y** — Développeur Java / Spring Boot

- Agence : [mv-tech.vercel.app](https://mv-tech.vercel.app)
- Portfolio : [modjovictor.vercel.app](https://modjovictor.vercel.app)
- LinkedIn : [in/victor-modjo](https://www.linkedin.com/in/victor-modjo)

---

> *"Build systems like you expect them to scale."* 🚀
