package cm.mvtech.drivehub.platform.admin.repositories;


import cm.mvtech.drivehub.platform.admin.models.PlatformAdmin;
import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformAdminRepository extends JpaRepository<PlatformAdmin, UUID> {

    boolean existsByRole(AdminRole role);

    Optional<PlatformAdmin> findByEmail(String email);

    List<PlatformAdmin> findByAdminStatus(AdminStatus adminStatus);

    // Pagination avec filtres
    Page<PlatformAdmin> findByAdminStatus(AdminStatus status, Pageable pageable);
    Page<PlatformAdmin> findByRole(AdminRole role, Pageable pageable);
    Page<PlatformAdmin> findByAdminStatusAndRole(AdminStatus status,
                                                 AdminRole role,
                                                 Pageable pageable);

    List<PlatformAdmin> findByRole(AdminRole role);
    // Counts pour les stats
    long countByAdminStatus(AdminStatus status);
}
