# 🚗 DrivingMaster – SaaS de gestion d’auto‑écoles (Multi‑tenant)

## 📌 Présentation générale

**DrivingMaster** est une plateforme **SaaS multi‑tenant** de gestion d’auto‑écoles, conçue pour répondre à des exigences **réelles, professionnelles et scalables**.

Le projet adopte une architecture **Shared Database / Separate Schemas** afin de garantir :

* 🔐 Isolation stricte des données par auto‑école (tenant)
* 🚀 Scalabilité horizontale
* 🧠 Simplicité métier côté requêtes
* 🏢 Mutualisation de l’infrastructure

---

## 🎯 Objectifs du projet

* Digitaliser la gestion complète d’une auto‑école
* Fournir une plateforme SaaS extensible et sécurisée
* Mettre en œuvre une **architecture multi‑tenant avancée** avec Spring Boot
* Séparer clairement les responsabilités **métier / sécurité / infrastructure**

---

## 🧱 Architecture globale

### 🏗️ Backend

* **Java 21**
* **Spring Boot 3.5.x**
* **Spring Security (JWT)**
* **Hibernate 6 – Multi‑Tenancy (SCHEMA)**
* **PostgreSQL**

### 🎨 Frontend (prévu)

* **Landing Page** : React
* **Dashboard Métier** : Angular
* **Back‑Office Admin** : Angular (séparé)

---

## 🧠 Stratégie Multi‑Tenant

### ✔️ Choix retenu : Shared Database / Separate Schema

* Une **seule base PostgreSQL**
* Un **schéma par auto‑école**
* Un schéma `public` pour :

  * utilisateurs techniques
  * registry des tenants
  * authentification

### 🔁 Résolution du tenant

Le tenant est résolu **à chaque requête** via :

* l’en‑tête HTTP `X-Tenant-ID`

Un `TenantResolutionFilter` positionne dynamiquement :

```java
SET search_path TO <tenant_schema>
```

👉 Résultat :

* **AUCUN `tenant_id` dans les entités**
* **AUCUNE clause WHERE tenant_id = ?**
* Le moteur SQL travaille directement dans le bon schéma

---

## 👥 Modèle des acteurs

### 👤 Utilisateurs Métier (par auto‑école)

* **STUDENT** : élève
* **MONITOR** : moniteur / responsable métier de l’auto‑école

### 🏢 Administrateurs Plateforme (Back‑Office)

* **ROOT** : contrôle total de la plateforme
* **SUPER_ADMIN** : supervision des auto‑écoles
* **REVIEWER** : validation des demandes (moniteurs)

⚠️ Les **PlatformAdmin** sont **totalement séparés** des utilisateurs métier.

---

## 🔐 Sécurité & Authentification

### 🔑 Authentification par JWT

* JWT **stateless**
* Claims contrôlés
* Séparation stricte :

  * Auth Users (tenant‑aware)
  * Auth Platform Admin (public)

### 🧩 Principes de sécurité

* Aucun accès tenant sans `X-Tenant-ID`
* Aucune donnée partagée entre auto‑écoles
* Aucune logique métier dans les filtres

---

## 🔄 Workflow principal (simplifié)

### 1️⃣ Inscription utilisateur

* Création d’un compte technique (public)
* Statut initial : `PENDING` ou `REGISTERED`

### 2️⃣ Création d’une auto‑école

* Réservée au **MONITOR** validé
* Génération automatique du schéma PostgreSQL

### 3️⃣ Validation

* Les **PlatformAdmin** valident :

  * moniteurs
  * auto‑écoles

### 4️⃣ Accès métier

* Accès au dashboard tenant
* Données isolées par schéma

---

## 🧪 État actuel du projet

### ✔️ Implémenté

* Multi‑tenancy par schéma
* Authentification JWT
* Registry des tenants
* Création automatique des schémas

### 🚧 En cours

* Stabilisation de la sécurité admin
* Séparation complète des chaînes de filtres
* Finalisation des workflows de validation

---

## 📁 Organisation du projet (simplifiée)

```
backend/
 ├── core/
 │   ├── tenant/
 │   ├── security/
 │   ├── auth/
 │   └── common/
 ├── modules/
 │   ├── driving-school/
 │   ├── student/
 │   └── monitor/
 └── platform/
     └── admin/
```

---

## 🧠 Philosophie du projet

Ce projet est conçu comme un **vrai produit SaaS**, pas comme un exercice académique.

Les choix techniques privilégient :

* la maintenabilité
* la lisibilité
* la séparation des responsabilités
* l’évolutivité long terme

---

## 👨‍💻 Auteur

**Kamsu Modjo Victor Y**
Développeur Java / Spring Boot

* 🌐 Agence : [https://mv-tech.vercel.app](https://mv-tech.vercel.app)
* 🌐 Portfolio : [https://modjovictor.vercel.app](https://modjovictor.vercel.app)
* linkedin : [https://www.linkedin.com/in/victor-modjo-5933162a3/](in/victor-modjo)
---

## ⚠️ Note importante

Ce projet est en **développement actif**.
Certaines parties sont volontairement itératives afin de refléter un cycle réel de conception SaaS.

---

> *"Build systems like you expect them to scale."* 🚀
