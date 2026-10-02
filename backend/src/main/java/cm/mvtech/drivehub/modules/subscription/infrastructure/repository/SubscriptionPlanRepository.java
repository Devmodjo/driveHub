package cm.mvtech.drivehub.modules.subscription.infrastructure.repository;

import cm.mvtech.drivehub.modules.subscription.domain.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {
}
