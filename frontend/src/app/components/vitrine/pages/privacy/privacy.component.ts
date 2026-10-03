import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/**
 * Politique de confidentialité (/confidentialite).
 *
 * Le texte est aussi disponible dans docs/POLITIQUE-CONFIDENTIALITE.md (à faire relire par un juriste).
 * À chaque modification importante : changer la date ci-dessous ET PrivacyPolicy.CURRENT_VERSION
 * côté backend, pour que la version acceptée par chaque utilisateur soit tracée.
 */
@Component({
  selector: 'app-privacy',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <article class="pt-32 pb-24 bg-white dark:bg-black">
      <div class="container mx-auto px-6 max-w-3xl">
        <h2 class="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-4">Vos données</h2>
        <h1 class="font-display text-3xl md:text-5xl font-black text-black dark:text-white tracking-tight leading-[1.1] mb-4">
          Politique de confidentialité
        </h1>
        <p class="text-sm text-black/50 dark:text-white/50 mb-12">Version du {{ version }} · Loi n° 2024/017 du 23 décembre 2024 relative à la protection des données à caractère personnel au Cameroun</p>

        <div class="space-y-10 text-black/70 dark:text-white/70 leading-relaxed">
          <section>
            <p>
              DriveHub est une plateforme de gestion d'auto-écoles. Pour fonctionner, elle a besoin de certaines
              informations sur vous. Cette page explique, simplement, quelles données nous collectons, pourquoi,
              qui peut les voir, combien de temps nous les gardons et quels sont vos droits.
            </p>
            <p class="mt-4 font-semibold text-black dark:text-white">
              Vos données ne sont utilisées que dans un cadre professionnel : vérifier votre identité, vérifier la
              validité de votre auto-école si vous en êtes responsable, et faire fonctionner le service. Elles ne sont
              ni vendues, ni louées, ni utilisées à des fins publicitaires.
            </p>
          </section>

          <section>
            <h3 class="text-xl font-bold text-black dark:text-white mb-3">1. Qui est responsable de vos données ?</h3>
            <p>
              Le responsable du traitement est l'éditeur de DriveHub, MV-Tech
              (<a href="mailto:contact@drivehub.cm" class="text-[#0070f3] font-semibold">contact&#64;drivehub.cm</a>).
              Pour toute question sur vos données, écrivez à cette adresse.
            </p>
          </section>

          <section>
            <h3 class="text-xl font-bold text-black dark:text-white mb-3">2. Quelles données collectons-nous ?</h3>
            <ul class="list-disc pl-5 space-y-2">
              <li><strong class="text-black dark:text-white">Identité et contact</strong> (tous les comptes) : prénom, nom, adresse email, téléphone, genre, date de naissance, nationalité, ville de résidence.</li>
              <li><strong class="text-black dark:text-white">Compte</strong> : mot de passe (enregistré uniquement sous forme chiffrée, nous ne pouvons pas le lire), rôle (élève ou moniteur), statut du compte, date et version de la politique de confidentialité acceptée.</li>
              <li><strong class="text-black dark:text-white">Auto-école</strong> (responsables) : nom, adresse, téléphone, WhatsApp, email, site web, présentation, et les justificatifs demandés pour vérifier que l'établissement est autorisé à exercer.</li>
              <li><strong class="text-black dark:text-white">Formation</strong> (élèves) : catégorie de permis, réservations de leçons, inscriptions aux examens, cours consultés. Aucun justificatif n'est demandé aux élèves.</li>
              <li><strong class="text-black dark:text-white">Justificatifs</strong> (moniteurs uniquement) : pièce d'identité (CNI ou passeport) et CAPEC (certificat d'aptitude à l'enseignement de la conduite). Ils ne sont demandés qu'au moment utile : avant la demande de création d'une auto-école ou la demande d'adhésion à une auto-école. Les élèves ne fournissent aucun justificatif.</li>
              <li><strong class="text-black dark:text-white">Paiements</strong> : montant, motif, moyen de paiement, numéro Mobile Money utilisé et référence de la transaction. Nous ne recevons jamais votre code secret Mobile Money.</li>
              <li><strong class="text-black dark:text-white">Données techniques</strong> : jeton de connexion et préférences (thème, langue) enregistrés dans votre navigateur, journaux techniques du serveur (date, adresse appelée) pour la sécurité.</li>
            </ul>
          </section>

          <section>
            <h3 class="text-xl font-bold text-black dark:text-white mb-3">3. Pourquoi les utilisons-nous ?</h3>
            <ul class="list-disc pl-5 space-y-2">
              <li><strong class="text-black dark:text-white">Vérifier votre identité</strong> et la validité de votre adresse email.</li>
              <li><strong class="text-black dark:text-white">Vérifier la validité de l'auto-école</strong> avant son ouverture sur DriveHub, afin d'écarter les établissements non autorisés.</li>
              <li><strong class="text-black dark:text-white">Faire fonctionner le service</strong> : inscription dans une auto-école, réservations, cours, examens, paiements.</li>
              <li><strong class="text-black dark:text-white">Vous informer</strong> par email sur votre compte et le service (validation, demandes d'adhésion, informations importantes).</li>
              <li><strong class="text-black dark:text-white">Assurer la sécurité</strong> de la plateforme et respecter nos obligations légales.</li>
            </ul>
          </section>

          <section>
            <h3 class="text-xl font-bold text-black dark:text-white mb-3">4. Sur quelle base ?</h3>
            <p>
              Votre <strong class="text-black dark:text-white">consentement</strong>, donné en cochant la case prévue lors de votre inscription,
              libre, spécifique et éclairé. Certaines données sont aussi nécessaires pour fournir le service que vous demandez
              ou pour respecter une obligation légale (par exemple la conservation des traces de paiement).
            </p>
          </section>

          <section>
            <h3 class="text-xl font-bold text-black dark:text-white mb-3">5. Qui peut voir vos données ?</h3>
            <ul class="list-disc pl-5 space-y-2">
              <li>L'équipe DriveHub habilitée, uniquement pour vérifier les comptes et les auto-écoles et assurer le support.</li>
              <li>L'auto-école à laquelle vous êtes inscrit, pour les seules données utiles à votre formation. Les données d'une auto-école sont séparées de celles des autres : une auto-école ne voit jamais les élèves d'une autre.</li>
              <li>Nos prestataires techniques, dans la limite de leur mission : hébergement du site et de la base de données, envoi des emails, paiement Mobile Money (Campay et les opérateurs MTN et Orange).</li>
              <li>Les autorités, uniquement sur demande prévue par la loi.</li>
            </ul>
            <p class="mt-4">
              Certains prestataires d'hébergement peuvent stocker des données hors du Cameroun. Ces transferts se font
              dans le respect des conditions prévues par la loi n° 2024/017.
            </p>
          </section>

          <section>
            <h3 class="text-xl font-bold text-black dark:text-white mb-3">6. Combien de temps les gardons-nous ?</h3>
            <ul class="list-disc pl-5 space-y-2">
              <li>Compte et données de formation : tant que votre compte est actif, puis 3 ans après votre dernière activité.</li>
              <li>Demande d'auto-école refusée : 1 an après le refus.</li>
              <li>Traces de paiement : 10 ans, durée imposée par les obligations comptables.</li>
              <li>Jetons de connexion : jusqu'à leur expiration ou votre déconnexion.</li>
            </ul>
          </section>

          <section>
            <h3 class="text-xl font-bold text-black dark:text-white mb-3">7. Comment les protégeons-nous ?</h3>
            <p>
              Connexion chiffrée (HTTPS), mots de passe chiffrés, séparation des données de chaque auto-école dans un
              espace distinct, accès réservé aux personnes habilitées, déconnexion qui invalide réellement votre jeton.
            </p>
            <p class="mt-4">
              Les justificatifs des moniteurs (CNI, CAPEC) sont chiffrés (AES-256) avant d'être stockés : même en cas de fuite
              du stockage, ils resteraient illisibles. Ils ne sont jamais publics et ne
              sont visibles que par les personnes qui doivent les vérifier (l'équipe DriveHub pour une nouvelle auto-école,
              le responsable de l'auto-école pour la demande d'adhésion d'un moniteur). Chaque consultation est enregistrée.
            </p>
          </section>

          <section>
            <h3 class="text-xl font-bold text-black dark:text-white mb-3">8. Vos droits</h3>
            <p>Vous pouvez à tout moment :</p>
            <ul class="list-disc pl-5 space-y-2 mt-2">
              <li>accéder à vos données et en obtenir une copie ;</li>
              <li>les faire corriger si elles sont inexactes ;</li>
              <li>demander leur effacement ou la limitation de leur utilisation ;</li>
              <li>vous opposer à leur traitement ou retirer votre consentement (le service ne pourra alors plus vous être fourni) ;</li>
              <li>demander leur portabilité.</li>
            </ul>
            <p class="mt-4">
              Pour exercer ces droits, écrivez à <a href="mailto:contact@drivehub.cm" class="text-[#0070f3] font-semibold">contact&#64;drivehub.cm</a>
              depuis l'adresse email de votre compte. Si vous estimez que vos droits ne sont pas respectés, vous pouvez saisir
              l'autorité de protection des données à caractère personnel instituée par la loi n° 2024/017.
            </p>
          </section>

          <section>
            <h3 class="text-xl font-bold text-black dark:text-white mb-3">9. Mineurs</h3>
            <p>
              Si vous avez moins de 18 ans, votre inscription doit se faire avec l'accord de votre parent ou tuteur légal.
            </p>
          </section>

          <section>
            <h3 class="text-xl font-bold text-black dark:text-white mb-3">10. Modifications</h3>
            <p>
              Nous pouvons faire évoluer cette politique. En cas de changement important, la nouvelle version vous sera
              présentée et votre accord vous sera de nouveau demandé.
            </p>
          </section>
        </div>

        <div class="mt-16">
          <a routerLink="/inscription" class="btn-primary">Créer un compte</a>
        </div>
      </div>
    </article>
  `,
})
export class PrivacyComponent {
  /** Même valeur que PrivacyPolicy.CURRENT_VERSION (backend). */
  protected readonly version = '2 octobre 2026';
}
