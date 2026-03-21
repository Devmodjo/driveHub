package cm.mvtech.drivehub.modules.drivingschool.infrastructure;

import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DrivingSchoolRepository extends JpaRepository<DrivingSchool, UUID> {
}
