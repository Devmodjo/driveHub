package cm.mvtech.drivehub.modules.exam;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ExamsInscriptionMapper {
    ExamsInscriptionMapper INSTANCE = Mappers.getMapper(ExamsInscriptionMapper.class);

    @Mapping(source = "examId", target = "exams.id")
    @Mapping(source = "studentId", target = "student.id")
    ExamsInscription fromRequestToEntity(ExamsInscriptionRequestDto examsInscriptionRequestDto);
    ExamsInscriptionResponseDto fromEntityToResponse(ExamsInscription examsInscription);
}