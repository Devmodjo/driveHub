package cm.mvtech.drivehub.platform.mailing.application.controller;

import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.platform.mailing.application.dto.AudienceCountResponse;
import cm.mvtech.drivehub.platform.mailing.application.dto.EmailCampaignResponse;
import cm.mvtech.drivehub.platform.mailing.application.dto.RecipientSuggestion;
import cm.mvtech.drivehub.platform.mailing.application.dto.SendPlatformEmailRequest;
import cm.mvtech.drivehub.platform.mailing.application.dto.SendPlatformEmailResponse;
import cm.mvtech.drivehub.platform.mailing.domain.model.EmailAudience;
import cm.mvtech.drivehub.platform.mailing.domain.services.PlatformEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Emails envoyés depuis le back-office aux auto-écoles, moniteurs et élèves.
 * Réservé aux administrateurs ROOT et SUPER_ADMIN (un REVIEWER reçoit 403).
 */
@RestController
@RequestMapping("/api/platform/emails")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROOT') || hasRole('SUPER_ADMIN')")
@Tag(name = "PLATFORM EMAILS", description = "Envoi d'emails depuis le back-office (groupe ou individuel)")
public class PlatformEmailController {

    private final PlatformEmailService platformEmailService;

    @Operation(summary = "Envoyer un email",
            description = "audience : ALL_SCHOOLS, ALL_MONITORS, ALL_STUDENTS, SCHOOL_MEMBERS (schoolId obligatoire) "
                    + "ou INDIVIDUAL (recipients obligatoire, 1 à 50 adresses). L'envoi se fait en arrière-plan.")
    @PostMapping
    public ResponseEntity<SendPlatformEmailResponse> send(@Valid @RequestBody SendPlatformEmailRequest request,
                                                          Authentication authentication) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(platformEmailService.send(request, authentication.getName()));
    }

    @Operation(summary = "Nombre de destinataires d'une audience (aperçu avant l'envoi)")
    @GetMapping("/audience-count")
    public AudienceCountResponse audienceCount(@RequestParam EmailAudience audience,
                                               @RequestParam(required = false) UUID schoolId) {
        return new AudienceCountResponse(platformEmailService.countRecipients(audience, schoolId));
    }

    @Operation(summary = "Rechercher des destinataires", description = "Par nom ou email, 2 caractères minimum, 20 résultats au plus")
    @GetMapping("/recipients")
    public List<RecipientSuggestion> recipients(@RequestParam("q") String query) {
        return platformEmailService.searchRecipients(query);
    }

    @Operation(summary = "Historique des envois", description = "Du plus récent au plus ancien")
    @GetMapping
    public ApiPageResponse<EmailCampaignResponse> history(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        return platformEmailService.history(page, size);
    }
}
