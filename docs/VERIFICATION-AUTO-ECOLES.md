# Vérifier qu'une auto-école existe vraiment (Cameroun)

Objectif : n'ouvrir sur DriveHub que des auto-écoles autorisées à exercer, sans bloquer les vraies.
Ce document répond à la question « quels documents demander ? » **avant** toute implémentation.

## Ce que possède toute auto-école en règle

| Document | Délivré par | Ce qu'il prouve | Vérifiable comment |
|---|---|---|---|
| **Décision d'autorisation de création et d'ouverture** (« agrément ») | Ministre des Transports (MINT), après instruction par la délégation régionale | L'établissement a le droit d'enseigner la conduite contre rémunération | Numéro et date de la décision ; listes publiées par le MINT (ex. auto-écoles appelées à régulariser) ; appel à la délégation régionale |
| **Récépissé de dépôt du dossier d'agrément** | Délégation régionale / départementale des Transports | Demande déposée ; vaut agrément si l'administration n'a pas répondu dans les 60 jours | Date du dépôt (plus de 60 jours) |
| **Attestation d'immatriculation fiscale (NIU)** | Direction générale des impôts (DGI) | L'entreprise existe fiscalement | En ligne, sans compte, sur le service de vérification de la DGI (Harmony 2) : nom, NIU, centre des impôts |
| **Registre du commerce (RCCM)** | Greffe du tribunal / CFCE | L'entreprise est immatriculée | Numéro RCCM sur l'extrait |
| **CAPEC du responsable / des moniteurs** (Certificat d'aptitude professionnelle à l'enseignement de la conduite automobile) | MINT | La personne est habilitée à enseigner la conduite | Numéro du certificat ; recensement des moniteurs en cours au MINT |
| Pièce d'identité du fondateur / dirigeant | — | Le demandeur est bien le dirigeant | Comparaison avec le nom sur l'agrément et le NIU |

Rappels réglementaires :
- l'apprentissage payant de la conduite se fait uniquement dans des établissements agréés par le ministre des
  Transports, et il ne peut être dispensé que par des titulaires du CAPEC ;
- texte de référence cité par le MINT : décision n° 00184/MINT du 8 septembre 2015 réglementant les conditions de
  création, d'ouverture et d'exploitation des établissements d'enseignement de la conduite automobile ;
- dossier d'agrément : demande timbrée, extrait de casier judiciaire du fondateur (moins de 3 mois),
  copie certifiée de la CNI, dossiers d'au moins deux moniteurs et du directeur.

## Recommandation pour DriveHub

1. **Garder une validation humaine au lancement** (c'est ce que fait déjà le back-office) : le volume est faible,
   et c'est votre meilleure protection contre les établissements clandestins.
2. **Demander à la création de l'auto-école** (champs + fichiers) :
   - obligatoire : numéro et date de la **décision d'autorisation du MINT** (scan), **NIU** (attestation d'immatriculation),
     **CAPEC du responsable** (scan), CNI du responsable ;
   - facultatif : RCCM, photo de la façade / des locaux.
3. **Contrôles dans le back-office** (le réviseur coche chaque point) : NIU vérifié en ligne et nom identique ;
   nom du dirigeant identique sur la CNI, le NIU et l'agrément ; décision MINT lisible et cohérente
   (région, date) ; en cas de doute, appel à la délégation régionale des Transports.
4. **Plus tard (semi-automatique)** : vérification automatique du format du NIU (12 caractères), détection des
   doublons (même NIU ou même numéro d'agrément sur deux comptes), relance automatique si les pièces sont
   incomplètes. Une validation 100 % automatique n'est pas possible tant que le MINT ne publie pas de registre
   consultable en ligne.

À implémenter après votre accord : stockage des fichiers (taille max., formats PDF/JPG, accès réservé aux
réviseurs), nouveaux champs dans `driving_school_registry`, écran de vérification dans le back-office,
et mention de ces pièces dans la politique de confidentialité (déjà prévue : « justificatifs d'autorisation »).

## Sources

- MINT — « Qui peut exercer l'activité de moniteur d'auto-école ? » : http://mintransports.net/en/foire-aux-questions/qui-peut-exercer-lactivite-de-moniteur-dauto-ecole/
- MINT — CAPEC, option moniteur d'auto-école : https://mintransports.net/en/capec-option-moniteur-dauto-ecole-date-de-lexamen-et-centres-de-formation/
- MINT — Liste des auto-écoles appelées à régulariser leur situation : https://mintransports.net/en/liste-des-auto-ecoles-appelees-a-regulariser-leur-situation/
- Camerlex — La réglementation du permis de conduire et des auto-écoles : https://www.camerlex.com/la-reglementation-du-permis-de-conduire-et-des-auto-ecoles-2260/
- Vérification d'une attestation d'immatriculation (NIU) : https://lefisk.cm/outils/attestation-immatriculation
