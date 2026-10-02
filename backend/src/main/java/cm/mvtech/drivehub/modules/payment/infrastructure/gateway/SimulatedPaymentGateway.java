package cm.mvtech.drivehub.modules.payment.infrastructure.gateway;

import cm.mvtech.drivehub.modules.enums.PaymentProvider;
import cm.mvtech.drivehub.modules.payment.domain.gateway.GatewayRequest;
import cm.mvtech.drivehub.modules.payment.domain.gateway.GatewayResult;
import cm.mvtech.drivehub.modules.payment.domain.gateway.PaymentGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Passerelle SIMULÉE, pour développer sans compte Campay. Elle reproduit le comportement asynchrone :
 * l'initiation renvoie PENDING, la vérification donne le résultat.
 * Un numéro qui se termine par {@code 0000} simule un refus (solde insuffisant).
 */
@Slf4j
@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    private static final String REFERENCE_PREFIX = "SIM-";

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.SIMULATED;
    }

    @Override
    public GatewayResult initiate(GatewayRequest request) {
        log.info("[SIMULATION] {} XAF depuis {} ({})", request.amount(), request.phoneNumber(), request.internalReference());
        // Le numéro est conservé dans la référence pour décider du résultat lors de la vérification.
        String reference = REFERENCE_PREFIX + request.phoneNumber().replaceAll("\\D", "") + "-" + System.nanoTime();
        return new GatewayResult(GatewayResult.Status.PENDING, reference, "Validez le paiement sur votre téléphone (simulation)");
    }

    @Override
    public GatewayResult checkStatus(String externalReference) {
        String digits = externalReference.substring(REFERENCE_PREFIX.length(), externalReference.lastIndexOf('-'));
        return digits.endsWith("0000")
                ? new GatewayResult(GatewayResult.Status.FAILED, externalReference, "Solde insuffisant (simulation)")
                : new GatewayResult(GatewayResult.Status.SUCCESSFUL, externalReference, null);
    }
}
