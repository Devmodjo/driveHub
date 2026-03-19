package cm.mvtech.drivehub.platform.admin.repositories;


import cm.mvtech.drivehub.platform.admin.models.PlatformAdmin;
import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformAdminRepository extends JpaRepository<PlatformAdmin, Long> {

    boolean existsByRole(AdminRole role);

    Optional<PlatformAdmin> findByEmail(String email);

    List<PlatformAdmin> findByAdminStatus(AdminStatus adminStatus);
}
