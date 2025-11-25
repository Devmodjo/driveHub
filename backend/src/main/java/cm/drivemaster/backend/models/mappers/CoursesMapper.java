package cm.drivemaster.backend.models.mappers;

import cm.drivemaster.backend.beans.Courses;
import cm.drivemaster.backend.models.dto.CoursesRequestDto;
import cm.drivemaster.backend.models.dto.CoursesResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CoursesMapper {

    CoursesMapper INSTANCE = Mappers.getMapper(CoursesMapper.class);

    @Mapping(source = "drivingSchoolId", target = "drivingSchool.id")
    Courses fromRequestToEntity(CoursesRequestDto coursesRequestDto);
    CoursesResponseDto fromEntityToResponse(Courses courses);
}