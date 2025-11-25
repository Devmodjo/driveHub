package cm.drivemaster.backend.repositories;

import cm.drivemaster.backend.beans.Vehicles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehiclesRepository extends JpaRepository<Vehicles, Long> {
}
