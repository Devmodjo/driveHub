package cm.mvtech.drivehub.modules.drivingschool.application.dto;

import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DrivingSchoolMapper {
    DrivingSchoolMapper INSTANCE = Mappers.getMapper(DrivingSchoolMapper.class);

    DrivingSchool fromRequestToEntity(DrivingSchoolRequestDto drivingSchoolRequestDto);
    DrivingSchoolResponseDto fromEntityToResponse(DrivingSchool drivingSchool);
}