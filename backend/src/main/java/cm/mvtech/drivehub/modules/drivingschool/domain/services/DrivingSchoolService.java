package cm.mvtech.drivehub.modules.drivingschool.domain.services;


import cm.mvtech.drivehub.modules.auth.domain.model.UserPrincipal;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.*;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.UUID;

public interface DrivingSchoolService {

    void createSchool(DrivingSchoolRequestDto req, UserPrincipal userPrincipal) throws IllegalAccessException;

    List<DrivingSchoolResponseDto> retreiveSchool();

    void approveRegistry(UUID registryId);

    List<DrivingSchoolPendingRequestDTO> retreivePendingRequest();

    Page<DrivingSchoolRegistryPageDTO> getAllRegistries(Pageable pageable,
                                                        DrivingSchoolStatus status);

    DrivingSchoolRegistryPageDTO getRegistryById(UUID registryId);

    void rejectRegistry(UUID registryId);

    void suspendRegistry(UUID registryId);

    void deleteRegistry(UUID registryId);

    SchoolRegistryStatsResponse getRegistryStats();

}
