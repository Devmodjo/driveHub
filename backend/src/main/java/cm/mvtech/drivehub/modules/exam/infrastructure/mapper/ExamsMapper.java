package cm.mvtech.drivehub.modules.exam.infrastructure.mapper;

import cm.mvtech.drivehub.modules.exam.application.dto.ExamsRequestDto;
import cm.mvtech.drivehub.modules.exam.application.dto.ExamsResponseDto;
import cm.mvtech.drivehub.modules.exam.domain.model.Exam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExamsMapper {

    void updateEntity(ExamsRequestDto request, @MappingTarget Exam exam);

    ExamsResponseDto fromEntityToResponse(Exam exam);
}
