# Politique de confidentialité — DriveHub

Version du 2 octobre 2026 — publiée sur le site à l'adresse `/confidentialite`
(`frontend/src/app/components/vitrine/pages/privacy/privacy.component.ts`).

> **À faire valider par un juriste avant la mise en ligne.** Points à compléter ou confirmer :
> - identité légale exacte du responsable du traitement (raison sociale, RCCM, NIU, adresse du siège) ;
> - formalités préalables auprès de l'autorité de protection des données prévue par la loi n° 2024/017
>   (déclaration ou autorisation avant tout traitement) ;
> - durées de conservation (section 6) ;
> - liste exacte des prestataires et pays d'hébergement (section 5).
>
> Quand le texte change de façon importante : modifier la date ici, dans le composant Angular,
> et `PrivacyPolicy.CURRENT_VERSION` dans le backend (la version acceptée est enregistrée sur chaque compte).

## Le consentement dans l'application

- À l'inscription (élève ou moniteur), la case « J'accepte la politique de confidentialité... » est obligatoire :
  le bouton reste inactif tant qu'elle n'est pas cochée, et le backend refuse toute inscription sans elle
  (`acceptPrivacyPolicy` doit valoir `true`).
- La date et la version acceptées sont enregistrées dans `_users.privacy_policy_accepted_at` et
  `_users.privacy_policy_version` (migration V12) : c'est la preuve du consentement.
- Texte de la case : « J'autorise DriveHub à collecter mes informations pour vérifier mon identité
  (et la validité de mon auto-école pour un moniteur). Elles ne sont utilisées que dans ce cadre professionnel. »

## Texte publié

1. **Responsable** : l'éditeur de DriveHub, MV-Tech — contact@drivehub.cm.
2. **Données collectées** : identité et contact ; compte (mot de passe chiffré, rôle, statut, consentement) ;
   auto-école (coordonnées, présentation, justificatifs d'autorisation) ; **moniteurs uniquement** : pièce
   d'identité et CAPEC (fichiers et numéros) ; formation (catégorie de permis, réservations, examens) ; paiements (montant, motif, moyen, numéro Mobile Money,
   référence — jamais le code secret) ; données techniques (jeton de connexion, préférences, journaux serveur).
3. **Finalités** : vérifier l'identité ; vérifier la validité de l'auto-école ; faire fonctionner le service ;
   informer par email ; sécurité et obligations légales. Aucune vente, location ni publicité.
4. **Base légale** : consentement libre, spécifique et éclairé (case à cocher) ; exécution du service ; obligations légales.
5. **Destinataires** : équipe DriveHub habilitée ; l'auto-école de l'utilisateur (données utiles uniquement,
   séparation stricte entre auto-écoles) ; prestataires (hébergement, emails, Campay / MTN / Orange) ;
   autorités sur demande légale. Hébergement possible hors du Cameroun, dans les conditions de la loi.
6. **Conservation** : compte actif + 3 ans ; demande refusée 1 an ; paiements 10 ans ; jetons jusqu'à expiration.
7. **Sécurité** : HTTPS, mots de passe chiffrés (BCrypt), un schéma de base de données par auto-école,
   accès restreints, déconnexion réelle (jetons révoqués). Justificatifs des moniteurs chiffrés (AES-256)
   avant d'être stockés (Cloudflare R2), visibles seulement par les personnes chargées de la vérification
   (équipe DriveHub, responsable de l'auto-école) ; chaque consultation est enregistrée.
8. **Droits** : accès, rectification, effacement, limitation, opposition, retrait du consentement, portabilité —
   par email à contact@drivehub.cm ; recours auprès de l'autorité de protection des données.
9. **Mineurs** : accord du parent ou tuteur légal avant 18 ans.
10. **Modifications** : nouvelle acceptation demandée en cas de changement important.

## Sources

- Loi n° 2024/017 du 23 décembre 2024 relative à la protection des données à caractère personnel au Cameroun
  (consentement préalable, libre, spécifique et éclairé ; droits d'accès, de rectification, d'effacement, de
  limitation, d'opposition et de portabilité ; autorité de contrôle ; période transitoire de 18 mois).
