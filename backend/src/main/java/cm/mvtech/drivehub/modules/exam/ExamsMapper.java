package cm.mvtech.drivehub.modules.exam;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ExamsMapper {
    ExamsMapper INSTANCE = Mappers.getMapper(ExamsMapper.class);

    @Mapping(source = "drivingSchoolId", target = "drivingSchool.id")
    Exam fromRequestToEntity(ExamsRequestDto examsRequestDto);
    ExamsResponseDto fromEntityToResponse(Exam exams);
}