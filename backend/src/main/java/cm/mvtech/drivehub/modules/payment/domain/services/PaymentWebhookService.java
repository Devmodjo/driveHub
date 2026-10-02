package cm.mvtech.drivehub.modules.payment.domain.services;

import cm.mvtech.drivehub.core.domain.service.TenantService;
import cm.mvtech.drivehub.core.infrastructure.TenantExecutor;
import cm.mvtech.drivehub.modules.payment.domain.gateway.GatewayResult;
import cm.mvtech.drivehub.modules.payment.infrastructure.gateway.CampayPaymentGateway;
import cm.mvtech.drivehub.modules.payment.infrastructure.gateway.PaymentProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Traitement des notifications Campay (webhook).
 *
 * <p>Le webhook n'a ni JWT ni en-tête X-Tenant-ID : l'auto-école est retrouvée grâce à la référence
 * interne "schema:idPaiement" transmise à Campay lors de l'initiation (external_reference).</p>
 *
 * <p>Sécurité : Campay signe chaque notification (paramètre {@code signature}, un JWT HS256 signé avec la
 * "App Webhook Key"). Sans clé configurée, les notifications sont ignorées : le statut est alors obtenu
 * par le bouton "Vérifier" (GET /transaction/{reference}/).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookService {

    private final PaymentService paymentService;
    private final TenantExecutor tenantExecutor;
    private final TenantService tenantService;
    private final PaymentProperties properties;

    public void handleCampayNotification(String status, String reference, String externalReference, String signature) {
        if (!isSignatureValid(signature)) {
            log.warn("Webhook Campay ignoré : signature absente ou invalide (référence {})", reference);
            return;
        }
        if (externalReference == null || !externalReference.contains(":")) {
            log.warn("Webhook Campay ignoré : external_reference inattendue ({})", externalReference);
            return;
        }
        String schema = externalReference.substring(0, externalReference.indexOf(':'));
        UUID paymentId;
        try {
            paymentId = UUID.fromString(externalReference.substring(externalReference.indexOf(':') + 1));
        } catch (IllegalArgumentException e) {
            log.warn("Webhook Campay ignoré : identifiant de paiement invalide ({})", externalReference);
            return;
        }
        if (!tenantService.isValidTenant(schema)) {
            log.warn("Webhook Campay ignoré : auto-école inconnue ou inactive ({})", schema);
            return;
        }
        GatewayResult result = CampayPaymentGateway.toResult(reference, status);
        tenantExecutor.runInTenant(schema, () -> paymentService.applyGatewayNotification(paymentId, reference, result));
    }

    private boolean isSignatureValid(String signature) {
        String key = properties.campay() == null ? null : properties.campay().webhookKey();
        if (key == null || key.isBlank() || signature == null || signature.isBlank()) {
            return false;
        }
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(signature);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
