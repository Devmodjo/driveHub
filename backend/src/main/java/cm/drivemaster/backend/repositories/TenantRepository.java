package cm.drivemaster.backend.repositories;

import cm.drivemaster.backend.beans.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TenantRepository extends JpaRepository<TenantEntity, Long> {
    Optional<TenantEntity> findByCode(String code);
}
