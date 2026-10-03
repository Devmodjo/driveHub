package cm.mvtech.drivehub.modules.subscription.infrastructure.repository;

import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.subscription.domain.model.SchoolSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SchoolSubscriptionRepository extends JpaRepository<SchoolSubscription, UUID> {

    Optional<SchoolSubscription> findByRegistry(DrivingSchoolRegistry registry);

    boolean existsByRegistry(DrivingSchoolRegistry registry);

    @EntityGraph(attributePaths = "registry")
    Page<SchoolSubscription> findAllByOrderByTrialEndsAtAsc(Pageable pageable);
}
