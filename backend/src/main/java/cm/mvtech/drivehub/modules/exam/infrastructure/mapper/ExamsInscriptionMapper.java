package cm.mvtech.drivehub.modules.exam.infrastructure.mapper;

import cm.mvtech.drivehub.modules.exam.application.dto.ExamsInscriptionResponseDto;
import cm.mvtech.drivehub.modules.exam.domain.model.ExamsInscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExamsInscriptionMapper {

    @Mapping(source = "exams.id", target = "examId")
    @Mapping(source = "exams.dateExams", target = "dateExams")
    @Mapping(source = "exams.category", target = "category")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.user.firstname", target = "studentFirstname")
    @Mapping(source = "student.user.lastname", target = "studentLastname")
    ExamsInscriptionResponseDto fromEntityToResponse(ExamsInscription inscription);
}
