package cm.mvtech.drivehub.modules.payment.application.controller;

import cm.mvtech.drivehub.modules.payment.domain.services.PaymentWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * URL à renseigner dans le tableau de bord Campay ("Webhook URL") :
 * https://VOTRE_DOMAINE/api/webhooks/campay
 * Route publique (pas de JWT, pas de X-Tenant-ID) : la confiance repose sur la signature.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "WEBHOOKS", description = "notifications des agrégateurs de paiement")
public class PaymentWebhookController {

    private final PaymentWebhookService webhookService;

    @Operation(summary = "notification Campay de fin de transaction")
    @RequestMapping(value = "/api/webhooks/campay", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Void> campay(@RequestParam(required = false) String status,
                                       @RequestParam(required = false) String reference,
                                       @RequestParam(name = "external_reference", required = false) String externalReference,
                                       @RequestParam(required = false) String signature) {
        webhookService.handleCampayNotification(status, reference, externalReference, signature);
        return ResponseEntity.ok().build();   // toujours 200 : Campay ne doit pas réessayer en boucle
    }
}
