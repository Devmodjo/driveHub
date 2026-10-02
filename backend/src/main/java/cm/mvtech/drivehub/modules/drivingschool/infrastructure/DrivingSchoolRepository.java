package cm.mvtech.drivehub.modules.drivingschool.infrastructure;

import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Accès à la table driving_school du schéma de l'auto-école COURANTE (tenant).
 * Chaque schéma tenant ne contient qu'une seule ligne : l'auto-école elle-même.
 * À appeler via TenantExecutor.inTenant(...).
 */
@Repository
public interface DrivingSchoolRepository extends JpaRepository<DrivingSchool, UUID> {

    Optional<DrivingSchool> findFirstByOrderByCreatedAtAsc();
}
