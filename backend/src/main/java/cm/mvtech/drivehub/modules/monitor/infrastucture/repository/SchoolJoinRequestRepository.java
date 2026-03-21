package cm.mvtech.drivehub.modules.monitor.infrastucture.repository;


import cm.mvtech.drivehub.modules.drivingschool.domain.model.SchoolJoinRequest;

import cm.mvtech.drivehub.modules.enums.JoinStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface SchoolJoinRequestRepository extends JpaRepository<SchoolJoinRequest, UUID> {

    Page<SchoolJoinRequest> findByDrivingSchoolIdAndJoinStatus(
            UUID drivingSchoolId,
            JoinStatus status,
            Pageable pageable
    );
}
