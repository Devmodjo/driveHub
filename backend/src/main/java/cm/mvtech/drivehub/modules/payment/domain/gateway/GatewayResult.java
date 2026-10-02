package cm.mvtech.drivehub.modules.payment.domain.gateway;

/**
 * Réponse normalisée d'une passerelle.
 *
 * @param status            état du paiement après l'appel
 * @param externalReference référence de la transaction chez le prestataire
 * @param message           information utile (code USSD, motif d'échec...)
 */
public record GatewayResult(Status status, String externalReference, String message) {

    public enum Status { PENDING, SUCCESSFUL, FAILED }

    public static GatewayResult failed(String message) {
        return new GatewayResult(Status.FAILED, null, message);
    }
}
