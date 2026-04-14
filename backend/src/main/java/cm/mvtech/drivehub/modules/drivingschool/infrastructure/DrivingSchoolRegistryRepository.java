package cm.mvtech.drivehub.modules.drivingschool.infrastructure;


import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface DrivingSchoolRegistryRepository
        extends JpaRepository<DrivingSchoolRegistry, UUID> {

    //  pour la validation tenante
    boolean existsBySchemaName(String schemaName);

    Optional<DrivingSchoolRegistry> findBySchemaName(String schemaName);

    Optional<DrivingSchoolRegistry> findByAdmin(User admin);

    @Query(value = "SELECT d FROM DrivingSchoolRegistry d WHERE d.drivingSchoolStatus= :status")
    List<DrivingSchoolRegistry> findByDrivingSchoolStatus(@Param("status") DrivingSchoolStatus  status);

    @Query("""
        SELECT r FROM DrivingSchoolRegistry r
        LEFT JOIN FETCH r.admin a
        LEFT JOIN FETCH a.monitors
        WHERE r.drivingSchoolStatus = :status
    """)
    List<DrivingSchoolRegistry> findByDrivingSchoolStatusWithAdmin(
            @Param("status") DrivingSchoolStatus status
    );

    Optional<DrivingSchoolRegistry> findBySchoolName(String schoolName);
}
