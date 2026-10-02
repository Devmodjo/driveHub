# Revue du backend : problèmes trouvés et corrections

L'architecture n'a pas changé : un schéma PostgreSQL par auto-école, tenant transmis par l'en-tête
`X-Tenant-ID`, filtres `TenantResolutionFilter` puis `JwtAuthenticationFilter`, découpage
`application / domain / infrastructure`. Les corrections rendent cette logique **effective**.

## 1. Le multi-tenant ne fonctionnait pas (bloquant)

| Problème | Conséquence | Correction |
|---|---|---|
| `hibernate.default_schema: public` dans `application.yaml` | Hibernate préfixait toutes les tables par `public.` : le `SET search_path` du `SchemaMultiTenantConnectionProvider` n'avait aucun effet, toutes les données métier allaient dans `public` | Réglage supprimé |
| `@Table(schema = "public")` sur `Student`, `Monitor`, `DrivingSchool` | Même effet pour ces tables | Schéma retiré : elles suivent le tenant courant |
| La table `public.tenants` n'était jamais remplie | `isValidTenant` renvoyait toujours `false` : toute route métier répondait 400 « Tenant invalide » | `TenantProvisioningService` enregistre le tenant à l'approbation, le désactive à la suspension |
| `TenantContext.setTenantId(...)` au milieu d'une méthode `@Transactional` | La session Hibernate déjà ouverte reste sur `public` : le changement est ignoré | `TenantExecutor` ouvre une nouvelle transaction dans le schéma voulu |
| Le JWT contenait toujours `tenant = "public"` (login appelé sur une route publique) | La vérification « tenant du token = X-Tenant-ID » de votre filtre ne pouvait jamais réussir | `UserTenantResolver` calcule le schéma de l'utilisateur au login ; votre méthode `generateToken(user, tenantId)` est utilisée |
| Vérification ignorée quand le token n'avait pas de tenant | Un utilisateur sans auto-école pouvait envoyer n'importe quel `X-Tenant-ID` | Le token doit porter exactement le tenant demandé |
| Nom de schéma concaténé dans du SQL sans contrôle | Injection SQL possible via le nom d'auto-école | `TenantSchemas` : noms validés (`^[a-z][a-z0-9_]{1,62}$`), préfixe `ae_`, suffixe unique |
| Script tenant exécuté en brut une seule fois | Impossible de faire évoluer les tables des auto-écoles existantes | Flyway par schéma (`db/migration/tenant`) + `TenantMigrationRunner` au démarrage |

## 2. Flux métier qui ne pouvaient pas aboutir

- **Adhésion à une auto-école** : `requestJoin` exigeait `REGISTERED`, mais les élèves étaient créés `ACTIVE` et les moniteurs passent `EMAIL_VERIFIED`, donc personne ne pouvait faire de demande. L'approbation exigeait `hasRole('ADMIN')`, rôle qui n'existe pas. L'auto-école était cherchée dans le tenant avec l'id du registre, donc toujours introuvable. La fiche élève était créée vide, ce qui faisait échouer les colonnes `NOT NULL`. Enfin, la clé étrangère `school_join_request → driving_school` refusait l'id du registre.
  → Email vérifié exigé, rôle `MONITOR`, contrôle que l'approbateur est le responsable de l'auto-école, profil copié dans le tenant, migration `V8`.
- **Catalogue public** : il lisait `driving_school`, qui est désormais dans chaque tenant. Il lit maintenant le registre (auto-écoles approuvées).
- **Approbation** : une même demande pouvait être approuvée deux fois. Le moniteur fondateur n'existait pas dans le schéma de son auto-école.

## 3. Entités et DTOs

- `EntityBase` :
  - `@Data` remplacé par `@Getter/@Setter`, car un `equals/hashCode` généré sur une entité JPA casse les `Set` ;
  - `@LastModifiedDate` ne faisait rien sans `@EnableJpaAuditing`, remplacé par `@UpdateTimestamp` ;
  - `deletedAt` était mis à jour à chaque modification ;
  - `@Where`, déprécié, remplacé par `@SQLRestriction`.
- `VehiclesRepository` et `ExamsRepository` étaient typés `Long` alors que les identifiants sont des UUID.
- `@NotBlank` et `@Positive` étaient placés sur des UUID, des enums ou des dates : la validation plantait. Remplacés par `@NotNull`.
- `StudentsResponseDto` exposait l'entité `User` complète, mot de passe haché compris.
- `Reservation.dateTime` portait `@CreationTimestamp` : le créneau choisi était écrasé par la date de création.
- `Payment.amount` était un `Double` : il passe en `BigDecimal` / `numeric(12,2)`. Le statut d'un paiement était fourni par le client ; il est désormais fixé par le serveur.
- Les DTOs de réservation étaient rangés dans le module `vehicle`. Les modules métier sont réorganisés en `application / domain / infrastructure`, comme `auth`. Le dossier `infrastucture` est renommé `infrastructure`.
- Le `drivingSchoolId` est retiré des requêtes métier : dans un schéma tenant, l'auto-école est implicite (`CurrentSchoolProvider`).

## 4. Configuration et erreurs

- `DataSourceConfig` contenait l'utilisateur et le mot de passe en dur : supprimé, la connexion vient de `application.yaml` et des variables d'environnement.
- CORS : la configuration était à la fois désactivée et réactivée, avec des `@CrossOrigin("*")` sur chaque contrôleur. Une seule source reste : `cors.allowed-origins`.
- `GlobalHandlerException` :
  - les 403 devenaient des 500 (vos tests d'intégration le détectaient) ;
  - `IllegalArgumentException` et `IllegalStateException` renvoyaient 500 ;
  - le message des erreurs 500 (SQL, chemins de fichiers) était renvoyé au client.
- `/error` est autorisé dans `SecurityConfig` : sinon les 401/400 envoyés par vos filtres devenaient des 403 vides.
- Login : message identique pour un email inconnu et un mauvais mot de passe, compte suspendu bloqué, email normalisé en minuscules.
- `resend-verification` ne révèle plus si un compte existe.
- Logs en `INFO` au lieu de `DEBUG`. `repair-on-migrate` retiré : il masquait les migrations modifiées après coup.

## 5. Ajouté pour compléter le backend

Services et contrôleurs `students`, `monitors`, `vehicles`, `courses`, `exams` (avec inscriptions),
`reservations` et `payments` (voir le README). Script `scripts/e2e-multitenant.sh` : 47 vérifications
de bout en bout, isolation entre auto-écoles comprise.

## 6. Paiement, endpoints pour le frontend, détails

- Paiement Mobile Money par Campay derrière une interface `PaymentGateway` (passerelle `SIMULATED` en
  développement, `CAMPAY` en production) ; webhook signé `/api/webhooks/campay` ; `POST /api/payments/{id}/refresh`.
- `POST /api/auth/refresh-token` : nouveau jeton qui contient le tenant dès que l'auto-école (ou l'adhésion)
  est validée, sans se déconnecter.
- `GET /api/driving-schools/me` (statut de la demande du moniteur) et `GET /api/join-school/me`
  (statut des demandes d'adhésion) pour l'écran d'accueil du dashboard.
- `EmailService` : l'URL du frontend était écrite en dur (`https://localhost:4200`) et les liens du
  back-office pointaient vers des pages inexistantes ; elle vient maintenant de `app.frontend-url`.
- Migration `V9` : reprise des auto-écoles validées avec l'ancien code (pas besoin de supprimer la base).
