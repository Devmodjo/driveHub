package cm.drivemaster.backend.repositories;

import cm.drivemaster.backend.beans.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonitorsRepository extends JpaRepository<Monitor, Long> {
}
