# Justificatifs d'identité et de qualification — API

Pourquoi : n'ouvrir DriveHub qu'à des auto-écoles et des moniteurs réels (lutte contre les auto-écoles
clandestines), sans alourdir l'inscription. Les pièces sont demandées **au moment où elles servent**, pas à
l'inscription.

| Qui | Pièces demandées | Quand | Qui les vérifie |
|-----|------------------|-------|-----------------|
| Moniteur fondateur (dirigeant) | Pièce d'identité (`CNI`) + CAPEC (`CAPEC`) | Avant d'envoyer la demande de création d'auto-école | L'équipe DriveHub (back-office) |
| Moniteur qui rejoint une auto-école | `CNI` + `CAPEC` | Avant d'envoyer la demande d'adhésion | Le responsable de l'auto-école |
| Moniteur ajouté par le responsable | `CNI` + `CAPEC` | Envoyées par le responsable dans le formulaire d'ajout | Le responsable (pièces considérées comme vérifiées) |
| Élève | `CNI` | Avant d'envoyer la demande d'adhésion | Le responsable de l'auto-école |

## Sécurité (résumé — détail dans le README, section « Justificatifs »)

- Fichiers acceptés : PDF, JPEG, PNG, WebP ; **5 Mo maximum** ; le type réel est vérifié sur le contenu du fichier
  (signature binaire), pas seulement sur l'extension.
- Chaque fichier est **chiffré (AES-256-GCM) avant d'être envoyé** au stockage (Cloudflare R2 en production,
  dossier local en développement), avec une clé propre au fichier, elle-même chiffrée par la clé maître
  (`DATA_ENCRYPTION_KEY`). Une fuite du bucket ne révèle donc rien.
- En base, le numéro du document et le nom du fichier sont chiffrés. Une empreinte (HMAC) du numéro permet de
  repérer un même numéro utilisé par deux comptes, sans jamais le déchiffrer.
- Les fichiers ne sont jamais publics : ils passent par l'API, qui vérifie les droits et **journalise chaque
  consultation** (qui, quand, depuis quel écran).

## Objet `DocumentResponse`

```json
{
  "id": "uuid",
  "type": "CNI | CAPEC",
  "status": "PENDING | VERIFIED | REJECTED",
  "documentNumberMasked": "••••4521",        // null si non renseigné
  "fileName": "cni-recto.jpg",
  "contentType": "image/jpeg",
  "sizeBytes": 245112,
  "uploadedAt": "2026-10-02T10:15:00",
  "reviewComment": "Photo illisible, merci d'en envoyer une nouvelle"   // null sauf refus
}
```

## Utilisateur connecté (moniteur ou élève, jeton utilisateur, sans `X-Tenant-ID`)

| Méthode | Route | Description |
|---------|-------|-------------|
| `GET` | `/api/documents/me` | Mes justificatifs → `DocumentResponse[]` |
| `GET` | `/api/documents/requirements` | `{ "required": ["CNI","CAPEC"], "missing": ["CAPEC"] }` selon mon rôle |
| `POST` | `/api/documents` | `multipart/form-data` : `type` (`CNI`/`CAPEC`), `file`, `documentNumber` (facultatif, 50 car. max). Remplace le document existant du même type. → `201 DocumentResponse` |
| `GET` | `/api/documents/{id}/file` | Voir mon document (flux binaire, `Content-Type` d'origine) |
| `DELETE` | `/api/documents/{id}` | Supprimer mon document (impossible s'il est déjà vérifié : 409) |

Erreurs : `400` (type de fichier refusé, fichier vide ou trop lourd, avec un message précis), `401`, `404`.

Effets sur les parcours existants :
- `POST /api/driving-schools/request` répond `400` « Ajoutez votre pièce d'identité et votre CAPEC… » si un justificatif manque.
- `POST /api/join-school/public` répond `400` avec le même type de message (élève : CNI ; moniteur : CNI + CAPEC).

## Responsable d'auto-école (jeton moniteur)

| Méthode | Route | Description |
|---------|-------|-------------|
| `GET` | `/api/join-school/admin/{requestId}/documents` | Justificatifs du demandeur → `DocumentResponse[]` |
| `GET` | `/api/join-school/admin/{requestId}/documents/{documentId}/file` | Voir un justificatif du demandeur |
| `GET` | `/api/students/{studentId}/documents` (avec `X-Tenant-ID`) | Justificatifs d'un élève de l'auto-école |
| `GET` | `/api/students/{studentId}/documents/{documentId}/file` (avec `X-Tenant-ID`) | Voir un justificatif d'un élève |
| `POST` | `/api/monitors` (avec `X-Tenant-ID`) | Ajouter un moniteur à l'auto-école. `multipart/form-data` : partie `monitor` en JSON (`application/json`) `{ firstname, lastname, email, phoneNumber, gender, nationality, residenceCity, dateOfBirth }`, fichiers `cni` et `capec` (obligatoires), `cniNumber`, `capecNumber` (facultatifs). Crée le compte, le rattache à l'auto-école et envoie une **invitation** par email. → `201 MonitorResponseDto` |

À l'approbation d'une demande d'adhésion, les justificatifs du demandeur passent à `VERIFIED`.

## Invitation d'un moniteur ajouté par le responsable

| Méthode | Route | Description |
|---------|-------|-------------|
| `POST` | `/api/auth/accept-invitation` | `{ "token", "password", "acceptPrivacyPolicy": true }` : le moniteur choisit son mot de passe et accepte la politique de confidentialité (lien de l'email : `/invitation?token=...`, valable 72 heures) |

## Back-office (jeton administrateur)

| Méthode | Route | Rôles | Description |
|---------|-------|-------|-------------|
| `GET` | `/api/platform/registries/{registryId}/documents` | tous | Justificatifs du fondateur de l'auto-école → `DocumentResponse[]` |
| `GET` | `/api/platform/documents/{documentId}/file` | tous | Voir un justificatif (consultation journalisée) |
| `PATCH` | `/api/platform/documents/{documentId}/review` | tous | `{ "status": "VERIFIED" \| "REJECTED", "comment": "..." }` (commentaire obligatoire en cas de refus ; l'utilisateur reçoit un email) |
| `GET` | `/api/platform/subscriptions?page=0&size=20` | ROOT, SUPER_ADMIN | Abonnements des auto-écoles (voir docs/ABONNEMENTS-ET-PAIEMENTS.md) |

L'approbation d'une auto-école (`PATCH /api/platform/registries/{id}/approve`) passe les justificatifs du fondateur à
`VERIFIED` et démarre la période d'essai de 15 jours.

## Abonnement (jeton moniteur responsable)

| Méthode | Route | Description |
|---------|-------|-------------|
| `GET` | `/api/driving-schools/me/subscription` | `{ "planCode": "ESSENTIEL", "planName": "Essentiel", "monthlyPriceFcfa": 25000, "status": "TRIAL", "trialEndsAt": "2026-10-17T10:00:00", "trialDaysLeft": 15, "billingEnabled": false }` — `204` si l'auto-école n'est pas encore validée |
