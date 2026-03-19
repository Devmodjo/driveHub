package cm.mvtech.drivehub.modules.drivingschool.domain.services;


import cm.mvtech.drivehub.modules.auth.domain.model.UserPrincipal;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolPendingRequestDTO;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolRequestDto;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolResponseDto;


import java.util.List;

public interface DrivingSchoolService {

    void createSchool(DrivingSchoolRequestDto req, UserPrincipal userPrincipal) throws IllegalAccessException;

    List<DrivingSchoolResponseDto> retreiveSchool();

    void approveRegistry(long registryId);

    List<DrivingSchoolPendingRequestDTO> retreivePendingRequest();

}
