# Emails : ne pas finir dans les spams

Le code fait déjà sa part (voir plus bas). **L'essentiel se règle une seule fois, dans le DNS de votre nom de
domaine** : sans SPF, DKIM et DMARC, Gmail et Outlook classent les emails en spam (ou les refusent), quelle
que soit leur qualité.

## 1. Ne pas envoyer depuis une adresse Gmail personnelle en production

Gmail convient pour développer, mais en production :

- un expéditeur `@gmail.com` envoyé par une application est jugé suspect ;
- Gmail limite à environ 500 envois par jour et peut bloquer le compte ;
- vous ne pouvez pas signer (DKIM) avec votre domaine.

Utilisez un **service d'emails transactionnels** avec votre domaine (ex. `drivehub.cm`) :

| Service | Offre gratuite (indicative) | Remarque |
|---------|-----------------------------|----------|
| [Brevo](https://www.brevo.com) | 300 emails / jour | Société européenne, interface en français, SMTP simple |
| [Resend](https://resend.com) | 3 000 emails / mois | Très simple, SMTP disponible |
| [Mailjet](https://www.mailjet.com) | 200 emails / jour | Interface en français |
| Amazon SES | Très bon marché | Plus technique à configurer |

DriveHub parle SMTP : changer de fournisseur = changer 5 variables d'environnement, aucun code.

## 2. Réglages (exemple avec Brevo)

1. Créez un compte Brevo, menu **Expéditeurs, domaines et IP dédiées → Domaines → Ajouter un domaine** :
   `drivehub.cm`.
2. Brevo affiche des enregistrements DNS à ajouter chez votre registraire (là où le domaine a été acheté) :
   - **code Brevo** (enregistrement TXT de vérification) ;
   - **DKIM** (enregistrement TXT ou CNAME) : signature cryptographique de chaque email ;
   - **SPF** (enregistrement TXT sur `drivehub.cm`) : la liste des serveurs autorisés à envoyer pour vous.
     Il ne doit exister **qu'un seul** enregistrement SPF : s'il y en a déjà un (Google Workspace, Zoho...),
     ajoutez-y Brevo, par exemple `v=spf1 include:spf.brevo.com include:_spf.google.com ~all` ;
   - **DMARC** (enregistrement TXT sur `_dmarc.drivehub.cm`) : commencez par
     `v=DMARC1; p=none; rua=mailto:dmarc@drivehub.cm`, puis passez à `p=quarantine` après quelques semaines
     sans problème.
3. Cliquez sur **Vérifier** dans Brevo (la propagation DNS prend de quelques minutes à 24 h).
4. Créez l'expéditeur `no-reply@drivehub.cm` (nom : DriveHub).
5. Menu **SMTP et API → SMTP** : notez le serveur, le port, l'identifiant et générez une clé SMTP.
6. Variables d'environnement du backend :

```properties
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=587
MAIL_USERNAME=identifiant-smtp-brevo
MAIL_PASSWORD=cle-smtp-brevo
MAIL_FROM=no-reply@drivehub.cm
MAIL_FROM_NAME=DriveHub
MAIL_REPLY_TO=contact@drivehub.cm     # boîte réellement lue par l'équipe
SUPPORT_EMAIL=support@drivehub.cm
APP_FRONTEND_URL=https://app.drivehub.cm
```

## 3. Vérifier

- Envoyez un email (inscription de test) à l'adresse donnée par [mail-tester.com](https://www.mail-tester.com) :
  visez **9/10 ou plus**. Le site dit précisément ce qui manque (SPF, DKIM, DMARC, contenu).
- Dans Gmail : ouvrez l'email, menu **⋮ → Afficher l'original** : `SPF: PASS`, `DKIM: PASS`, `DMARC: PASS`.

## 4. Ce que fait déjà le code (EmailService)

| Mesure | Pourquoi |
|--------|----------|
| Version **texte** en plus de la version HTML | Un email sans version texte est un signal de spam |
| Un seul modèle soigné, logo **joint** (pas d'image externe) | Pas d'image bloquée, pas de lien de tracking |
| Objets sans emoji ni MAJUSCULES, un seul bouton | Critères de contenu des filtres |
| Lien en clair sous le bouton, pied de page (qui, pourquoi, contact) | Transparence attendue par les filtres et les lecteurs |
| `Message-ID` au nom de votre domaine | Évite un identifiant au nom de la machine du serveur |
| `Reply-To` vers une vraie boîte | Les réponses arrivent à l'équipe |
| En-tête `List-Unsubscribe` sur les annonces du back-office | Exigé par Gmail et Yahoo pour les envois groupés |
| Envois un par un, personnalisés « Bonjour {prénom} » | Pas de longue liste de destinataires en copie |

## 5. Bonnes pratiques au quotidien

- **Montée en charge progressive** : un domaine neuf qui envoie 2 000 emails le premier jour est suspect.
  Commencez petit (quelques dizaines par jour), augmentez sur 2 à 4 semaines.
- Annonces du back-office : seulement à des personnes qui utilisent DriveHub, avec un contenu utile.
- Retirez les adresses en erreur (Brevo les liste dans **Statistiques**).
- Demandez aux premiers utilisateurs d'**ajouter l'expéditeur à leurs contacts** et, si l'email est arrivé dans
  les spams, de cliquer sur « Non spam » : cela améliore la réputation du domaine. L'application le leur
  rappelle déjà sous chaque message « un email vous a été envoyé ».
