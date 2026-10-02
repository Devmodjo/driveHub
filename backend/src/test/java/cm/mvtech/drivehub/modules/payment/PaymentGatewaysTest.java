package cm.mvtech.drivehub.modules.payment;

import cm.mvtech.drivehub.core.domain.service.TenantService;
import cm.mvtech.drivehub.core.infrastructure.TenantExecutor;
import cm.mvtech.drivehub.modules.enums.PaymentProvider;
import cm.mvtech.drivehub.modules.payment.domain.gateway.GatewayRequest;
import cm.mvtech.drivehub.modules.payment.domain.gateway.GatewayResult;
import cm.mvtech.drivehub.modules.payment.domain.services.PaymentService;
import cm.mvtech.drivehub.modules.payment.domain.services.PaymentWebhookService;
import cm.mvtech.drivehub.modules.payment.infrastructure.gateway.CampayPaymentGateway;
import cm.mvtech.drivehub.modules.payment.infrastructure.gateway.PaymentGatewayResolver;
import cm.mvtech.drivehub.modules.payment.infrastructure.gateway.PaymentProperties;
import cm.mvtech.drivehub.modules.payment.infrastructure.gateway.SimulatedPaymentGateway;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Passerelles Mobile Money (patron Stratégie) et webhook Campay. */
class PaymentGatewaysTest {

    private static final String WEBHOOK_KEY = "cle-webhook-campay-de-test-32-caracteres-min";

    @Test
    void simulatedGatewayBehavesAsynchronouslyLikeCampay() {
        SimulatedPaymentGateway gateway = new SimulatedPaymentGateway();

        GatewayResult ok = gateway.initiate(new GatewayRequest("ae_x:1", new BigDecimal("5000"), "+237 677 11 22 33", "test"));
        assertEquals(GatewayResult.Status.PENDING, ok.status());
        assertEquals(GatewayResult.Status.SUCCESSFUL, gateway.checkStatus(ok.externalReference()).status());

        GatewayResult ko = gateway.initiate(new GatewayRequest("ae_x:2", new BigDecimal("5000"), "+237 699 00 0000", "test"));
        assertEquals(GatewayResult.Status.FAILED, gateway.checkStatus(ko.externalReference()).status());
    }

    @Test
    void campayStatusesAreNormalized() {
        assertEquals(GatewayResult.Status.SUCCESSFUL, CampayPaymentGateway.toResult("r", "SUCCESSFUL").status());
        assertEquals(GatewayResult.Status.FAILED, CampayPaymentGateway.toResult("r", "FAILED").status());
        assertEquals(GatewayResult.Status.PENDING, CampayPaymentGateway.toResult("r", "PENDING").status());
    }

    @Test
    void resolverFailsAtStartupWhenProviderHasNoImplementation() {
        PaymentProperties properties = new PaymentProperties(PaymentProvider.CAMPAY, null);
        assertThrows(IllegalStateException.class,
                () -> new PaymentGatewayResolver(List.of(new SimulatedPaymentGateway()), properties));
    }

    @Test
    void webhookIsAppliedOnlyWithValidSignature() {
        PaymentService paymentService = mock(PaymentService.class);
        TenantExecutor tenantExecutor = mock(TenantExecutor.class);
        TenantService tenantService = mock(TenantService.class);
        when(tenantService.isValidTenant("ae_ecole_abc123")).thenReturn(true);
        doAnswer(inv -> { inv.<Runnable>getArgument(1).run(); return null; })
                .when(tenantExecutor).runInTenant(anyString(), any(Runnable.class));
        PaymentWebhookService webhook = new PaymentWebhookService(paymentService, tenantExecutor, tenantService,
                new PaymentProperties(PaymentProvider.CAMPAY,
                        new PaymentProperties.Campay("https://demo.campay.net/api", "u", "p", WEBHOOK_KEY)));
        UUID paymentId = UUID.randomUUID();
        String externalReference = "ae_ecole_abc123:" + paymentId;

        // Signature falsifiée : aucun effet
        webhook.handleCampayNotification("SUCCESSFUL", "ref-1", externalReference, "faux");
        verifyNoInteractions(paymentService);

        // Signature valide : le paiement est mis à jour dans le schéma de l'auto-école
        String signature = Jwts.builder().setSubject("campay")
                .signWith(Keys.hmacShaKeyFor(WEBHOOK_KEY.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
        webhook.handleCampayNotification("SUCCESSFUL", "ref-1", externalReference, signature);
        verify(tenantExecutor).runInTenant(eq("ae_ecole_abc123"), any(Runnable.class));
        verify(paymentService).applyGatewayNotification(eq(paymentId), eq("ref-1"), any(GatewayResult.class));
    }
}
