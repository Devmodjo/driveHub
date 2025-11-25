package cm.drivemaster.backend.repositories;

import cm.drivemaster.backend.beans.DrivingSchool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DrivingSchoolRepository extends JpaRepository<DrivingSchool, Long> {
}
