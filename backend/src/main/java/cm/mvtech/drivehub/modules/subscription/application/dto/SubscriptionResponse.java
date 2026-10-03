package cm.mvtech.drivehub.modules.subscription.application.dto;

import cm.mvtech.drivehub.modules.subscription.domain.model.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abonnement d'une auto-école.
 *
 * @param trialDaysLeft  jours d'essai restants (0 une fois l'essai terminé)
 * @param billingEnabled faux : la facturation n'est pas encore active, rien n'est facturé ni bloqué
 */
public record SubscriptionResponse(
        UUID id,
        UUID registryId,
        String schoolName,
        String planCode,
        String planName,
        BigDecimal monthlyPriceFcfa,
        SubscriptionStatus status,
        LocalDateTime trialStartedAt,
        LocalDateTime trialEndsAt,
        long trialDaysLeft,
        boolean billingEnabled
) {
}
