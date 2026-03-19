package cm.mvtech.drivehub.modules.course;

import cm.mvtech.drivehub.modules.course.domain.model.Course;
import cm.mvtech.drivehub.modules.course.application.CoursesRequestDto;
import cm.mvtech.drivehub.modules.course.application.CoursesResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CoursesMapper {

    CoursesMapper INSTANCE = Mappers.getMapper(CoursesMapper.class);

    @Mapping(source = "drivingSchoolId", target = "drivingSchool.id")
    Course fromRequestToEntity(CoursesRequestDto coursesRequestDto);
    CoursesResponseDto fromEntityToResponse(Course courses);
}