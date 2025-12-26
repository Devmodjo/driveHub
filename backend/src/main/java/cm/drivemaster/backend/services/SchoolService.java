package cm.drivemaster.backend.services;

import cm.drivemaster.backend.models.dto.DrivingSchoolRequestDto;

public interface SchoolService {

    void createSchool(DrivingSchoolRequestDto req, long userId) throws IllegalAccessException;
}
