package cm.mvtech.drivehub.modules.student.infrastructure.mapper;

import cm.mvtech.drivehub.modules.student.application.dto.StudentsResponseDto;
import cm.mvtech.drivehub.modules.student.domain.model.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Conversion entité -> DTO (MapStruct génère l'implémentation à la compilation). */
@Mapper(componentModel = "spring")
public interface StudentsMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.firstname", target = "firstname")
    @Mapping(source = "user.lastname", target = "lastname")
    @Mapping(source = "user.email", target = "email")
    StudentsResponseDto fromEntityToResponse(Student student);
}
