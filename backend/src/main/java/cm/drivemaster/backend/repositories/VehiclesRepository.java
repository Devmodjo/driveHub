package cm.drivemaster.backend.repositories;

import cm.drivemaster.backend.beans.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehiclesRepository extends JpaRepository<Vehicle, Long> {
}
