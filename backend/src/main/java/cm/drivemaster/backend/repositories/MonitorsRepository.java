package cm.drivemaster.backend.repositories;

import cm.drivemaster.backend.beans.Monitors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonitorsRepository extends JpaRepository<Monitors, Long> {
}
