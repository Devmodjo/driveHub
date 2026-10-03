package cm.mvtech.drivehub.modules.subscription.domain.model;

import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Abonnement d'une auto-école (table public.school_subscriptions, migration V15). */
@Entity
@Table(name = "school_subscriptions", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class SchoolSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registry_id", nullable = false, unique = true)
    private DrivingSchoolRegistry registry;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "plan_code", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "trial_started_at", nullable = false)
    private LocalDateTime trialStartedAt;

    @Column(name = "trial_ends_at", nullable = false)
    private LocalDateTime trialEndsAt;

    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
