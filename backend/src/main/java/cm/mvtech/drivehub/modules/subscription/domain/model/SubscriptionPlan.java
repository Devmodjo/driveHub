package cm.mvtech.drivehub.modules.subscription.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Offre (table public.subscription_plans, migration V15) : Essentiel, Business, Sur mesure. */
@Entity
@Table(name = "subscription_plans", schema = "public")
@Getter
@NoArgsConstructor
public class SubscriptionPlan {

    @Id
    @Column(length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    /** Prix mensuel en francs CFA ; null pour une offre sur devis. */
    @Column(name = "monthly_price_fcfa")
    private BigDecimal monthlyPriceFcfa;

    @Column(nullable = false)
    private boolean active;
}
