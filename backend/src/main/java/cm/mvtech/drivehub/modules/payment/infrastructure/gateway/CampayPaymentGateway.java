package cm.mvtech.drivehub.modules.payment.infrastructure.gateway;

import cm.mvtech.drivehub.modules.enums.PaymentProvider;
import cm.mvtech.drivehub.modules.payment.domain.gateway.GatewayRequest;
import cm.mvtech.drivehub.modules.payment.domain.gateway.GatewayResult;
import cm.mvtech.drivehub.modules.payment.domain.gateway.PaymentGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;

/**
 * Passerelle Campay (https://www.campay.net) : Mobile Money MTN et Orange au Cameroun.
 *
 * <ol>
 *   <li>{@code POST /token/} (username, password de l'application Campay) → jeton temporaire ;</li>
 *   <li>{@code POST /collect/} → l'élève reçoit une demande de validation (USSD) sur son téléphone ;</li>
 *   <li>résultat : webhook Campay (voir PaymentWebhookController) ou {@code GET /transaction/{reference}/}.</li>
 * </ol>
 * Activation : {@code PAYMENT_PROVIDER=CAMPAY} + identifiants (voir README).
 */
@Slf4j
@Component
public class CampayPaymentGateway implements PaymentGateway {

    private static final String CAMEROON_PREFIX = "237";

    private final RestClient restClient;
    private final PaymentProperties.Campay config;

    /** Jeton mis en cache jusqu'à son expiration (évite un appel /token/ à chaque paiement). */
    private String cachedToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public CampayPaymentGateway(RestClient.Builder builder, PaymentProperties properties) {
        this.config = properties.campay();
        this.restClient = builder.baseUrl(config.baseUrl()).build();
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.CAMPAY;
    }

    @Override
    public GatewayResult initiate(GatewayRequest request) {
        try {
            Map<String, Object> body = Map.of(
                    "amount", request.amount().setScale(0, RoundingMode.DOWN).toPlainString(), // le XAF n'a pas de centimes
                    "currency", "XAF",
                    "from", toCampayPhone(request.phoneNumber()),
                    "description", request.description(),
                    "external_reference", request.internalReference());

            Map<?, ?> response = restClient.post()
                    .uri("/collect/")
                    .header("Authorization", "Token " + accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            Object reference = response == null ? null : response.get("reference");
            if (reference == null) {
                return GatewayResult.failed("Réponse Campay invalide");
            }
            return new GatewayResult(GatewayResult.Status.PENDING, reference.toString(),
                    "Validez le paiement sur votre téléphone (code " + response.get("ussd_code") + ")");
        } catch (RestClientException | IllegalStateException e) {
            log.error("Échec de l'initiation Campay ({})", request.internalReference(), e);
            return GatewayResult.failed("Le service Mobile Money est momentanément indisponible");
        }
    }

    @Override
    public GatewayResult checkStatus(String externalReference) {
        try {
            Map<?, ?> response = restClient.get()
                    .uri("/transaction/{reference}/", externalReference)
                    .header("Authorization", "Token " + accessToken())
                    .retrieve()
                    .body(Map.class);
            return toResult(externalReference, response == null ? null : String.valueOf(response.get("status")));
        } catch (RestClientException | IllegalStateException e) {
            log.error("Échec de la vérification Campay ({})", externalReference, e);
            // On ne conclut pas à un échec : le paiement reste en attente, on réessaiera.
            return new GatewayResult(GatewayResult.Status.PENDING, externalReference, null);
        }
    }

    /** Statut Campay (SUCCESSFUL / FAILED / PENDING) → statut normalisé. */
    public static GatewayResult toResult(String reference, String campayStatus) {
        if ("SUCCESSFUL".equals(campayStatus)) {
            return new GatewayResult(GatewayResult.Status.SUCCESSFUL, reference, null);
        }
        if ("FAILED".equals(campayStatus)) {
            return new GatewayResult(GatewayResult.Status.FAILED, reference, "Paiement refusé ou annulé");
        }
        return new GatewayResult(GatewayResult.Status.PENDING, reference, null);
    }

    /** Jeton d'accès Campay (renouvelé une minute avant son expiration). */
    private synchronized String accessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedToken;
        }
        if (config.username() == null || config.username().isBlank()) {
            throw new IllegalStateException("Identifiants Campay absents (CAMPAY_USERNAME / CAMPAY_PASSWORD)");
        }
        Map<?, ?> response = restClient.post()
                .uri("/token/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", config.username(), "password", config.password()))
                .retrieve()
                .body(Map.class);
        if (response == null || response.get("token") == null) {
            throw new IllegalStateException("Jeton Campay absent de la réponse");
        }
        cachedToken = response.get("token").toString();
        long expiresIn = response.get("expires_in") instanceof Number n ? n.longValue() : 3600L;
        tokenExpiresAt = Instant.now().plusSeconds(Math.max(60, expiresIn - 60));
        return cachedToken;
    }

    /** "+237 6 99 00 11 22" ou "699001122" → "237699001122" (format attendu par Campay). */
    static String toCampayPhone(String phone) {
        String digits = phone.replaceAll("\\D", "");
        return digits.startsWith(CAMEROON_PREFIX) ? digits : CAMEROON_PREFIX + digits;
    }
}
