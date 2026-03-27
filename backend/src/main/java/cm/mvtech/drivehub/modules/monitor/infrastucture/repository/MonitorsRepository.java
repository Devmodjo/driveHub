package cm.mvtech.drivehub.modules.monitor.infrastucture.repository;

import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MonitorsRepository extends JpaRepository<Monitor, UUID> {
}
