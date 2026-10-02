package cm.mvtech.drivehub.modules.subscription.application.controller;

import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.subscription.application.dto.SubscriptionResponse;
import cm.mvtech.drivehub.modules.subscription.domain.services.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Consultation des abonnements (aucune facturation : billing.enabled = false). */
@RestController
@RequiredArgsConstructor
@Tag(name = "ABONNEMENTS", description = "Période d'essai et abonnements des auto-écoles (facturation désactivée)")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    @Operation(summary = "Abonnement de mon auto-école", description = "204 si l'auto-école n'est pas encore validée")
    @GetMapping("/api/driving-schools/me/subscription")
    @PreAuthorize("hasRole('MONITOR')")
    public ResponseEntity<SubscriptionResponse> mine(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .flatMap(subscriptionService::forOwner)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Abonnements des auto-écoles (back-office)")
    @GetMapping("/api/platform/subscriptions")
    @PreAuthorize("hasRole('ROOT') || hasRole('SUPER_ADMIN')")
    public ApiPageResponse<SubscriptionResponse> all(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return subscriptionService.list(page, size);
    }
}
