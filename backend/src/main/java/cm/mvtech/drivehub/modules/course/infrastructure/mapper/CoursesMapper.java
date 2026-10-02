package cm.mvtech.drivehub.modules.course.infrastructure.mapper;

import cm.mvtech.drivehub.modules.course.application.dto.CoursesRequestDto;
import cm.mvtech.drivehub.modules.course.application.dto.CoursesResponseDto;
import cm.mvtech.drivehub.modules.course.domain.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CoursesMapper {

    void updateEntity(CoursesRequestDto request, @MappingTarget Course course);

    CoursesResponseDto fromEntityToResponse(Course course);
}
