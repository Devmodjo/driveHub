package cm.drivemaster.backend.models.mappers;

import cm.drivemaster.backend.beans.ExamsInscription;
import cm.drivemaster.backend.models.dto.ExamsInscriptionRequestDto;
import cm.drivemaster.backend.models.dto.ExamsInscriptionResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ExamsInscriptionMapper {
    ExamsInscriptionMapper INSTANCE = Mappers.getMapper(ExamsInscriptionMapper.class);

    @Mapping(source = "examId", target = "exams.id")
    @Mapping(source = "studentId", target = "students.id")
    ExamsInscription fromRequestToEntity(ExamsInscriptionRequestDto examsInscriptionRequestDto);
    ExamsInscriptionResponseDto fromEntityToResponse(ExamsInscription examsInscription);
}