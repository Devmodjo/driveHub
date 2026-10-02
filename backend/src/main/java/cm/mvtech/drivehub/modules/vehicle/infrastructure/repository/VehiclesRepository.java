package cm.mvtech.drivehub.modules.vehicle.infrastructure.repository;

import cm.mvtech.drivehub.modules.vehicle.domain.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/** Correction : l'identifiant est un UUID (et non Long), comme dans EntityBase. */
@Repository
public interface VehiclesRepository extends JpaRepository<Vehicle, UUID> {

    boolean existsByMatriculationIgnoreCase(String matriculation);

    boolean existsByMatriculationIgnoreCaseAndIdNot(String matriculation, UUID id);

    Page<Vehicle> findAllBy(Pageable pageable);
}
