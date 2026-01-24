package cm.drivemaster.backend.services;

import cm.drivemaster.backend.models.dto.DrivingSchoolRequestDto;
import cm.drivemaster.backend.models.dto.DrivingSchoolResponseDto;

import java.util.List;

public interface DrivingSchoolService {

    void createSchool(DrivingSchoolRequestDto req, long userId) throws IllegalAccessException;

    List<DrivingSchoolResponseDto> retreiveSchool();

    void approveRegistry(long registryId);

}
