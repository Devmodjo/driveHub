package cm.drivemaster.backend.repositories;


import cm.drivemaster.backend.beans.DrivingSchoolRegistry;
import cm.drivemaster.backend.beans.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DrivingSchoolRegistryRepository
        extends JpaRepository<DrivingSchoolRegistry, Long> {

    // IMPORTANT pour la validation tenant
    boolean existsBySchemaName(String schemaName);

    Optional<DrivingSchoolRegistry> findBySchemaName(String schemaName);

    Optional<DrivingSchoolRegistry> findByAdmin(User admin);

    Optional<DrivingSchoolRegistry> findBySchoolName(String schoolName);
}
