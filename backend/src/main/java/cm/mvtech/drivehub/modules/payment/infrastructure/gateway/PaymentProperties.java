package cm.mvtech.drivehub.modules.payment.infrastructure.gateway;

import cm.mvtech.drivehub.modules.enums.PaymentProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Section {@code payment:} de application.yaml.
 *
 * @param provider passerelle active : SIMULATED (développement) ou CAMPAY
 */
@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(PaymentProvider provider, Campay campay) {

    /**
     * @param baseUrl    https://demo.campay.net/api (tests) ou https://www.campay.net/api (production)
     * @param webhookKey "App Webhook Key" du tableau de bord Campay : sert à vérifier la signature des notifications
     */
    public record Campay(String baseUrl, String username, String password, String webhookKey) {
    }
}
