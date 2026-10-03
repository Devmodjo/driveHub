package cm.mvtech.drivehub.modules.subscription.domain.model;

/**
 * Cycle de vie d'un abonnement (voir docs/ABONNEMENTS-ET-PAIEMENTS.md).
 * Aujourd'hui seul TRIAL est utilisé : la facturation est désactivée (billing.enabled = false).
 */
public enum SubscriptionStatus {
    /** Période d'essai gratuite (15 jours après la validation de l'auto-école). */
    TRIAL,
    /** Abonnement payé pour la période en cours. */
    ACTIVE,
    /** Facture impayée : délai de grâce avant suspension. */
    PAST_DUE,
    /** Accès restreint faute de paiement (les données sont conservées). */
    SUSPENDED,
    /** Abonnement arrêté à la demande de l'auto-école. */
    CANCELLED
}
