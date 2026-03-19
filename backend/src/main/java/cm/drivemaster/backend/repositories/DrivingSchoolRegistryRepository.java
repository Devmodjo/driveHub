package cm.drivemaster.backend.repositories;


import cm.drivemaster.backend.beans.DrivingSchoolRegistry;
import cm.drivemaster.backend.beans.User;
import cm.drivemaster.backend.enums.DrivingSchoolStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DrivingSchoolRegistryRepository
        extends JpaRepository<DrivingSchoolRegistry, Long> {

    //  pour la validation tenant
    boolean existsBySchemaName(String schemaName);

    Optional<DrivingSchoolRegistry> findBySchemaName(String schemaName);

    Optional<DrivingSchoolRegistry> findByAdmin(User admin);

    @Query(value = "SELECT d FROM DrivingSchoolRegistry d WHERE d.drivingSchoolStatus= :status")
    List<DrivingSchoolRegistry> findByDrivingSchoolStatus(@Param("status") DrivingSchoolStatus  status);

    Optional<DrivingSchoolRegistry> findBySchoolName(String schoolName);
}
