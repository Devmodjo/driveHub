package cm.drivemaster.backend.models.mappers;

import cm.drivemaster.backend.beans.Students;
import cm.drivemaster.backend.models.dto.StudentsRequestDto;
import cm.drivemaster.backend.models.dto.StudentsResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StudentsMapper {
    StudentsMapper INSTANCE = Mappers.getMapper(StudentsMapper.class);
    @Mapping(source = "userId", target = "users.id")
    Students fromRequestToEntity(StudentsRequestDto studentsRequestDto);
    StudentsResponseDto fromEntityToResponse(Students students);
}