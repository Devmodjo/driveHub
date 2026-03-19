package cm.mvtech.drivehub.modules.student;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StudentsMapper {
    StudentsMapper INSTANCE = Mappers.getMapper(StudentsMapper.class);
    @Mapping(source = "userId", target = "user.id")
    Student fromRequestToEntity(StudentsRequestDto studentsRequestDto);
    StudentsResponseDto fromEntityToResponse(Student student);
}