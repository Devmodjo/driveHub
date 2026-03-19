package cm.mvtech.drivehub.modules.monitor.infrastucture.repository;


import cm.mvtech.drivehub.modules.drivingschool.domain.model.SchoolJoinRequest;

import cm.mvtech.drivehub.modules.enums.JoinStatus;
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
