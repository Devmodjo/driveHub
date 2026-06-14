package cm.mvtech.drivehub.modules.drivingschool.infrastructure;


import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface DrivingSchoolRegistryRepository
        extends JpaRepository<DrivingSchoolRegistry, UUID> {

    //  pour la validation tenante
    boolean existsBySchemaName(String schemaName);

    Optional<DrivingSchoolRegistry> findBySchemaName(String schemaName);

    Optional<DrivingSchoolRegistry> findByAdmin(User admin);

    @Query(value = "SELECT d FROM DrivingSchoolRegistry d WHERE d.drivingSchoolStatus= :status")
    List<DrivingSchoolRegistry> findByDrivingSchoolStatus(@Param("status") DrivingSchoolStatus status);

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

    @Query("""
                SELECT r FROM DrivingSchoolRegistry r
                LEFT JOIN FETCH r.admin a
                LEFT JOIN FETCH a.monitors
                WHERE r.drivingSchoolStatus = :status
            """)
    Page<DrivingSchoolRegistry> findByDrivingSchoolStatus(
            @Param("status") DrivingSchoolStatus status, Pageable pageable);

    @Query("""
                SELECT r FROM DrivingSchoolRegistry r
                LEFT JOIN FETCH r.admin a
                LEFT JOIN FETCH a.monitors
            """)
    Page<DrivingSchoolRegistry> findAllWithAdmin(Pageable pageable);

    long countByDrivingSchoolStatus(DrivingSchoolStatus status);


    @Query("""
            SELECT COUNT(r) FROM DrivingSchoolRegistry r
            WHERE r.admin.roles = :adminRoles
        """)
    long countByAdminRole(@Param("adminRoles") Role adminRole);


    @Query("""
                SELECT COUNT(r) FROM DrivingSchoolRegistry r
                WHERE r.createdAt >= :startDate
            """)
    long countByCreatedAtAfter(@Param("startDate") java.time.LocalDate startDate);
}
