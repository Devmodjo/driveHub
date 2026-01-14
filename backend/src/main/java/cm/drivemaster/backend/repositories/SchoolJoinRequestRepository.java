package cm.drivemaster.backend.repositories;


import cm.drivemaster.backend.beans.SchoolJoinRequest;

import cm.drivemaster.backend.enums.JoinStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SchoolJoinRequestRepository extends JpaRepository<SchoolJoinRequest, Long> {

    Page<SchoolJoinRequest> findByDrivingSchoolIdAndJoinStatus(
            Long drivingSchoolId,
            JoinStatus status,
            Pageable pageable
    );
}
