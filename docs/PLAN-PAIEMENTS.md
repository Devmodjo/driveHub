# Plan de mise en oeuvre des paiements

> Destinataires : le porteur du projet (décisions, démarches) et le développeur qui réalisera le travail.
> Les concepts (offres, cycle de vie d'un abonnement, avoirs, règles de sécurité générales) sont expliqués dans
> [ABONNEMENTS-ET-PAIEMENTS.md](ABONNEMENTS-ET-PAIEMENTS.md) : ce document ne les répète pas. Il dit **quoi faire,
> dans quel ordre, dans quels fichiers, et comment savoir que c'est terminé**.

Chemins abrégés utilisés plus bas :

- `BE` = `backend/src/main/java/cm/mvtech/drivehub`
- `MIG` = `backend/src/main/resources/db/migration` (sous-dossiers `public/` et `tenant/`)
- `FE` = `frontend/src/app`

---

## 1. État actuel

| Flux | Ce qui existe | Ce qui manque |
|------|---------------|---------------|
| **Élève -> auto-école** (inscription, examens) | `PaymentGateway` (stratégie), `SimulatedPaymentGateway`, `CampayPaymentGateway`, `PaymentGatewayResolver`, `PaymentService`, `PaymentWebhookService` (signature JWT HS256), `PaymentController` (`/api/payments`), `PaymentWebhookController` (`/api/webhooks/campay`), migration tenant `V4__paiement_mobile_money.sql`, config `payment.*`, tests `PaymentServiceTest`, `PaymentWebhookServiceTest`, `PaymentGatewaysTest` | Passage en argent réel (voir écarts ci-dessous), vue de rapprochement, remboursements, journal des actions |
| **Auto-école -> DriveHub** (abonnement) | `SubscriptionService.startTrial`, `SubscriptionController` (`GET /api/driving-schools/me/subscription`, `GET /api/platform/subscriptions`), `BillingProperties` (`billing.enabled=false`, `trial-days=15`, `default-plan=ESSENTIEL`), migration publique `V15__abonnements.sql` (tables `subscription_plans`, `school_subscriptions`, `subscription_invoices` vide) | Entité et génération des factures, PDF, emails, encaissement Campay, relances, suspension, comptabilité |

### Écarts constatés dans le code actuel (paiements élèves)

Relevés en lisant `PaymentService` et `PaymentWebhookService` ; ils sont traités en phase 1.

| # | Constat | Gravité |
|---|---------|---------|
| E1 | Les identifiants Campay (`payment.campay.*`) sont **globaux** : tout l'argent des élèves de toutes les auto-écoles arrive sur **un seul compte Campay**, celui de DriveHub. | Bloquant (décision D1) |
| E2 | Le montant vient de la requête (`PaymentRequestDto.amount`) : l'élève choisit ce qu'il paie. Aucun barème côté serveur. | Haute |
| E3 | Le webhook applique le statut reçu (`CampayPaymentGateway.toResult`) **sans redemander à Campay** (`checkStatus`) et sans comparer le montant. | Haute |
| E4 | `PaymentService.validate` permet au moniteur de valider à la main un paiement **CAMPAY** en attente : il peut être marqué payé alors que l'argent n'arrivera jamais. | Moyenne |
| E5 | Pas de verrou entre webhook et bouton « Vérifier » (`refresh`) : deux traitements simultanés possibles (sans effet grave aujourd'hui, mais à fermer). | Faible |
| E6 | Pas de limitation de débit sur `POST /api/payments` (un élève peut déclencher des dizaines de demandes USSD). | Moyenne |
| E7 | Aucune trace de **qui** a validé / rejeté un paiement à la main. | Moyenne |
| E8 | Si `payment.provider=SIMULATED` en production, **tout paiement est validé** sans argent réel. Aucun garde-fou au démarrage. | Haute |
| E9 | Pas de statut de remboursement (`PaymentStatus` = `PENDING`, `VALIDATE`, `REJECTED`). | Moyenne |

Déjà en place et correct : index unique `uk_payments_external_reference (provider, external_reference)` (V4),
`applyGatewayNotification` ignore un paiement qui n'est plus `PENDING` (idempotence), référence interne
`schema:idPaiement` vérifiée (`tenantService.isValidTenant`), webhook toujours en 200, jeton Campay mis en cache.

---

## 2. Prérequis à obtenir (porteur du projet)

| # | Prérequis | Qui | Délai indicatif | Nécessaire pour |
|---|-----------|-----|-----------------|-----------------|
| P1 | Compte **Campay business** + KYC (statuts, RCCM, pièce du gérant, justificatif d'adresse) | Porteur | 1 à 3 semaines | Phases 1 et 3 |
| P2 | Identifiants **de production** (`username`, `password` de l'application Campay) et **App Webhook Key**, distincts de ceux de `demo.campay.net` | Porteur (tableau de bord Campay) | 1 jour après KYC | Phases 1 et 3 |
| P3 | Une **deuxième application Campay** « DriveHub Abonnements » (identifiants et clé webhook propres) | Porteur | 1 jour | Phase 3 |
| P4 | **Décision D1** : à qui appartient l'argent des élèves ? (voir 2.1) avec avis d'un juriste / de Campay | Porteur + conseil | 1 à 2 semaines | Phase 1 |
| P5 | Identifiants légaux de DriveHub pour les factures : raison sociale, adresse, **NIU**, **RCCM**, régime fiscal, centre des impôts | Porteur | déjà disponibles ou 1 semaine | Phase 2 |
| P6 | **Taux de TVA** : 19,25 % (17,5 % + 10 % de centimes additionnels communaux) **à confirmer par un comptable**, ainsi que : prix des offres HT ou TTC ? mentions obligatoires ? | Comptable | 1 semaine | Phase 2 |
| P7 | Compte de **règlement** (compte bancaire ou MoMo marchand) sur lequel Campay reverse les fonds | Porteur | avec P1 | Phases 1 et 3 |
| P8 | Adresse email d'expédition des factures (ex. `facturation@...`) avec SPF/DKIM (voir [EMAILS-DELIVRABILITE.md](EMAILS-DELIVRABILITE.md)) | Porteur + dev | 2 jours | Phase 2 |
| P9 | Conditions générales de vente : prix, échéance, délai de grâce, suspension, remboursements | Porteur (+ juriste) | 1 semaine | Phases 2 et 4 |

### 2.1 Décision D1 : à qui va l'argent des élèves

| Option | Principe | Pour | Contre |
|--------|----------|------|--------|
| **A. Compte Campay par auto-école** (recommandé) | Chaque auto-école crée son compte Campay ; ses identifiants sont saisis dans DriveHub et stockés **chiffrés** dans son schéma | DriveHub ne touche jamais l'argent des élèves ; pas de reversement à gérer | Chaque auto-école doit faire son KYC |
| B. Compte DriveHub + reversements | Un seul compte, DriveHub reverse (API de décaissement) | Simple pour l'auto-école | DriveHub devient intermédiaire de paiement (réglementation COBAC/BEAC à vérifier), comptabilité de tiers, risque de litige |

Le plan ci-dessous suppose l'option A. En attendant, les auto-écoles sans compte gardent l'encaissement à la caisse
(saisie par le moniteur, déjà possible).

---

## 3. Phases

### Phase 1 : paiements des élèves en argent réel (Campay)

**But** : un élève paie son inscription ou son examen par MTN MoMo / Orange Money, l'argent arrive sur le compte de
son auto-école, et le responsable peut rapprocher ses encaissements avec son relevé Campay.

**Tâches**

- [ ] Garde-fou au démarrage : refuser `payment.provider=SIMULATED` quand le profil Spring `prod` est actif (E8).
- [ ] Identifiants Campay par auto-école (D1, option A) : table tenant `payment_settings`, secrets chiffrés avec le service de chiffrement existant (`security.encryption.master-key`) ; `CampayPaymentGateway` reçoit les identifiants de l'auto-école courante au lieu de `PaymentProperties.campay()` (cache de jeton par schéma). La clé webhook reste globale si Campay le permet, sinon par auto-école.
- [ ] Barème côté serveur (E2) : table tenant `fee_schedule` (motif, catégorie de permis, montant) ; l'élève choisit un **motif**, le serveur calcule le montant (paiement partiel autorisé seulement jusqu'au reste dû).
- [ ] Double vérification (E3) : dans `PaymentWebhookService`, après signature valide, appeler `gateway.checkStatus(reference)` et appliquer **ce** résultat ; comparer le montant renvoyé par Campay avec `payment.amount` (ajouter `amount` à `GatewayResult`). Écart = paiement laissé `PENDING` + log `WARN`.
- [ ] Interdire `validate` / `reject` manuels pour `provider != MANUAL` (E4) ; à la place, bouton « Vérifier » (`refresh`).
- [ ] Verrou (E5) : colonne `version` + `@Version` sur `Payment`, ou `findByIdForUpdate` (`@Lock(PESSIMISTIC_WRITE)`) dans `applyGatewayNotification` et `refresh`.
- [ ] Tâche planifiée `PendingPaymentReconciler` (`@Scheduled`, toutes les 10 min) : pour chaque schéma actif, re-vérifie les paiements `PENDING` de plus de 5 min ; passe en `REJECTED` (« expiré ») après 24 h. `@EnableScheduling` est déjà présent sur `DriveHubApplication`.
- [ ] Limitation de débit (E6) : 3 initiations par élève et par 10 min, 20 par auto-école et par minute (filtre maison en mémoire ou Bucket4j). Réponse 429 avec message clair.
- [ ] Journal (E7) : colonnes `validated_by`, `validated_at`, `status_reason` sur `payments`.
- [ ] Remboursements (E9) : statut `REFUNDED` ; remboursement **fait hors DriveHub** par l'auto-école (Campay n'annule pas un débit), puis enregistré dans DriveHub avec motif et auteur. Politique écrite dans les CGV de l'auto-école.
- [ ] Messages d'erreur : traduire les cas Campay (solde insuffisant, délai dépassé, numéro non Mobile Money, service indisponible) en messages français affichés par `FE/components/dashboard/pages/payments`.
- [ ] Vue de rapprochement pour le responsable : filtres date / statut / fournisseur, totaux, référence Campay visible, export CSV.
- [ ] Recette sur `demo.campay.net` (montants de test), puis 3 paiements réels de 100 FCFA en production.

**Fichiers**

| Action | Fichier |
|--------|---------|
| Modifier | `BE/modules/payment/domain/services/PaymentService.java`, `PaymentWebhookService.java` |
| Modifier | `BE/modules/payment/infrastructure/gateway/CampayPaymentGateway.java`, `PaymentGatewayResolver.java` (garde-fou), `PaymentProperties.java` |
| Modifier | `BE/modules/payment/domain/gateway/GatewayResult.java` (ajout `amount`), `BE/modules/payment/domain/model/Payment.java`, `BE/modules/enums/PaymentStatus.java` (`REFUNDED`) |
| Modifier | `BE/modules/payment/application/dto/PaymentRequestDto.java` (montant facultatif pour l'élève), `PaymentController.java`, `PaymentsRepository.java` |
| Créer | `BE/modules/payment/domain/model/FeeSchedule.java`, `PaymentSettings.java` + dépôts ; `BE/modules/payment/domain/services/PendingPaymentReconciler.java`, `PaymentExportService.java` |
| Créer | `BE/core/infrastructure/filter/PaymentRateLimitFilter.java` |
| Modifier | `FE/components/dashboard/pages/payments/payments.component.ts` (+ template) |

**Migration** : `MIG/tenant/V6__paiements_production.sql` (tables `fee_schedule`, `payment_settings` ; colonnes
`version`, `validated_by`, `validated_at`, `status_reason`, `refunded_at` sur `payments` ; contrainte de statut
élargie à `REFUNDED`). Toutes les colonnes ajoutées sont nullables ou avec valeur par défaut.

**Endpoints**

| Méthode | Chemin | Rôles |
|---------|--------|-------|
| GET / PUT | `/api/payments/settings` (identifiants Campay de l'auto-école, secrets jamais renvoyés) | MONITOR responsable |
| GET / PUT | `/api/payments/fees` (barème) | MONITOR responsable (PUT), MONITOR et STUDENT (GET) |
| GET | `/api/payments?from=&to=&status=&provider=` | MONITOR, STUDENT (les siens) |
| GET | `/api/payments/export.csv?from=&to=` | MONITOR responsable |
| PATCH | `/api/payments/{id}/refund` (body : `reason`) | MONITOR responsable |

**Tests**

- Unitaires : garde-fou SIMULATED en `prod` ; montant calculé depuis le barème (montant client ignoré) ; webhook SUCCESSFUL mais `checkStatus` FAILED -> `REJECTED` ; montant Campay différent -> reste `PENDING` ; `validate` refusé pour un paiement CAMPAY ; réconciliateur (PENDING > 24 h -> expiré) ; limite de débit -> 429.
- Intégration (style `BusinessRoutesIntegrationTest`) : webhook rejoué deux fois -> un seul changement d'état ; webhook avec signature fausse -> ignoré, 200 ; élève A ne peut pas `refresh` le paiement de l'élève B ; export CSV réservé au responsable.

**Critères d'acceptation** : un paiement réel de 100 FCFA passe `VALIDATE` via le webhook **et** via « Vérifier » ;
le montant ne peut pas être modifié depuis le navigateur ; l'application refuse de démarrer en `prod` avec
`SIMULATED` ; le total « encaissé » du mois correspond au relevé Campay de l'auto-école.

**Charge** : 6 à 8 jours (hors délai KYC).

---

### Phase 2 : émission des factures d'abonnement (encaissement manuel)

**But** : chaque mois, une facture conforme est créée, numérotée sans trou, envoyée en PDF au responsable ; le
back-office la marque payée à la main (virement, espèces, MoMo direct).

**Tâches**

- [ ] Entité `SubscriptionInvoice` sur la table existante + colonnes manquantes (V18).
- [ ] Numérotation sans trou : `InvoiceNumberService` lit et incrémente `invoice_number_counters` (une ligne par année) avec `SELECT ... FOR UPDATE` **dans la transaction de création**. Ne pas utiliser une séquence PostgreSQL seule : elle saute des numéros en cas d'annulation de transaction. Format `DH-2026-000123` ; avoirs `AV-2026-000007`.
- [ ] Calcul HT / TVA / TTC en entiers FCFA (arrondi au franc, règle validée par le comptable, P6). Taux lu dans la configuration, **copié dans la facture** (une facture garde son taux même si la loi change).
- [ ] Copie (« instantané ») des informations client et vendeur dans la facture : nom de l'auto-école, adresse, NIU client si connu, offre, prix.
- [ ] `InvoiceGenerationJob` : `@Scheduled(cron = "0 0 2 * * *", zone = "Africa/Douala")`, ne fait rien si `billing.enabled=false` ; cible les abonnements dont l'essai ou la période se termine dans les 3 jours ; contrainte unique `(subscription_id, period_start)` pour qu'un double lancement ne crée pas deux factures.
- [ ] PDF : `InvoicePdfRenderer` (gabarit Thymeleaf déjà présent + dépendance `com.openhtmltopdf:openhtmltopdf-pdfbox`). PDF régénéré à la demande depuis des données immuables ; empreinte SHA-256 stockée.
- [ ] Email au responsable avec le PDF en pièce jointe, via `EmailService` (`BE/modules/auth/domain/services/EmailService.java`) et un gabarit dans `templates/emails/`.
- [ ] Immuabilité : déclencheur PostgreSQL qui refuse `DELETE` et toute modification de `number`, `amount_*`, `vat_*`, `period_*` ; seuls `status`, `paid_at`, `provider`, `external_reference` évoluent. Correction = avoir (`CREDIT_NOTE`, montant négatif, lien vers la facture d'origine).
- [ ] Journal `billing_audit_log` : chaque action manuelle (marquer payée, avoir, prolongation) enregistre l'administrateur, la date, l'ancien et le nouvel état, le motif.
- [ ] Pages : « Mes factures » (`FE/components/dashboard/pages/invoices/`), « Factures » au back-office (`FE/components/back-office/pages/billing/`).

**Fichiers à créer** (dans `BE/modules/subscription/`)

`domain/model/SubscriptionInvoice.java`, `InvoiceStatus.java`, `BillingAuditEntry.java` ;
`domain/services/InvoiceService.java`, `InvoiceNumberService.java`, `InvoiceGenerationJob.java`, `InvoicePdfRenderer.java`, `InvoiceMailer.java` ;
`infrastructure/repository/SubscriptionInvoiceRepository.java`, `BillingAuditRepository.java` ;
`application/controller/InvoiceController.java`, `PlatformBillingController.java` ; DTO `InvoiceResponse`, `MarkPaidRequest`, `CreditNoteRequest` ;
gabarits `backend/src/main/resources/templates/invoices/invoice.html`, `templates/emails/invoice-issued.html`.
À modifier : `BillingProperties.java` (ajout `vatRate`, `dueDays`, `graceDays`, `Company company` : raison sociale, adresse, NIU, RCCM), `application.yaml` (`billing.*`), `SubscriptionService.java`, `backend/pom.xml`.

**Migration** : `MIG/public/V18__factures_emission.sql` : colonnes `subtotal_ht_fcfa`, `vat_rate`, `vat_fcfa`,
`due_at`, `sent_at`, `credit_of_invoice_id`, `customer_snapshot` (jsonb), `pdf_sha256` ; table
`invoice_number_counters` ; table `billing_audit_log` ; contrainte unique `(subscription_id, period_start)` hors
avoirs ; déclencheur d'immuabilité.

**Endpoints**

| Méthode | Chemin | Rôles |
|---------|--------|-------|
| GET | `/api/driving-schools/me/invoices` | MONITOR responsable |
| GET | `/api/driving-schools/me/invoices/{id}/pdf` | MONITOR responsable (sa facture uniquement) |
| GET | `/api/platform/invoices?status=&month=` | ROOT, SUPER_ADMIN |
| POST | `/api/platform/invoices/{id}/mark-paid` (mode, référence, motif) | ROOT, SUPER_ADMIN |
| POST | `/api/platform/invoices/{id}/credit-note` (montant, motif) | ROOT |
| POST | `/api/platform/billing/run` (lancement manuel, idempotent) | ROOT |

**Tests**

- Unitaires : numérotation (100 créations concurrentes -> 100 numéros consécutifs, aucun doublon ni trou) ; calcul TVA et arrondi ; job inactif si `billing.enabled=false` ; offre « Sur mesure » (prix nul) -> pas de facture automatique, alerte back-office ; avoir ne dépasse pas la facture d'origine.
- Intégration : déclencheur refuse `UPDATE amount_fcfa` et `DELETE` ; job lancé deux fois -> une seule facture ; un responsable ne télécharge pas la facture d'une autre auto-école (404) ; `mark-paid` écrit dans `billing_audit_log`.

**Critères d'acceptation** : facture PDF relue et validée par le comptable ; numéros consécutifs sur un mois de
test ; aucune facture modifiable ni supprimable en base ; email reçu par le responsable.

**Charge** : 8 à 10 jours.

---

### Phase 3 : paiement en ligne des factures (Campay)

**But** : le responsable paie sa facture depuis DriveHub par Mobile Money ; la facture passe `PAID` et
l'abonnement `ACTIVE` sans intervention humaine.

**Tâches**

- [ ] Extraire un `CampayClient` (jeton, `collect`, `transaction`) de `CampayPaymentGateway` pour l'utiliser avec **deux** jeux d'identifiants : élèves (`payment.campay.*` ou identifiants de l'auto-école) et abonnements (`billing.campay.*`, application Campay P3).
- [ ] Table `subscription_payment_attempts` : une facture peut avoir plusieurs tentatives (échec puis nouvel essai). Référence interne envoyée à Campay : `INV:<idFacture>:<numéroTentative>` ; le préfixe `INV:` la distingue d'un paiement élève (`schema:id`).
- [ ] Montant lu dans la facture (TTC restant dû), jamais dans la requête.
- [ ] Route dédiée `/api/webhooks/campay-billing` (URL de l'application Campay « Abonnements ») ; `PaymentWebhookService` ignore toute référence `INV:` et `BillingWebhookService` ignore toute référence sans ce préfixe.
- [ ] Traitement : signature -> analyse de la référence -> `checkStatus` auprès de Campay -> comparaison montant / devise / référence -> dans **une** transaction : tentative `SUCCESSFUL`, facture `PAID` (+ `external_reference`, unique), abonnement `ACTIVE`, période +1 mois -> email de reçu après validation de la transaction.
- [ ] Bouton « Vérifier » pour le responsable et réconciliation planifiée des tentatives `PENDING` (comme en phase 1).
- [ ] Limite : une seule tentative `PENDING` à la fois par facture ; 3 tentatives par heure.

**Fichiers** : créer `BE/modules/payment/infrastructure/gateway/CampayClient.java`,
`BE/modules/subscription/domain/model/InvoicePaymentAttempt.java`,
`BE/modules/subscription/domain/services/InvoicePaymentService.java`, `BillingWebhookService.java`,
`BE/modules/subscription/application/controller/BillingWebhookController.java` ; modifier
`CampayPaymentGateway.java`, `PaymentWebhookService.java`, `BillingProperties.java` (`campay`), `application.yaml`,
`SecurityConfig.java` uniquement si la règle `/api/webhooks/**` ne couvre pas la nouvelle route (elle la couvre
aujourd'hui, ainsi que `TenantResolutionFilter`).

**Migration** : `MIG/public/V19__factures_paiement_en_ligne.sql` (table `subscription_payment_attempts`, index
uniques sur `internal_reference` et `(provider, external_reference)`).

**Endpoints**

| Méthode | Chemin | Rôles |
|---------|--------|-------|
| POST | `/api/driving-schools/me/invoices/{id}/pay` (body : `phone`) | MONITOR responsable |
| POST | `/api/driving-schools/me/invoices/{id}/refresh` | MONITOR responsable |
| GET, POST | `/api/webhooks/campay-billing` | public, signature obligatoire |

```mermaid
sequenceDiagram
    autonumber
    actor R as Responsable
    participant F as Frontend
    participant API as InvoicePaymentService
    participant DB as schéma public
    participant C as Campay (app Abonnements)
    participant W as BillingWebhookService

    R->>F: Payer la facture DH-2026-000123
    F->>API: POST /api/driving-schools/me/invoices/{id}/pay {phone}
    API->>DB: facture ISSUED de SON auto-école ? aucune tentative PENDING ?
    API->>DB: tentative PENDING, référence INV:{id}:1
    API->>C: POST /collect/ (montant lu en base, external_reference INV:{id}:1)
    C-->>R: demande de code PIN
    R->>C: code PIN
    C->>W: GET/POST /api/webhooks/campay-billing (signature)
    W->>W: signature valide ? préfixe INV: ? tentative connue et PENDING ?
    W->>C: GET /transaction/{reference}/ (double vérification)
    C-->>W: SUCCESSFUL, montant, devise
    W->>DB: transaction : tentative SUCCESSFUL, facture PAID, abonnement ACTIVE, période +1 mois
    W-->>R: email « paiement reçu » + reçu PDF
    Note over W,DB: Webhook rejoué : tentative déjà SUCCESSFUL -> rien ; external_reference UNIQUE en dernier rempart
```

**Tests**

- Unitaires : référence `INV:` mal formée ou inconnue -> ignorée ; webhook SUCCESSFUL mais transaction FAILED chez Campay -> facture inchangée ; montant différent -> alerte, facture inchangée ; référence élève reçue sur la route abonnements -> ignorée (et inversement).
- Intégration : webhook rejoué 3 fois -> une seule facture `PAID`, une seule prolongation ; deux paiements simultanés de la même facture -> le second est refusé (409) ; un responsable ne peut pas payer la facture d'une autre auto-école.

**Critères d'acceptation** : facture réelle de 100 FCFA (offre de test) payée de bout en bout ; un rejeu du webhook
ne change rien ; le relevé Campay « Abonnements » et la liste des factures `PAID` concordent.

**Charge** : 5 à 7 jours.

---

### Phase 4 : relances et suspension

**But** : une facture impayée déclenche des rappels puis, à défaut, la lecture seule ; le paiement réactive
immédiatement l'espace. Le calendrier (J0, J+3, J+6, suspension à J+7) suit ABONNEMENTS-ET-PAIEMENTS.md, section 2.

**Tâches**

- [ ] `DunningJob` quotidien (8 h, Africa/Douala) : `ACTIVE`/`TRIAL` avec facture échue -> `PAST_DUE` + email J0 ; emails J+3 et J+6 ; J+7 -> `SUSPENDED` + email. Table `subscription_reminders` avec unicité `(invoice_id, kind)` : jamais deux fois le même rappel.
- [ ] `SubscriptionAccessFilter` (après `TenantResolutionFilter`) : si `billing.enabled` et auto-école `SUSPENDED`, refuse `POST`/`PUT`/`PATCH`/`DELETE` avec 403 et code `SUBSCRIPTION_SUSPENDED` ; laisse passer `GET`, `/api/auth/**`, `/api/webhooks/**`, `/api/driving-schools/me/invoices/**`. Statut mis en cache 60 s par schéma.
- [ ] Décision porteur : les élèves d'une auto-école suspendue peuvent-ils encore payer leur auto-école ? (recommandé : oui, ne pas pénaliser l'élève).
- [ ] Réactivation : paiement reçu (phase 3 ou `mark-paid`) -> `ACTIVE`, période calculée depuis la fin de la période précédente (pas de jours offerts), cache invalidé.
- [ ] Prolongation manuelle d'un délai de grâce par ROOT, avec motif (journal).
- [ ] Frontend : bandeau `PAST_DUE` / `SUSPENDED` dans le tableau de bord, intercepteur HTTP qui traduit `SUBSCRIPTION_SUSPENDED` en message avec lien « Payer ma facture ».

**Fichiers** : créer `BE/modules/subscription/domain/services/DunningJob.java`, `SubscriptionAccessService.java`,
`BE/core/infrastructure/filter/SubscriptionAccessFilter.java`, gabarits `templates/emails/invoice-reminder.html`,
`subscription-suspended.html` ; modifier `SubscriptionService.java`, `SubscriptionStatus.java` (aucune valeur
nouvelle), intercepteur HTTP Angular et composant de bandeau dans `FE/components/dashboard/`.

**Migration** : `MIG/public/V20__relances_suspension.sql` (table `subscription_reminders` ; colonnes
`past_due_since`, `suspended_at`, `grace_extended_until` sur `school_subscriptions`).

**Endpoints** : `POST /api/platform/subscriptions/{id}/extend-grace` (ROOT, body : `until`, `reason`) ;
`POST /api/platform/subscriptions/{id}/suspend` et `/reactivate` (ROOT, motif obligatoire).

**Tests** : unitaires sur chaque transition datée (horloge injectée `java.time.Clock`) ; intégration : auto-école
suspendue -> `GET` 200, `POST /api/reservations` 403, `POST .../invoices/{id}/pay` autorisé ; job relancé le même
jour -> aucun email en double ; `billing.enabled=false` -> filtre inactif.

**Critères d'acceptation** : scénario complet simulé sur 8 jours (horloge de test) conforme au calendrier ; aucune
donnée supprimée ; réactivation effective en moins d'une minute après paiement.

**Charge** : 4 à 5 jours.

---

### Phase 5 : comptabilité dans le back-office

**But** : le porteur et son comptable disposent du journal des ventes, des encaissements, d'exports et
d'indicateurs, sans requête SQL.

**Tâches**

- [ ] Journal des ventes mensuel : factures et avoirs (numéro, date, auto-école, HT, TVA, TTC, statut), totaux.
- [ ] Encaissements : factures payées avec mode, référence Campay ou manuelle, date ; écarts de rapprochement signalés.
- [ ] Export CSV (séparateur `;`, UTF-8 avec BOM pour Excel, neutralisation des cellules commençant par `=`, `+`, `-`, `@`).
- [ ] Indicateurs : MRR, essais en cours, taux de conversion essai -> payant, montant impayé, nombre de suspensions.
- [ ] Pages `FE/components/back-office/pages/accounting/` et service `FE/services/billing-service/`.

**Fichiers** : `BE/modules/subscription/domain/services/AccountingService.java`, `AccountingCsvExporter.java`,
`BE/modules/subscription/application/controller/AccountingController.java`, DTO `SalesJournalLine`,
`BillingKpis`. Pas de nouvelle table prévue (migration `V21` seulement si des vues SQL s'avèrent utiles).

**Endpoints** (tous `@PreAuthorize("hasRole('ROOT') || hasRole('SUPER_ADMIN')")`)

| Méthode | Chemin |
|---------|--------|
| GET | `/api/platform/accounting/sales-journal?month=2026-11` |
| GET | `/api/platform/accounting/payments?from=&to=` |
| GET | `/api/platform/accounting/export.csv?type=sales\|payments&month=` |
| GET | `/api/platform/accounting/kpis` |

**Tests** : unitaires sur le calcul du MRR et du taux de conversion (jeu de données connu), échappement CSV ;
intégration : un REVIEWER ou un MONITOR reçoit 403 ; totaux de l'export = totaux du journal.

**Critères d'acceptation** : le comptable importe l'export d'un mois sans retouche ; les totaux concordent avec les
relevés Campay et bancaires.

**Charge** : 4 à 5 jours.

---

## 4. Calendrier indicatif

| Phase | Contenu | Charge (jours) | Dépend de | Activable sans la suivante ? |
|-------|---------|----------------|-----------|------------------------------|
| 0 | Démarches P1 à P9 (porteur) | 2 à 4 semaines calendaires | - | - |
| 1 | Paiements élèves en réel | 6 à 8 | P1, P2, P4, P7 | Oui |
| 2 | Factures d'abonnement (encaissement manuel) | 8 à 10 | P5, P6, P8, P9 | Oui |
| 3 | Paiement en ligne des factures | 5 à 7 | Phase 2, P3 | Oui |
| 4 | Relances et suspension | 4 à 5 | Phase 2 (3 conseillée) | Oui |
| 5 | Comptabilité back-office | 4 à 5 | Phase 2 | Oui |
| | **Total développement** | **27 à 35 jours** | | |

Les phases 1 et 2 peuvent avancer en parallèle (deux flux indépendants).

---

## 5. Liste de contrôle sécurité (à cocher avant chaque mise en production)

- [ ] **Montants côté serveur** : barème (élèves) et facture (abonnements) ; aucun montant lu dans une requête d'élève ou de responsable.
- [ ] **Signature + double vérification** : webhook signé (JWT HS256, clé webhook) **puis** `GET /transaction/{reference}/` ; montant, devise et référence comparés.
- [ ] **Idempotence** : traitement seulement si l'état est encore `PENDING` ; index uniques sur `external_reference` ; jobs protégés par contraintes uniques.
- [ ] **Verrous** : `@Version` ou `SELECT ... FOR UPDATE` sur paiements, tentatives et compteur de numéros.
- [ ] **Aucun secret dans Git** : `CAMPAY_*`, `BILLING_CAMPAY_*`, clé de chiffrement en variables d'environnement ; identifiants des auto-écoles chiffrés en base et jamais renvoyés par l'API.
- [ ] **Séparation test / production** : `demo.campay.net` et identifiants de test en recette ; `www.campay.net` uniquement en production ; refus de `SIMULATED` en `prod` ; contrôle au démarrage que `base-url` et profil sont cohérents.
- [ ] **Rôles** : `@PreAuthorize` sur chaque route ; contrôle de propriété (le responsable ne voit que les factures de son auto-école) ; tests 403/404 écrits.
- [ ] **Journal d'audit** : toute action manuelle (valider, rejeter, rembourser, marquer payée, avoir, suspendre, prolonger) avec auteur, date, motif.
- [ ] **Limitation de débit** sur toutes les initiations de paiement (429).
- [ ] **Journaux** sans numéro de téléphone complet ni secret (masquer : `2376****1122`).
- [ ] **Webhook** toujours en 200, sans détail d'erreur dans la réponse.
- [ ] **Rapprochement hebdomadaire** relevé Campay / base, écarts traités et notés.

---

## 6. Mise en service et retour arrière

**Mise en service**

1. Tout développer et tester avec `PAYMENT_PROVIDER=SIMULATED` en local, puis `CAMPAY` sur `CAMPAY_BASE_URL=https://demo.campay.net/api` en recette.
2. Phase 1 : une auto-école pilote, 3 paiements réels de 100 FCFA, rapprochement avec son relevé, puis ouverture aux autres.
3. Phases 2 à 4 : déployer avec `BILLING_ENABLED=false` (le code est présent mais inactif) ; vérifier en recette avec `BILLING_ENABLED=true` et une horloge avancée.
4. Prévenir les auto-écoles depuis le back-office (page Emails) **au moins 15 jours** avant la première facture : prix, date, moyens de paiement, CGV.
5. Activer `BILLING_ENABLED=true` en production ; premier mois : encaissement manuel autorisé en parallèle ; relances actives mais suspension prolongée manuellement si besoin.
6. Phase 5 dès la clôture du premier mois facturé.

**Retour arrière**

| Problème | Action |
|----------|--------|
| Facturation incorrecte | `BILLING_ENABLED=false` : plus de factures, de relances ni de suspension ; corriger par avoirs, jamais par suppression |
| Suspension à tort | `POST /api/platform/subscriptions/{id}/reactivate` (motif) ; ou `BILLING_ENABLED=false` qui désactive le filtre |
| Paiements élèves défaillants | Désactiver le paiement en ligne (nouveau réglage `payment.online-enabled=false`) : la caisse manuelle reste disponible. **Ne jamais repasser en `SIMULATED` en production** |
| Migration en cause | Migrations uniquement additives (colonnes nullables, nouvelles tables) : on redéploie la version précédente du code sans toucher au schéma |

---

## 7. Risques et parades

| Risque | Probabilité | Impact | Parade |
|--------|-------------|--------|--------|
| KYC Campay long ou refusé | Moyenne | Retard des phases 1 et 3 | Lancer P1 immédiatement ; phase 2 (manuelle) ne dépend pas de Campay |
| DriveHub requalifié intermédiaire de paiement (option B) | Moyenne | Juridique, sanctions | Option A (compte par auto-école) ; avis juridique avant toute autre option |
| Webhook falsifié ou clé divulguée | Faible | Facture ou paiement validé sans argent | Double vérification `checkStatus` + comparaison du montant ; rotation de la clé |
| Webhook perdu ou en retard | Moyenne | Paiement payé mais affiché en attente | Bouton « Vérifier » + réconciliateur planifié |
| Trou ou doublon dans la numérotation | Faible | Non-conformité fiscale | Compteur verrouillé dans la transaction, test de concurrence, déclencheur d'immuabilité |
| Mauvais taux ou mentions de TVA | Moyenne | Redressement fiscal | Validation du comptable (P6) avant activation ; taux copié dans chaque facture |
| Suspension d'une auto-école qui a payé | Faible | Perte de confiance | Rapprochement avant suspension, prolongation manuelle, réactivation immédiate |
| Job lancé deux fois (plusieurs instances) | Moyenne | Factures ou emails en double | Contraintes uniques `(subscription_id, period_start)` et `(invoice_id, kind)` |
| Mode `SIMULATED` laissé en production | Faible | Paiements validés sans argent | Refus au démarrage en profil `prod` |
| Emails de facture en spam | Moyenne | Relances non lues | SPF/DKIM (P8), factures aussi visibles dans l'application |
| Panne Campay | Moyenne | Encaissement impossible | Message clair, encaissement manuel, pas de suspension pendant une panne avérée |
