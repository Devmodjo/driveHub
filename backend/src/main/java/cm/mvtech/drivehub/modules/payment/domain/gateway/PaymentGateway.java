package cm.mvtech.drivehub.modules.payment.domain.gateway;

import cm.mvtech.drivehub.modules.enums.PaymentProvider;

/**
 * PATRON STRATÉGIE : contrat commun à toutes les passerelles Mobile Money.
 *
 * <p>Le {@code PaymentService} ne connaît que cette interface. Passer de la simulation à Campay,
 * ou ajouter un autre agrégateur plus tard, ne demande qu'une nouvelle implémentation et un
 * changement de {@code payment.provider} dans application.yaml.</p>
 *
 * <p>Un paiement Mobile Money est ASYNCHRONE : l'initiation renvoie PENDING (l'élève doit valider
 * sur son téléphone), le résultat final arrive par le webhook ou par {@link #checkStatus}.</p>
 */
public interface PaymentGateway {

    PaymentProvider provider();

    /** Demande le débit du compte Mobile Money. */
    GatewayResult initiate(GatewayRequest request);

    /** Interroge le prestataire sur l'état d'une transaction déjà initiée. */
    GatewayResult checkStatus(String externalReference);
}
