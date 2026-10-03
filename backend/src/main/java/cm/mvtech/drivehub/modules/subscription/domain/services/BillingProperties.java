package cm.mvtech.drivehub.modules.subscription.domain.services;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Réglages des abonnements (application.yaml : billing.*).
 *
 * @param enabled     facturation active ? FAUX tant que tout n'est pas prêt (voir docs/ABONNEMENTS-ET-PAIEMENTS.md)
 * @param trialDays   durée de la période d'essai, en jours (15)
 * @param defaultPlan offre attribuée à la validation de l'auto-école
 */
@ConfigurationProperties(prefix = "billing")
public record BillingProperties(boolean enabled, int trialDays, String defaultPlan) {
}
