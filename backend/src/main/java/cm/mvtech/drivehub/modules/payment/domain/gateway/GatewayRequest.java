package cm.mvtech.drivehub.modules.payment.domain.gateway;

import java.math.BigDecimal;

/**
 * @param internalReference référence chez nous, au format "schema:idPaiement" : elle permet au
 *                          webhook (qui n'a pas d'en-tête X-Tenant-ID) de retrouver l'auto-école
 * @param phoneNumber       numéro Mobile Money à débiter
 */
public record GatewayRequest(String internalReference, BigDecimal amount, String phoneNumber, String description) {
}
