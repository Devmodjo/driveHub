package cm.mvtech.drivehub.modules.monitor.infrastructure.repository;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.SchoolJoinRequest;
import cm.mvtech.drivehub.modules.enums.JoinStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SchoolJoinRequestRepository extends JpaRepository<SchoolJoinRequest, UUID> {

    Page<SchoolJoinRequest> findByDrivingSchoolIdAndJoinStatus(
            UUID drivingSchoolId,
            JoinStatus status,
            Pageable pageable
    );

    /** Évite qu'un utilisateur envoie plusieurs fois la même demande. */
    boolean existsByUserAndDrivingSchoolIdAndJoinStatus(User user, UUID drivingSchoolId, JoinStatus status);

    /** Dernière adhésion acceptée : détermine l'auto-école (tenant) de l'utilisateur à la connexion. */
    Optional<SchoolJoinRequest> findFirstByUserAndJoinStatusOrderByCreatedOnDesc(User user, JoinStatus status);
}
