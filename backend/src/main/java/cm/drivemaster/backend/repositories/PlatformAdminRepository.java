package cm.drivemaster.backend.repositories;


import cm.drivemaster.backend.beans.PlatformAdmin;
import cm.drivemaster.backend.enums.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatformAdminRepository extends JpaRepository<PlatformAdmin, Long> {

    boolean existsByRole(AdminRole role);
    Optional<PlatformAdmin> findByEmail(String email);
}
