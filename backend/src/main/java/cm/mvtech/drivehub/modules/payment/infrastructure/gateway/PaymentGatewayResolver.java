package cm.mvtech.drivehub.modules.payment.infrastructure.gateway;

import cm.mvtech.drivehub.modules.enums.PaymentProvider;
import cm.mvtech.drivehub.modules.payment.domain.gateway.PaymentGateway;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Choisit la passerelle. Spring injecte toutes les implémentations de {@link PaymentGateway} :
 * en ajouter une nouvelle ne modifie pas cette classe.
 */
@Component
public class PaymentGatewayResolver {

    private final Map<PaymentProvider, PaymentGateway> gateways = new EnumMap<>(PaymentProvider.class);
    private final PaymentProvider active;

    public PaymentGatewayResolver(List<PaymentGateway> available, PaymentProperties properties) {
        available.forEach(gateway -> gateways.put(gateway.provider(), gateway));
        this.active = properties.provider() == null ? PaymentProvider.SIMULATED : properties.provider();
        if (!gateways.containsKey(active)) {
            throw new IllegalStateException("Aucune passerelle de paiement pour " + active);
        }
    }

    /** Passerelle des NOUVEAUX paiements (payment.provider). */
    public PaymentGateway active() {
        return gateways.get(active);
    }

    /** Passerelle qui a traité un paiement existant (pour vérifier son statut). */
    public PaymentGateway get(PaymentProvider provider) {
        PaymentGateway gateway = gateways.get(provider);
        if (gateway == null) {
            throw new IllegalStateException("Passerelle inconnue : " + provider);
        }
        return gateway;
    }
}
