# Tester le workflow principal depuis Swagger

Ce guide déroule tout le parcours, dans l'ordre, endpoint par endpoint :
un moniteur crée son auto-école, l'admin de la plateforme la valide, un élève la rejoint,
réserve une leçon et paie. À chaque étape : l'endpoint, le corps JSON à coller, ce qu'il faut
vérifier, et l'email envoyé.

> Le script `scripts/e2e-multitenant.sh` fait exactement le même parcours automatiquement
> (52 vérifications). Ce guide est sa version « à la main ».

---

## 0. Préparer

1. Démarrer PostgreSQL et le backend (`mvn spring-boot:run` dans `backend/`).
2. Ouvrir **http://localhost:8082/swagger-ui.html**.
3. Pour recevoir les emails pendant les tests, deux possibilités :
   - **Vraie boîte mail** : `MAIL_USERNAME` / `MAIL_PASSWORD` (mot de passe d'application Gmail) dans `.env`,
     et utiliser de vraies adresses email dans les inscriptions ci-dessous.
   - **Sans serveur mail** : les emails échouent en silence (l'action réussit quand même, l'erreur est
     dans les logs). Le jeton de vérification se lit alors en base :
     ```sql
     SELECT t.token FROM public.email_verification_tokens t
     JOIN public._users u ON u.id = t.user_id
     WHERE u.email = 'moniteur@test.cm';
     ```

### Comment utiliser le bouton « Authorize »

En haut de Swagger, le bouton **Authorize** a deux champs. Ce que vous y mettez est envoyé avec
toutes les requêtes suivantes :

| Champ | Valeur |
|-------|--------|
| `bearerAuth` | le `token` reçu à la connexion (coller le jeton seul, sans écrire « Bearer ») |
| `tenantId` (en-tête `X-Tenant-ID`) | le schéma de l'auto-école, **uniquement pour les routes métier** (`/api/students`, `/api/vehicles`, `/api/reservations`...). Il est ignoré sur les autres routes, vous pouvez le laisser rempli. |

Pour changer d'utilisateur (moniteur → admin → élève) : **Authorize → Logout → coller le nouveau jeton**.
Gardez les jetons dans un bloc-notes : vous allez alterner entre trois comptes.

**Trouver le tenant** : c'est le claim `tenant` du jeton. Collez le jeton sur https://jwt.io pour le lire,
ou en SQL : `SELECT schema_name FROM public.driving_school_registry;` (exemple : `ae_horizon_3f2a1c`).

---

## Étape 1 — Le moniteur crée son compte

**`POST /api/auth/register/monitor`** (aucune autorisation)

```json
{
  "firstname": "Awa",
  "lastname": "Mbarga",
  "email": "moniteur@test.cm",
  "password": "Password123",
  "phoneNumber": "+237699112233",
  "gender": "FEMALE",
  "nationality": "Camerounaise",
  "residenceCity": "Douala",
  "dateOfBirth": "1990-05-10"
}
```

- Attendu : **201**.
- Email : **« Vérifiez votre adresse email »** au moniteur.

## Étape 2 — Le moniteur vérifie son email

**`GET /api/auth/verify-email?token=...`** : le jeton est dans le lien de l'email (ou en base, voir étape 0).

- Attendu : **200**, « Email vérifié avec succès ».
- Sans cette étape, la demande d'auto-école est refusée.

## Étape 3 — Le moniteur se connecte

**`POST /api/auth/login`**

```json
{ "email": "moniteur@test.cm", "password": "Password123" }
```

- Attendu : **202**, avec un `token` dans la réponse.
- **Authorize → `bearerAuth`** = ce jeton.

## Étape 4 — Le moniteur demande la création de son auto-école

**`POST /api/driving-schools/request`**

```json
{
  "name": "Auto-Ecole Horizon",
  "email": "contact@horizon.cm",
  "country": "Cameroun",
  "city": "Douala",
  "phoneNumber": "+237699445566",
  "address": "Bonapriso, rue 1.234",
  "description": "Permis A et B",
  "websiteUrl": null,
  "whatsappNumber": "+237699445566"
}
```

- Attendu : **201**, « Auto ecole enregistrée en attente de validation ».
- Contrôle : **`GET /api/driving-schools/me`** renvoie la demande avec `"drivingSchoolStatus": "PENDING"`.

## Étape 5 — Vous (propriétaire du SaaS) validez l'auto-école

1. **Authorize → Logout**.
2. **`POST /api/platform/admin/login`** avec le compte ROOT (`MOCK_ROOT_USERNAME` / `MOCK_ROOT_PASSWORD` du `.env`) :
   ```json
   { "email": "root@drivehub.cm", "password": "votre-mot-de-passe-root" }
   ```
3. **Authorize → `bearerAuth`** = jeton admin.
4. **`GET /api/platform/registries/pending`** : copier l'`id` de « Auto-Ecole Horizon ».
5. **`PATCH /api/platform/registries/{registryId}/approve`** : attendu **202**.

Ce qui se passe : création du schéma PostgreSQL de l'auto-école, création de ses tables (Flyway),
enregistrement dans `public.tenants`, puis la fiche de l'auto-école et le moniteur fondateur sont
copiés dans le schéma.

- Email : **« Votre auto-école est validée »** au moniteur.
- Pour refuser à la place : `PATCH /api/platform/registries/{registryId}/reject` (email « Votre demande n'a pas été retenue »).
- Les chiffres de la plateforme : `GET /api/platform/registries/stats` et `GET /api/platform/admin/stats`.

## Étape 6 — Le moniteur récupère un jeton « avec auto-école »

Son ancien jeton ne contient pas encore de tenant.

1. **Authorize → `bearerAuth`** = jeton du moniteur (étape 3).
2. **`POST /api/auth/refresh-token`** : un nouveau `token` est renvoyé (une nouvelle connexion marche aussi).
3. **Authorize** :
   - **`bearerAuth`** = ce nouveau jeton ;
   - **`tenantId`** = son claim `tenant` (jwt.io), par exemple `ae_auto_ecole_horizon_3f2a1c`.

À partir d'ici, les routes métier fonctionnent pour le moniteur.

## Étape 7 — Le moniteur prépare son auto-école

**`POST /api/vehicles`**

```json
{ "matriculation": "LT 452 AB", "model": "Toyota Yaris", "state": "DISPOSABLE" }
```

- Attendu : **201**. Notez l'`id` du véhicule.

**`POST /api/courses`**

```json
{ "title": "Les priorités", "content": "Priorité à droite, sauf panneau contraire." }
```

**`POST /api/exams`**

```json
{ "dateExams": "2030-06-15T08:00:00", "category": "B" }
```

## Étape 8 — L'élève crée son compte et vérifie son email

1. **Authorize → Logout** (vider les deux champs).
2. **`POST /api/auth/register/student`** : même corps qu'à l'étape 1, avec `"email": "eleve@test.cm"`.
   Email : « Vérifiez votre adresse email ».
3. **`GET /api/auth/verify-email?token=...`**.
4. **`POST /api/auth/login`** avec `eleve@test.cm`. **Authorize → `bearerAuth`** = jeton élève.

## Étape 9 — L'élève demande à rejoindre l'auto-école

1. **`GET /api/driving-schools/public/all`** (catalogue public) : copier l'`id` d'« Auto-Ecole Horizon ».
2. **`POST /api/join-school/public`** :

   ```json
   { "drivingSchoolId": "id-copié-du-catalogue", "role": "STUDENT" }
   ```

   - Attendu : **200**.
   - Email : **« Nouvelle demande d'adhésion »** au moniteur.
3. Contrôle : **`GET /api/join-school/me`** renvoie la demande en `PENDING`.

## Étape 10 — Le moniteur voit la demande et l'accepte

1. **Authorize → `bearerAuth`** = jeton du moniteur (étape 6).
2. **`GET /api/join-school/admin/pending`** : copier le `requestId`.
3. **`POST /api/join-school/admin/{id}/approve`** : attendu **200**.

Le profil de l'élève est copié dans le schéma de l'auto-école.

- Email : **« Votre inscription est acceptée »** à l'élève.
- Pour refuser à la place : `POST /api/join-school/admin/{id}/reject` (email « Votre demande n'a pas été acceptée »).

## Étape 11 — L'élève accède à son espace

1. **Authorize → `bearerAuth`** = jeton élève (étape 8).
2. **`POST /api/auth/refresh-token`** : nouveau jeton avec le tenant.
3. **Authorize** : `bearerAuth` = nouveau jeton, `tenantId` = son claim `tenant` (le même que celui du moniteur).
4. **`GET /api/students/me`** : son dossier. **`GET /api/courses`** : les cours publiés.

## Étape 12 — L'élève réserve une leçon de conduite

1. **`GET /api/monitors`** : copier l'`id` du moniteur.
2. **`POST /api/reservations`** :

   ```json
   {
     "monitorId": "id-du-moniteur",
     "vehicleId": "id-du-vehicule",
     "dateTime": "2030-01-10T09:00:00",
     "types": "CONDUITE"
   }
   ```

   - Attendu : **201**, `"reservationStatus": "PENDING"`.
   - Refusé (**409**) si le moniteur, l'élève ou le véhicule est déjà pris dans l'heure.

## Étape 13 — Le moniteur voit et confirme la réservation

1. **Authorize** : jeton du moniteur, même `tenantId`.
2. **`GET /api/reservations`** : la réservation de l'élève apparaît. Copier son `id`.
3. **`PATCH /api/reservations/{id}/confirm`** : `"reservationStatus": "CONFIRMED"`.

## Étape 14 — L'élève paie par Mobile Money

Avec `PAYMENT_PROVIDER=SIMULATED` (par défaut), aucun argent ne circule.

1. **Authorize** : jeton élève.
2. **`POST /api/payments`** :

   ```json
   { "amount": 50000, "method": "MOMO", "motif": "INSCRIPTION", "phoneNumber": "677112233" }
   ```

   - Attendu : **201**, `"paymentStatus": "PENDING"` (avec Campay, l'élève confirme alors sur son téléphone).
3. **`POST /api/payments/{id}/refresh`** : `"paymentStatus": "VALIDATE"`.
   - En simulation, un numéro finissant par `0000` donne `REJECTED`.

Côté moniteur :
- **`GET /api/payments/summary`** : total encaissé.
- Paiement en espèces : `POST /api/payments` avec `"studentsId"` et `"method": "CASH"`.

## Étape 15 — Déconnexion

**`POST /api/auth/logout`** (admin : **`POST /api/platform/admin/logout`**)

- Attendu : **200**, « Déconnexion réussie ».
- Ensuite, le même jeton est refusé avec **401** (essayez `GET /api/auth/me`). Une nouvelle connexion donne un nouveau jeton valide.

---

## Récapitulatif des emails

| Moment | Destinataire | Objet |
|--------|--------------|-------|
| Inscription (élève ou moniteur) | le nouvel inscrit | Vérifiez votre adresse email |
| Auto-école validée / refusée | le moniteur | Votre auto-école est validée / Votre demande d'auto-école |
| Demande d'adhésion envoyée | le responsable de l'auto-école | Nouvelle demande d'adhésion |
| Adhésion acceptée / refusée | l'élève (ou le moniteur salarié) | Bienvenue chez … / Votre demande d'adhésion |
| Mot de passe oublié | l'utilisateur | Réinitialisation de mot de passe |

Chaque envoi est tracé dans les logs du backend : `Email « … » envoyé à …`, ou `Échec envoi email …`
si le serveur mail est mal configuré.

## Codes de réponse à connaître

| Code | Signification |
|------|---------------|
| 400 | Donnée invalide (champ manquant, `X-Tenant-ID` absent sur une route métier, tenant inconnu) |
| 401 | Pas de jeton valide : absent, expiré, révoqué par un logout, ou `X-Tenant-ID` différent du tenant du jeton |
| 403 | Connecté, mais pas le bon rôle (un élève sur `/api/students`, par exemple) |
| 404 | Élément introuvable dans **ce** tenant |
| 409 | Conflit : doublon (email, immatriculation, créneau déjà pris, demande déjà traitée) |
