package cm.drivemaster.backend.models.mappers;

import cm.drivemaster.backend.beans.DrivingSchool;
import cm.drivemaster.backend.models.dto.DrivingSchoolRequestDto;
import cm.drivemaster.backend.models.dto.DrivingSchoolResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DrivingSchoolMapper {
    DrivingSchoolMapper INSTANCE = Mappers.getMapper(DrivingSchoolMapper.class);

    DrivingSchool fromRequestToEntity(DrivingSchoolRequestDto drivingSchoolRequestDto);
    DrivingSchoolResponseDto fromEntityToResponse(DrivingSchool drivingSchool);
}