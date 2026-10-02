package cm.mvtech.drivehub.modules.payment.domain.services;

import cm.mvtech.drivehub.core.domain.service.TenantService;
import cm.mvtech.drivehub.core.infrastructure.TenantExecutor;
import cm.mvtech.drivehub.modules.enums.PaymentProvider;
import cm.mvtech.drivehub.modules.payment.domain.gateway.GatewayResult;
import cm.mvtech.drivehub.modules.payment.infrastructure.gateway.PaymentProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de {@link PaymentWebhookService} (notifications Campay).
 *
 * <p>Le webhook est une route PUBLIQUE : n'importe qui peut l'appeler. La seule protection est la
 * signature (un JWT HS256 signé avec la « App Webhook Key » de Campay). Ces tests vérifient qu'une
 * notification sans signature valide n'a AUCUN effet, et qu'une notification valide est appliquée
 * dans le schéma de la bonne auto-école.</p>
 *
 * <p>Complète {@code PaymentGatewaysTest#webhookIsAppliedOnlyWithValidSignature} avec les cas limites.</p>
 */
@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceTest {

    private static final String WEBHOOK_KEY = "cle-webhook-campay-de-test-32-caracteres-min";
    private static final String TENANT = "ae_ecole_webhook_abc123";

    @Mock private PaymentService paymentService;
    @Mock private TenantExecutor tenantExecutor;
    @Mock private TenantService tenantService;

    private PaymentWebhookService webhook;
    private UUID paymentId;

    @BeforeEach
    void setUp() {
        webhook = newWebhook(WEBHOOK_KEY);
        paymentId = UUID.randomUUID();
    }

    private PaymentWebhookService newWebhook(String key) {
        return new PaymentWebhookService(paymentService, tenantExecutor, tenantService,
                new PaymentProperties(PaymentProvider.CAMPAY,
                        new PaymentProperties.Campay("https://demo.campay.net/api", "u", "p", key)));
    }

    /** Signature telle que Campay la produit : JWT HS256 signé avec la clé donnée. */
    private static String sign(String key, Date expiration) {
        var builder = Jwts.builder().setSubject("campay");
        if (expiration != null) {
            builder.setExpiration(expiration);
        }
        return builder.signWith(Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Le TenantExecutor mocké exécute simplement le code reçu (sans changer de schéma). */
    private void givenTenantExecutorRunsWork() {
        doAnswer(inv -> {
            inv.<Runnable>getArgument(1).run();
            return null;
        }).when(tenantExecutor).runInTenant(anyString(), any(Runnable.class));
    }

    // ─── cas nominal ──────────────────────────────────────────────────────────

    /** Signature valide : la notification est appliquée DANS le schéma indiqué par la référence. */
    @Test
    void validSignature_ShouldApplyNotificationInTenant() {
        when(tenantService.isValidTenant(TENANT)).thenReturn(true);
        givenTenantExecutorRunsWork();

        webhook.handleCampayNotification("SUCCESSFUL", "ref-1", TENANT + ":" + paymentId, sign(WEBHOOK_KEY, null));

        verify(tenantExecutor).runInTenant(eq(TENANT), any(Runnable.class));
        ArgumentCaptor<GatewayResult> captor = ArgumentCaptor.forClass(GatewayResult.class);
        verify(paymentService).applyGatewayNotification(eq(paymentId), eq("ref-1"), captor.capture());
        assertEquals(GatewayResult.Status.SUCCESSFUL, captor.getValue().status());
    }

    /** Statut FAILED transmis tel quel (le paiement passera REJECTED). */
    @Test
    void validSignature_FailedStatus_ShouldTransmitFailedResult() {
        when(tenantService.isValidTenant(TENANT)).thenReturn(true);
        givenTenantExecutorRunsWork();

        webhook.handleCampayNotification("FAILED", "ref-2", TENANT + ":" + paymentId, sign(WEBHOOK_KEY, null));

        ArgumentCaptor<GatewayResult> captor = ArgumentCaptor.forClass(GatewayResult.class);
        verify(paymentService).applyGatewayNotification(eq(paymentId), eq("ref-2"), captor.capture());
        assertEquals(GatewayResult.Status.FAILED, captor.getValue().status());
    }

    // ─── signature invalide ───────────────────────────────────────────────────

    @Test
    void missingSignature_ShouldBeIgnored() {
        webhook.handleCampayNotification("SUCCESSFUL", "ref-1", TENANT + ":" + paymentId, null);
        webhook.handleCampayNotification("SUCCESSFUL", "ref-1", TENANT + ":" + paymentId, "  ");

        verifyNoInteractions(paymentService, tenantExecutor, tenantService);
    }

    /** Signature produite avec une AUTRE clé (tentative de falsification) : ignorée. */
    @Test
    void signatureWithWrongKey_ShouldBeIgnored() {
        String forged = sign("une-autre-cle-secrete-de-32-caracteres-minimum", null);

        webhook.handleCampayNotification("SUCCESSFUL", "ref-1", TENANT + ":" + paymentId, forged);

        verifyNoInteractions(paymentService, tenantExecutor, tenantService);
    }

    /** Signature expirée : ignorée (un attaquant ne peut pas rejouer une ancienne notification). */
    @Test
    void expiredSignature_ShouldBeIgnored() {
        String expired = sign(WEBHOOK_KEY, new Date(System.currentTimeMillis() - 60_000));

        webhook.handleCampayNotification("SUCCESSFUL", "ref-1", TENANT + ":" + paymentId, expired);

        verifyNoInteractions(paymentService, tenantExecutor, tenantService);
    }

    /** Aucune clé webhook configurée : toutes les notifications sont ignorées. */
    @Test
    void noWebhookKeyConfigured_ShouldIgnoreEverything() {
        PaymentWebhookService withoutKey = newWebhook("");

        withoutKey.handleCampayNotification("SUCCESSFUL", "ref-1", TENANT + ":" + paymentId, sign(WEBHOOK_KEY, null));

        verifyNoInteractions(paymentService, tenantExecutor, tenantService);
    }

    // ─── référence externe invalide ───────────────────────────────────────────

    @Test
    void externalReferenceWithoutSeparator_ShouldBeIgnored() {
        webhook.handleCampayNotification("SUCCESSFUL", "ref-1", "pas-de-separateur", sign(WEBHOOK_KEY, null));

        verifyNoInteractions(paymentService, tenantExecutor, tenantService);
    }

    @Test
    void externalReferenceWithInvalidPaymentId_ShouldBeIgnored() {
        webhook.handleCampayNotification("SUCCESSFUL", "ref-1", TENANT + ":pas-un-uuid", sign(WEBHOOK_KEY, null));

        verifyNoInteractions(paymentService, tenantExecutor, tenantService);
    }

    /** Auto-école inconnue ou désactivée : rien n'est exécuté dans un schéma. */
    @Test
    void unknownTenant_ShouldBeIgnored() {
        when(tenantService.isValidTenant(TENANT)).thenReturn(false);

        webhook.handleCampayNotification("SUCCESSFUL", "ref-1", TENANT + ":" + paymentId, sign(WEBHOOK_KEY, null));

        verifyNoInteractions(paymentService, tenantExecutor);
    }
}
