package cm.mvtech.drivehub.modules.subscription.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.subscription.application.dto.SubscriptionResponse;
import cm.mvtech.drivehub.modules.subscription.domain.model.SchoolSubscription;
import cm.mvtech.drivehub.modules.subscription.domain.model.SubscriptionPlan;
import cm.mvtech.drivehub.modules.subscription.domain.model.SubscriptionStatus;
import cm.mvtech.drivehub.modules.subscription.infrastructure.repository.SchoolSubscriptionRepository;
import cm.mvtech.drivehub.modules.subscription.infrastructure.repository.SubscriptionPlanRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Période d'essai de 15 jours démarrée à la validation de l'auto-école ; aucune facturation tant que billing.enabled = false. */
class SubscriptionServiceTest {

    private final SchoolSubscriptionRepository subscriptionRepository = mock(SchoolSubscriptionRepository.class);
    private final SubscriptionPlanRepository planRepository = mock(SubscriptionPlanRepository.class);
    private final DrivingSchoolRegistryRepository registryRepository = mock(DrivingSchoolRegistryRepository.class);
    private final SubscriptionService service = new SubscriptionService(subscriptionRepository, planRepository,
            registryRepository, new BillingProperties(false, 15, "ESSENTIEL"));

    private static SubscriptionPlan plan() {
        SubscriptionPlan plan = new SubscriptionPlan();
        ReflectionTestUtils.setField(plan, "code", "ESSENTIEL");
        ReflectionTestUtils.setField(plan, "name", "Essentiel");
        ReflectionTestUtils.setField(plan, "monthlyPriceFcfa", new BigDecimal("25000"));
        return plan;
    }

    private static DrivingSchoolRegistry registry() {
        DrivingSchoolRegistry registry = new DrivingSchoolRegistry();
        registry.setId(UUID.randomUUID());
        registry.setSchoolName("Auto-école Test");
        return registry;
    }

    @Test
    void startTrial_CreatesFifteenDayTrialOnDefaultPlan() {
        DrivingSchoolRegistry registry = registry();
        when(planRepository.findById("ESSENTIEL")).thenReturn(Optional.of(plan()));

        service.startTrial(registry);

        ArgumentCaptor<SchoolSubscription> saved = ArgumentCaptor.forClass(SchoolSubscription.class);
        verify(subscriptionRepository).save(saved.capture());
        SchoolSubscription s = saved.getValue();
        assertEquals(SubscriptionStatus.TRIAL, s.getStatus());
        assertEquals(15, Duration.between(s.getTrialStartedAt(), s.getTrialEndsAt()).toDays());
        assertSame(registry, s.getRegistry());
    }

    @Test
    void startTrial_IsIdempotent() {
        DrivingSchoolRegistry registry = registry();
        when(subscriptionRepository.existsByRegistry(registry)).thenReturn(true);

        service.startTrial(registry);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void forOwner_ReturnsDaysLeftAndBillingDisabled() {
        User owner = new User();
        DrivingSchoolRegistry registry = registry();
        SchoolSubscription s = new SchoolSubscription();
        s.setRegistry(registry);
        s.setPlan(plan());
        s.setStatus(SubscriptionStatus.TRIAL);
        s.setTrialStartedAt(LocalDateTime.now());
        s.setTrialEndsAt(LocalDateTime.now().plusDays(15).plusMinutes(1));
        when(registryRepository.findByAdmin(owner)).thenReturn(Optional.of(registry));
        when(subscriptionRepository.findByRegistry(registry)).thenReturn(Optional.of(s));

        SubscriptionResponse response = service.forOwner(owner).orElseThrow();

        assertEquals(15, response.trialDaysLeft());
        assertEquals("ESSENTIEL", response.planCode());
        assertFalse(response.billingEnabled());
    }

    @Test
    void forOwner_SchoolNotValidated_IsEmpty() {
        User owner = new User();
        when(registryRepository.findByAdmin(owner)).thenReturn(Optional.empty());
        assertTrue(service.forOwner(owner).isEmpty());
    }
}
