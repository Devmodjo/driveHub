package cm.mvtech.drivehub.modules.monitor.infrastructure.repository;

import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonitorsRepository extends JpaRepository<Monitor, UUID> {

    Optional<Monitor> findFirstByUser_Id(UUID userId);

    @EntityGraph(attributePaths = "user")
    List<Monitor> findAllByOrderByCreatedOnAsc();
}
