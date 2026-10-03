package cm.mvtech.drivehub.modules.subscription.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.subscription.application.dto.SubscriptionResponse;
import cm.mvtech.drivehub.modules.subscription.domain.model.SchoolSubscription;
import cm.mvtech.drivehub.modules.subscription.domain.model.SubscriptionPlan;
import cm.mvtech.drivehub.modules.subscription.domain.model.SubscriptionStatus;
import cm.mvtech.drivehub.modules.subscription.infrastructure.repository.SchoolSubscriptionRepository;
import cm.mvtech.drivehub.modules.subscription.infrastructure.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Abonnements des auto-écoles — ENVIRONNEMENT PRÉPARÉ, FACTURATION DÉSACTIVÉE.
 *
 * <p>Aujourd'hui ce service fait seulement deux choses :</p>
 * <ul>
 *   <li>démarrer la période d'essai (15 jours) quand l'équipe DriveHub valide une auto-école ;</li>
 *   <li>afficher l'état de l'abonnement (auto-école, back-office).</li>
 * </ul>
 * <p>Il ne crée aucune facture, ne demande aucun paiement et ne bloque aucun accès.
 * Les étapes pour activer la facturation sont décrites dans docs/ABONNEMENTS-ET-PAIEMENTS.md.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SchoolSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final DrivingSchoolRegistryRepository registryRepository;
    private final BillingProperties billing;

    /** Démarre la période d'essai d'une auto-école qui vient d'être validée (sans effet si elle existe déjà). */
    @Transactional
    public void startTrial(DrivingSchoolRegistry registry) {
        if (subscriptionRepository.existsByRegistry(registry)) {
            return;
        }
        SubscriptionPlan plan = planRepository.findById(billing.defaultPlan())
                .orElseThrow(() -> new IllegalStateException("Offre par défaut introuvable : " + billing.defaultPlan()));
        LocalDateTime now = LocalDateTime.now();
        SchoolSubscription subscription = new SchoolSubscription();
        subscription.setRegistry(registry);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscription.setTrialStartedAt(now);
        subscription.setTrialEndsAt(now.plusDays(billing.trialDays()));
        subscription.setCreatedAt(now);
        subscription.setUpdatedAt(now);
        subscriptionRepository.save(subscription);
        log.info("Période d'essai de {} jours démarrée pour {}", billing.trialDays(), registry.getSchoolName());
    }

    /** Abonnement de l'auto-école dirigée par ce moniteur (vide si elle n'est pas encore validée). */
    @Transactional(readOnly = true)
    public Optional<SubscriptionResponse> forOwner(User owner) {
        return registryRepository.findByAdmin(owner)
                .flatMap(subscriptionRepository::findByRegistry)
                .map(this::toResponse);
    }

    /** Back-office : tous les abonnements, les essais qui se terminent le plus tôt en premier. */
    @Transactional(readOnly = true)
    public ApiPageResponse<SubscriptionResponse> list(int page, int size) {
        return ApiPageResponse.from(subscriptionRepository
                .findAllByOrderByTrialEndsAtAsc(PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)))
                .map(this::toResponse));
    }

    private SubscriptionResponse toResponse(SchoolSubscription s) {
        long daysLeft = s.getStatus() == SubscriptionStatus.TRIAL
                ? Math.max(0, (long) Math.ceil(Duration.between(LocalDateTime.now(), s.getTrialEndsAt()).toHours() / 24.0))
                : 0;
        return new SubscriptionResponse(s.getId(), s.getRegistry().getId(), s.getRegistry().getSchoolName(),
                s.getPlan().getCode(), s.getPlan().getName(), s.getPlan().getMonthlyPriceFcfa(), s.getStatus(),
                s.getTrialStartedAt(), s.getTrialEndsAt(), daysLeft, billing.enabled());
    }
}
